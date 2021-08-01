# sql-performance-experiment

## Mysql

### Local Mysql

Launch Mysql locally using Docker Compose:

* `docker-compose up --build --no-start mysql` to construct the Mysql container.
* `docker-compose start mysql` to start Mysql.
* `docker-compose stop mysql` to stop Mysql.

Mysql will be available at localhost:3308. See `docker-compose.yml` for credentials.

### AWS RDS Mysql

* `envSpec=<ENV_SPEC> ./tool.sh deploy-mysql` to deploy the mysql CloudFormation stack. `<ENV_SPEC>` is `micro` or `small`.
* `./tool.sh undeploy-mysql` to undeploy the mysql CloudFormation stack.

### Applying migrations

* `env=<ENV> ./tool.sh migrate-mysql` to apply DB migrations. `<ENV>` is `local` or `aws`.
  
### Populating the database

* `env=<ENV> ./tool.sh populate-mysql` to populate the database. `<ENV>` is `local` or `aws`.

### To run the test

* `env=<ENV> ./tool.sh test-mysql`. `<ENV>` is `local` or `aws`.

## Postgres

### Local Postgres

Launch Postgres locally using Docker Compose:

* `docker-compose up --build --no-start postgres` to construct the Postgres container.
* `docker-compose start postgres` to start Postgres.
* `docker-compose stop postgres` to stop Postgres.

Postgres will be available at localhost:5432.  See `docker-compose.yml` for credentials.

### AWS RDS Postgres

* `envSpec=<ENV_SPEC> ./tool.sh deploy-postgres` to deploy the postgres CloudFormation stack. `<ENV_SPEC>` is `micro` or `small`.
* `./tool.sh undeploy-postgres` to undeploy the postgres CloudFormation stack.

### Applying migrations

* `env=<ENV> ./tool.sh migrate-postgres` to apply DB migrations. `<ENV>` is `local` or `aws`.

### Populating the database

* `env=<ENV> ./tool.sh populate-postgres` to populate the database. `<ENV>` is `local` or `aws`.

### To run the test

* `env=<ENV> ./tool.sh test-postgres`. `<ENV>` is `local` or `aws`.
