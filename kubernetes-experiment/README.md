# kubernetes-experiment

1. Have `kubectl` installed locally.
2. Create Kubernetes cluster on DigitalOcean and download the cluster configuration file.
3. Before using `kubectl`, do `export KUBECONFIG=<name of cluster config file>.yaml`
4. `./tool.sh deploy` (note: deployment is asynchronous, give it some time before `test`), `./tool.sh test`, `./tool.sh undeploy`
