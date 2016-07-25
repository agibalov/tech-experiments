Illustrates how to customize networking for Docker Compose.

* Have Docker (1.11.2) and Docker Compose (1.7.1) installed
* Have Docker configured to run without `sudo`
* Create a network using `./create-network.sh`. This will create a "test" network with subnet `172.25.0.0/16`.
* Run `docker-compose up`
* See how "tester" container says its address is 172.25.0.99, see how it pings `web1`, `web2` and `lb` by their IPs.
* Go to http://172.25.0.11:80 for web1
* Go to http://172.25.0.22:80 for web2
* Go to http://172.25.0.33:80 for lb (make sure to force refresh to see how round-robin works)
* Launch ubuntu/bash (`./test.sh`) to start an ubuntu container attached to `test` network. Ping all those hosts or curl them.
