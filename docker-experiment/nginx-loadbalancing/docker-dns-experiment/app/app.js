var Docker = require('dockerode');
var dnsd = require('dnsd');
var detectSeries = require('async/detectSeries');

var DOCKER_SOCK = process.env.DOCKER_SOCK || '/var/run/docker.sock';
var DNS_PORT = parseInt(process.env.DNS_PORT || '1153');

var docker = new Docker({ socketPath: DOCKER_SOCK });

dnsd.createServer(function(req, res) {
  var hostname = req.question[0].name;

  docker.listContainers(function(err, containerInfos) {
    if(err) {
      throw err;
    }

    detectSeries(containerInfos, function(containerInfo, callback) {
      var container = docker.getContainer(containerInfo.Id);
      container.inspect(function(err, containerData) {
        if(err) {
          callback(err);
          return;
        }

        var containerId = containerData.Id;
        var containerIp = containerData.NetworkSettings.IPAddress;
        if(!containerIp) {
          for(var networkName in containerData.NetworkSettings.Networks) {
            var network = containerData.NetworkSettings.Networks[networkName];
            if(network.IPAddress) {
              containerIp = network.IPAddress;
              break;
            }
          }
        }
        
        var containerEnv = containerData.Config.Env;
        if(!containerEnv) {
          callback(null, false);
          return;
        }

        var foundHost = false;
        for(var j = 0; j < containerEnv.length; ++j) {
          var kvp = containerEnv[j];
          var kvpArray = kvp.split('=');
          if(kvpArray.length !== 2) {
            continue;
          }

          var key = kvpArray[0];
          if(key !== 'DNSEXP_NAME') {
            continue;
          }

          var containerDnsName = kvpArray[1];
          if(containerDnsName === hostname) {
            res.end(containerIp);
            foundHost = true;
            break;
          }
        }

        callback(null, foundHost);        
      });
    }, function(err, item) {
      if(err) {
        throw err;
      }

      if(!item) {
        res.end();
      }
    });
  });
}).listen(DNS_PORT);
