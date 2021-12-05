# sql-performance-experiment

In this PoC I try to use Gatling to measure SQL query performance.

## What's being measured

In the database, there are 2 tables: `Accounts` and `Users`. The connection is one Account to many Users. There are 100 rows in the `Accounts` table, and each Account has 200,000 rows in the `Users` table.

The tests I run are:

* `...CountAllUsers` which tests the `select count(*) from Users` query.
* `...CountUsersInOneAccount` which tests the `select count(*) from Users where accountId = ?` query.

## How to run

* Deploy Mysql and Postgres stacks to AWS. Use the `large` env spec. It translated to `db.m5.large` RDS instance type, which is the "smallest" available instance type with predictable performance (no burst capacity, etc).
* Apply migrations to the databases.
* Populate the databases.
* Run tests against Mysql:
  * `env=aws simulation=io.agibalov.MysqlCountAllUsersSimulation ./tool.sh test-mysql`
  * `env=aws simulation=io.agibalov.MysqlCountUsersInOneAccountSimulation ./tool.sh test-mysql`
* Run tests against Postgres:
  * `env=aws simulation=io.agibalov.PostgresCountAllUsersSimulation ./tool.sh test-postgres`
  * `env=aws simulation=io.agibalov.PostgresCountUsersInOneAccountSimulation ./tool.sh test-postgres`

## Results

As of Dec 5, 2021:

### `MysqlCountAllUsersSimulation`
```
> request count                                          5 (OK=5      KO=0     )
> min response time                                  75396 (OK=75396  KO=-     )
> max response time                                  76045 (OK=76045  KO=-     )
> mean response time                                 75679 (OK=75679  KO=-     )
> std deviation                                        212 (OK=212    KO=-     )
> response time 50th percentile                      75649 (OK=75649  KO=-     )
> response time 75th percentile                      75716 (OK=75716  KO=-     )
> response time 95th percentile                      75979 (OK=75979  KO=-     )
> response time 99th percentile                      76032 (OK=76032  KO=-     )
> mean requests/sec                                  0.013 (OK=0.013  KO=-     )
```

### `MysqlCountUsersInOneAccountSimulation`
```
> request count                                         10 (OK=10     KO=0     )
> min response time                                    131 (OK=131    KO=-     )
> max response time                                    448 (OK=448    KO=-     )
> mean response time                                   165 (OK=165    KO=-     )
> std deviation                                         94 (OK=94     KO=-     )
> response time 50th percentile                        134 (OK=134    KO=-     )
> response time 75th percentile                        136 (OK=136    KO=-     )
> response time 95th percentile                        308 (OK=308    KO=-     )
> response time 99th percentile                        420 (OK=420    KO=-     )
> mean requests/sec                                      5 (OK=5      KO=-     )
```

### `PostgresCountAllUsersSimulation`
```
> request count                                         10 (OK=10     KO=0     )
> min response time                                   1181 (OK=1181   KO=-     )
> max response time                                   1691 (OK=1691   KO=-     )
> mean response time                                  1246 (OK=1246   KO=-     )
> std deviation                                        149 (OK=149    KO=-     )
> response time 50th percentile                       1197 (OK=1197   KO=-     )
> response time 75th percentile                       1210 (OK=1210   KO=-     )
> response time 95th percentile                       1480 (OK=1480   KO=-     )
> response time 99th percentile                       1649 (OK=1649   KO=-     )
> mean requests/sec                                  0.769 (OK=0.769  KO=-     )
```

### `PostgresCountUsersInOneAccountSimulation`
```
> request count                                         10 (OK=10     KO=0     )
> min response time                                     46 (OK=46     KO=-     )
> max response time                                    362 (OK=362    KO=-     )
> mean response time                                    79 (OK=79     KO=-     )
> std deviation                                         94 (OK=94     KO=-     )
> response time 50th percentile                         48 (OK=48     KO=-     )
> response time 75th percentile                         49 (OK=49     KO=-     )
> response time 95th percentile                        222 (OK=222    KO=-     )
> response time 99th percentile                        334 (OK=334    KO=-     )
> mean requests/sec                                     10 (OK=10     KO=-     )
```

## CLI reference

### Mysql

#### Local Mysql

Launch Mysql locally using Docker Compose:

* `docker-compose up --build --no-start mysql` to construct the Mysql container.
* `docker-compose start mysql` to start Mysql.
* `docker-compose stop mysql` to stop Mysql.

Mysql will be available at `localhost:3308`. See `docker-compose.yml` for credentials.

#### AWS RDS Mysql

* `envSpec=<ENV_SPEC> ./tool.sh deploy-mysql` to deploy the mysql CloudFormation stack. `<ENV_SPEC>` is `small` or `large`.
* `./tool.sh undeploy-mysql` to undeploy the mysql CloudFormation stack.

#### Applying migrations

* `env=<ENV> ./tool.sh migrate-mysql` to apply DB migrations. `<ENV>` is `local` or `aws`.

#### Populating the database

* `env=<ENV> ./tool.sh populate-mysql` to populate the database. `<ENV>` is `local` or `aws`.

#### To run the test

* `env=<ENV> simulation=<SIMULATION> ./tool.sh test-mysql`. `<ENV>` is `local` or `aws`. `<SIMULATION>` is a fully qualified class name of Gatling simulation to run.

### Postgres

#### Local Postgres

Launch Postgres locally using Docker Compose:

* `docker-compose up --build --no-start postgres` to construct the Postgres container.
* `docker-compose start postgres` to start Postgres.
* `docker-compose stop postgres` to stop Postgres.

Postgres will be available at `localhost:5432`.  See `docker-compose.yml` for credentials.

#### AWS RDS Postgres

* `envSpec=<ENV_SPEC> ./tool.sh deploy-postgres` to deploy the postgres CloudFormation stack. `<ENV_SPEC>` is `small` or `large`.
* `./tool.sh undeploy-postgres` to undeploy the postgres CloudFormation stack.

#### Applying migrations

* `env=<ENV> ./tool.sh migrate-postgres` to apply DB migrations. `<ENV>` is `local` or `aws`.

#### Populating the database

* `env=<ENV> ./tool.sh populate-postgres` to populate the database. `<ENV>` is `local` or `aws`.

#### To run the test

* `env=<ENV> simulation=<SIMULATION> ./tool.sh test-postgres`. `<ENV>` is `local` or `aws`. `<SIMULATION>` is a fully qualified class name of Gatling simulation to run.
