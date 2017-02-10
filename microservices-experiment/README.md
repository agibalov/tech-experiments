# microservices-experiment

* `add-service` has the only endpoint `/add/{a}/{b}`. It adds 2 numbers.
* `sub-service` has the only endpoint `/sub/{a}/{b}`. It subtracts 2 numbers.
* `calculator-service` has 2 endpoints `/add/{a}/{b}` and `/sub/{a}/{b}`. It delegates 2 `add-service` and `sub-service`.
* `integration-test` is a test-only project that talks to all 3 services and checks if they work as expected.

All 3 services are dockerized (see docker-compose.yml). The entire project is also dockerized to run integration tests via gradle.

First, run `./build.sh` to build everything, then run `./test.sh` to launch the whole thing and perform integration testing.

## PM2

There's also an attempt to replace (to some degree) Docker Compose with [PM2](http://pm2.keymetrics.io/). See how similar `services.yml` is to `docker-compose.yml`.

* Do `build.sh` to build service JARs
* Do `npm i` to install PM2
* Do `npm start` to start all services
* Do `npm stop` to stop all service
* Do `npm run-script monitor` to monitor all services

Test like this:

```bash
loki2302@ubuntu:~$ curl -w "\n" http://localhost:8081/add/2/3
{"a":2,"b":3,"result":5}
loki2302@ubuntu:~$ curl -w "\n" http://localhost:8082/sub/2/3
{"a":2,"b":3,"result":-1}
loki2302@ubuntu:~$ curl -w "\n" http://localhost:8083/add/2/3
{"a":2,"b":3,"result":5}
loki2302@ubuntu:~$ curl -w "\n" http://localhost:8083/sub/2/3
{"a":2,"b":3,"result":-1}
```
