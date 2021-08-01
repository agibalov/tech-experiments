# sql-performance-experiment

## Mysql

* `docker-compose up --build --no-start mysql` to construct the Mysql container.
* `docker-compose start mysql` to start Mysql.
* `docker-compose stop mysql` to stop Mysql.

Mysql will be available at localhost:3308. See `docker-compose.yml` for credentials. Mysql container stores its data in `.data` directory. Stop the container and delete this directory if you want Mysql to re-initialize from scratch.

## Postgres

* `docker-compose up --build --no-start postgres` to construct the Postgres container.
* `docker-compose start postgres` to start Postgres.
* `docker-compose stop postgres` to stop Postgres.

## Mysql

* `envSpec=<ENV_SPEC> ./tool.sh deploy-mysql`. `<ENV_SPEC>` is `micro` or `small`.
* `./tool.sh undeploy-mysql`
* `env=<ENV> ./tool.sh migrate-mysql`
* `env=<ENV> ./tool.sh populate-mysql`
* `env=<ENV> ./tool.sh test-mysql`

## Postgres

* `envSpec=<ENV_SPEC> ./tool.sh deploy-postgres`. `<ENV_SPEC>` is `micro` or `small`.
* `./tool.sh undeploy-postgres`
* `env=<ENV> ./tool.sh migrate-postgres`
* `env=<ENV> ./tool.sh populate-postgres`
* `env=<ENV> ./tool.sh test-postgres`
