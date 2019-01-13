# openshift-experiment

TODO

#### Run OpenShift on Ubuntu 17.10 using Minishift and deploy a dummy Spring Boot / MVC app (WildFly)

* Download OpenShift: https://www.openshift.org/download.html (it should be OK to download just the client tools package)
* Download Minishift: https://github.com/minishift/minishift/releases
* Configure Ubuntu as described here: https://docs.openshift.org/latest/minishift/getting-started/setting-up-driver-plugin.html (they refer to group `libvirtd`, but it's just `libvirt` on 17.10)
* Append both OpenShift and Minishift to `PATH` - add these to `~/.bashrc`:
   ```
   export PATH="~/openshift/":$PATH 
   export PATH="~/minishift/":$PATH
   ```
* Do `minishift start`. It should launch everything without asking any other questions. It will print the web console URL (like `https://192.168.42.118:8443`). You should be able to log in and see something similar to openshift.com console.
* Go to web console and copy the login command (`oc login https://....`). Run this command to log in.
* Create new project: `oc new-project dummy-project`
* Create new app based on some sample repository: `oc new-app https://github.com/agibalov/openshift-experiment.git --context-dir dummyapp2 --image-stream="openshift/wildfly:10.0"`. It will also trigger the build/deployment process. Use `oc logs -f bc/openshift-experiment` to see how it goes.
* Expose the app: `oc expose service/openshift-experiment`
* Show the app URL: `oc status`. The actual app URL is something like `http://openshift-experiment-dummy-project1.192.168.42.118.nip.io/dummy-app-2-1.0-SNAPSHOT/`
