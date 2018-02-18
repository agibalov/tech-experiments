# openshift-experiment

TODO

#### Run OpenShift on Ubuntu 17.10 using Minishift and run some sample app

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
* Create new app based on some sample repository: `oc new-app centos/ruby-22-centos7~https://github.com/openshift/ruby-ex.git`. It will also trigger the build/deployment process. Use `oc logs -f bc/ruby-ex` to see how it goes.
* Expose the app: `oc expose service/ruby-ex`
* Show the app URL: `oc status`
