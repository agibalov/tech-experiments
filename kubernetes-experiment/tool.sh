NAMESPACE=myns

command=$1

if [[ "${command}" == "deploy" ]]; then
  kubectl apply -f hello-application.yaml
elif [[ "${command}" == "undeploy" ]]; then
  kubectl delete namespace ${NAMESPACE}
elif [[ "${command}" == "test" ]]; then
  publicIp=$(kubectl get services \
    --namespace=${NAMESPACE} \
    --output=jsonpath='{.items[*].status.loadBalancer.ingress[0].ip}')
  echo "Load balancer IP: ${publicIp}"
  curl -w '\n' http://${publicIp}
elif [[ "${command}" == "" ]]; then
  echo "No command specified"
else
  echo "Unknown command: ${command}"
fi
