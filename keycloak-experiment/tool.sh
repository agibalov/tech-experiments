#!/bin/bash

set -x

command=$1

if [[ "${command}" == "init" ]]; then
  terraform init
elif [[ "${command}" == "deploy" ]]; then
  terraform apply -auto-approve ./tf
elif [[ "${command}" == "undeploy" ]]; then
  terraform destroy -auto-approve ./tf
fi
