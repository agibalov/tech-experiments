# microservices-experiment

* `add-service` has the only endpoint `/add/{a}/{b}`. It adds 2 numbers.
* `sub-service` has the only endpoint `/sub/{a}/{b}`. It subtracts 2 numbers.
* `calculator-service` has 2 endpoints `/add/{a}/{b}` and `/sub/{a}/{b}`. It delegates 2 `add-service` and `sub-service`.
* `integration-test` is a test-only project that talks to all 3 services and checks if they work as expected.

All 3 services are dockerized (see docker-compose.yml). The entire project is also dockerized to run integration tests via gradle.

First, run `./build.sh` to build everything, then run `./test.sh` to launch the whole thing and perform integration testing.
