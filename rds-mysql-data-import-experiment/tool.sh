#!/bin/bash

set -x

Region=us-east-1

command=$1

get_stack_name() {
  local envTag=$1
  echo "${envTag}-data-import-experiment"
}

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

db_tool() {
  local command=${command:?not set or empty}
  local dbName=${dbName:?not set or empty}
  local host=${host:?not set or empty}
  local port=${port:?not set or empty}
  local user=${user:?not set or empty}
  local password=${password:?not set or empty}

  if [[ "${command}" == "create-db" ]]; then
    mysql \
      --user=${user} \
      --password=${password} \
      --host=${host} \
      --port=${port} \
      --execute="create database if not exists \`${dbName}\`;"

    FLYWAY_USER=${user} \
    FLYWAY_PASSWORD=${password} \
    FLYWAY_URL=jdbc:mysql://${host}:${port}/${dbName} \
    ./gradlew flywayMigrate

  elif [[ "${command}" == "destroy-db" ]]; then
    mysql \
      --user=${user} \
      --password=${password} \
      --host=${host} \
      --port=${port} \
      --execute="drop database if exists \`${dbName}\`;"

  elif [[ "${command}" == "" ]]; then
    echo "db_tool: No command specified"
    exit 1
  else
    echo "db_tool: Unknown command ${command}"
    exit 1
  fi
}

if [[ "${command}" == "deploy" ]]; then
  envTag=${envTag:?not set or empty}
  stackName=$(get_stack_name ${envTag})
  aws cloudformation deploy \
    --template-file template.yml \
    --stack-name ${stackName} \
    --capabilities CAPABILITY_NAMED_IAM \
    --region ${Region} \
    --parameter-overrides \
    EnvTag=${envTag}

elif [[ "${command}" == "undeploy" ]]; then
  envTag=${envTag:?not set or empty}
  stackName=$(get_stack_name ${envTag})
  undeploy_stack ${stackName}

elif [[ "${command}" == "run" ]]; then
  envTag=${envTag:?not set or empty}
  stackName=$(get_stack_name ${envTag})

  mysqlHost=$(get_stack_output ${stackName} "DbInstanceHost")
  mysqlPort=$(get_stack_output ${stackName} "DbInstancePort")
  mysqlUsername=$(get_stack_output ${stackName} "DbUsername")
  mysqlPassword=$(get_stack_output ${stackName} "DbPassword")

  dbName=db1

  command=create-db \
  dbName=${dbName} \
  host=${mysqlHost} \
  port=${mysqlPort} \
  user=${mysqlUsername} \
  password=${mysqlPassword} \
  db_tool

  MYSQL_HOST=${mysqlHost} \
  MYSQL_PORT=${mysqlPort} \
  MYSQL_USERNAME=${mysqlUsername} \
  MYSQL_PASSWORD=${mysqlPassword} \
  MYSQL_DATABASE=${dbName} \
  ./gradlew clean bootRun

elif [[ "${command}" == "" ]]; then
  echo "No command specified"
else
  echo "Unknown command ${command}"
fi
