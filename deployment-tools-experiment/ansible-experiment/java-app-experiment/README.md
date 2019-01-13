# java-app-experiment

Create DO droplet. Do `sudo apt install python` on that droplet. Put its IP to `hosts`.

* `./gradlew clean bootRepackage`
* `./tool.sh install` - once per node
* `./tool.sh deploy` - once per deployment
* Check `...:80/` - once per deployment
* `./tool.sh undeploy` - once per deployment
* `./tool.sh uninstall` - once per node
