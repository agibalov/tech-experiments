#!/bin/bash

set -x

command=$1

MysqlUsername=user1
MysqlPassword=password1
MysqlUrl="jdbc:mysql://localhost:3308/db1?allowPublicKeyRetrieval=true&useSSL=false"

PostgresUsername=user1
PostgresPassword=qwerty
PostgresUrl="jdbc:postgresql://localhost:5432/db1"

if [[ "${command}" == "migrate-mysql" ]]; then
  FLYWAY_USER=${MysqlUsername} \
  FLYWAY_PASSWORD=${MysqlPassword} \
  FLYWAY_URL="${MysqlUrl}" \
  FLYWAY_LOCATIONS="filesystem:./migrations/mysql" \
  ./gradlew clean flywayMigrate -i

elif [[ "${command}" == "populate-mysql" ]]; then
  SPRING_PROFILES_ACTIVE=mysql \
  SPRING_DATASOURCE_USERNAME=${MysqlUsername} \
  SPRING_DATASOURCE_PASSWORD=${MysqlPassword} \
  SPRING_DATASOURCE_URL="${MysqlUrl}" \
  ./gradlew clean bootRun

elif [[ "${command}" == "test-mysql" ]]; then
  SPRING_DATASOURCE_USERNAME=${MysqlUsername} \
  SPRING_DATASOURCE_PASSWORD=${MysqlPassword} \
  SPRING_DATASOURCE_URL="${MysqlUrl}" \
  ./gradlew clean gatlingRun-io.agibalov.DummyMysqlSimulation

elif [[ "${command}" == "migrate-postgres" ]]; then
  FLYWAY_USER=${PostgresUsername} \
  FLYWAY_PASSWORD=${PostgresPassword} \
  FLYWAY_URL="${PostgresUrl}" \
  FLYWAY_LOCATIONS="filesystem:./migrations/postgres" \
  ./gradlew clean flywayMigrate -i

elif [[ "${command}" == "populate-postgres" ]]; then
  SPRING_PROFILES_ACTIVE=postgres \
  SPRING_DATASOURCE_USERNAME=${PostgresUsername} \
  SPRING_DATASOURCE_PASSWORD=${PostgresPassword} \
  SPRING_DATASOURCE_URL="${PostgresUrl}" \
  ./gradlew clean bootRun

elif [[ "${command}" == "test-postgres" ]]; then
  SPRING_DATASOURCE_USERNAME=${PostgresUsername} \
  SPRING_DATASOURCE_PASSWORD=${PostgresPassword} \
  SPRING_DATASOURCE_URL="${PostgresUrl}" \
  ./gradlew clean gatlingRun-io.agibalov.DummyPostgresSimulation

elif [[ "${command}" == "" ]]; then
  echo "No command specified"
else
  echo "Unknown command ${command}"
fi
