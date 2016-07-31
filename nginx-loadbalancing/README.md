Illustrates how to customize networking for Docker Compose.

* Have Docker (1.11.2) and Docker Compose (1.7.1) installed
* Have Docker configured to run without `sudo`
* Create a network using `./create-network.sh`. This will create a "test" network with subnet `172.25.0.0/16`.

There are 4 network configuration experiment. In all cases there are 2 web nodes (web1 and web2) which are behind the load-balancer node. There's also a tester container which tries to ping all these 3 nodes.

### dnsdock experiment

[dnsdock](https://github.com/tonistiigi/dnsdock) is a DNS server that takes its data from Docker. As of today it only seems to [support](https://github.com/tonistiigi/dnsdock/issues/67) the bridge networking scenario. Tester container pings nodes by names like `node1.omg.docker`.

### dnsexp-bridge and dnsexp-external experiments

A primitive [DNS server](https://github.com/loki2302/docker-experiment/blob/master/nginx-loadbalancing/docker-dns-experiment/app/app.js) inspired by dnsdock. Same thing, but works fine in both bridge and external network modes. Test container pings nodes by names like `node1` (bridge case) or `node1.test` (external network case).

### staticips experiment

No DNS. Tester pings nodes by static IPs.

### other

* Launch ubuntu/bash (`./test.sh`) to start an ubuntu container attached to `test` network. Ping all those hosts or curl them.
