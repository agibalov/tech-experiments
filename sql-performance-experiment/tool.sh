#!/bin/bash

set -x

command=$1

MysqlUsername=user1
MysqlPassword=password1
MysqlUrl="jdbc:mysql://localhost:3308/db1?allowPublicKeyRetrieval=true&useSSL=false"

PostgresUsername=user1
PostgresPassword=qwerty
PostgresUrl="jdbc:postgresql://localhost:5432/db1"

Region=us-east-1
MysqlStackName=mysql
PostgresStackName=postgres

undeploy_stack() {
  local stackName=$1
  aws cloudformation delete-stack \
    --stack-name ${stackName} \
    --region ${Region}

  aws cloudformation wait stack-delete-complete \
    --stack-name ${stackName} \
    --region ${Region}
}

get_stack_output() {
  local stackName=$1
  local outputName=$2
  aws cloudformation describe-stacks \
    --stack-name ${stackName} \
    --query 'Stacks[0].Outputs[?OutputKey==`'${outputName}'`].OutputValue' \
    --output text \
    --region ${Region}
}

if [[ "${command}" == "deploy-mysql" ]]; then
  envSpec=${envSpec:?not set or empty}
  aws cloudformation deploy \
    --template-file cloudformation/mysql.yml \
    --stack-name ${MysqlStackName} \
    --capabilities CAPABILITY_NAMED_IAM \
    --region ${Region} \
    --parameter-overrides \
    EnvSpec=${envSpec}
  if [[ $? -ne 0 ]]; then
    echo "Failed to deploy ${MysqlStackName}"
    exit 1
  fi

elif [[ "${command}" == "undeploy-mysql" ]]; then
  undeploy_stack ${MysqlStackName}

elif [[ "${command}" == "migrate-mysql" ]] || \
  [[ "${command}" == "populate-mysql" ]] || \
  [[ "${command}" == "test-mysql" ]]; then

  env=${env:?not set or empty}
  if [[ "${env}" == "local" ]]; then
    mysqlUsername=${MysqlUsername}
    mysqlPassword=${MysqlPassword}
    mysqlUrl="${MysqlUrl}"
  elif [[ "${env}" == "aws" ]]; then
    mysqlUsername=$(get_stack_output "${MysqlStackName}" "DbUsername")
    mysqlPassword=$(get_stack_output "${MysqlStackName}" "DbPassword")
    mysqlUrl="$(get_stack_output "${MysqlStackName}" "DbJdbcUrl")"
  else
    echo "should never get here"
    exit 1
  fi

  if [[ "${command}" == "migrate-mysql" ]]; then
    FLYWAY_USER=${mysqlUsername} \
    FLYWAY_PASSWORD=${mysqlPassword} \
    FLYWAY_URL="${mysqlUrl}" \
    FLYWAY_LOCATIONS="filesystem:./migrations/mysql" \
    ./gradlew clean flywayMigrate -i

  elif [[ "${command}" == "populate-mysql" ]]; then
    SPRING_PROFILES_ACTIVE=mysql \
    SPRING_DATASOURCE_USERNAME=${mysqlUsername} \
    SPRING_DATASOURCE_PASSWORD=${mysqlPassword} \
    SPRING_DATASOURCE_URL="${mysqlUrl}" \
    ./gradlew clean bootRun

  elif [[ "${command}" == "test-mysql" ]]; then
    simulation=${simulation:?not set or empty}

    SPRING_DATASOURCE_USERNAME=${mysqlUsername} \
    SPRING_DATASOURCE_PASSWORD=${mysqlPassword} \
    SPRING_DATASOURCE_URL="${mysqlUrl}" \
    ./gradlew clean gatlingRun-${simulation}

  else
    echo "should never get here"
    exit 1
  fi

elif [[ "${command}" == "deploy-postgres" ]]; then
  envSpec=${envSpec:?not set or empty}
  aws cloudformation deploy \
    --template-file cloudformation/postgres.yml \
    --stack-name ${PostgresStackName} \
    --capabilities CAPABILITY_NAMED_IAM \
    --region ${Region} \
    --parameter-overrides \
    EnvSpec=${envSpec}
  if [[ $? -ne 0 ]]; then
    echo "Failed to deploy ${PostgresStackName}"
    exit 1
  fi

elif [[ "${command}" == "undeploy-postgres" ]]; then
  undeploy_stack ${PostgresStackName}

elif [[ "${command}" == "migrate-postgres" ]] || \
  [[ "${command}" == "populate-postgres" ]] || \
  [[ "${command}" == "test-postgres" ]]; then

  env=${env:?not set or empty}
  if [[ "${env}" == "local" ]]; then
    postgresUsername=${PostgresUsername}
    postgresPassword=${PostgresPassword}
    postgresUrl="${PostgresUrl}"
  elif [[ "${env}" == "aws" ]]; then
    postgresUsername=$(get_stack_output "${PostgresStackName}" "DbUsername")
    postgresPassword=$(get_stack_output "${PostgresStackName}" "DbPassword")
    postgresUrl="$(get_stack_output "${PostgresStackName}" "DbJdbcUrl")"
  else
    echo "should never get here"
    exit 1
  fi

  if [[ "${command}" == "migrate-postgres" ]]; then
    FLYWAY_USER=${postgresUsername} \
    FLYWAY_PASSWORD=${postgresPassword} \
    FLYWAY_URL="${postgresUrl}" \
    FLYWAY_LOCATIONS="filesystem:./migrations/postgres" \
    ./gradlew clean flywayMigrate -i

  elif [[ "${command}" == "populate-postgres" ]]; then
    SPRING_PROFILES_ACTIVE=postgres \
    SPRING_DATASOURCE_USERNAME=${postgresUsername} \
    SPRING_DATASOURCE_PASSWORD=${postgresPassword} \
    SPRING_DATASOURCE_URL="${postgresUrl}" \
    ./gradlew clean bootRun

  elif [[ "${command}" == "test-postgres" ]]; then
    simulation=${simulation:?not set or empty}

    SPRING_DATASOURCE_USERNAME=${postgresUsername} \
    SPRING_DATASOURCE_PASSWORD=${postgresPassword} \
    SPRING_DATASOURCE_URL="${postgresUrl}" \
    ./gradlew clean gatlingRun-${simulation}

  else
    echo "should never get here"
    exit 1
  fi

elif [[ "${command}" == "" ]]; then
  echo "No command specified"
else
  echo "Unknown command ${command}"
fi
