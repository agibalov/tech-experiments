#!/bin/bash

set -x

command=$1

if [[ "${command}" == "test-mysql" ]]; then
  SPRING_DATASOURCE_USERNAME=user1 \
  SPRING_DATASOURCE_PASSWORD=password1 \
  SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3308/db1?allowPublicKeyRetrieval=true&useSSL=false" \
  ./gradlew clean gatlingRun-io.agibalov.DummyMysqlSimulation
elif [[ "${command}" == "test-postgres" ]]; then
  SPRING_DATASOURCE_USERNAME=user1 \
  SPRING_DATASOURCE_PASSWORD=qwerty \
  SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/db1" \
  ./gradlew clean gatlingRun-io.agibalov.DummyPostgresSimulation
elif [[ "${command}" == "" ]]; then
  echo "No command specified"
else
  echo "Unknown command ${command}"
fi
