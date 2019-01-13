Proudly copypasted from here:
http://fabiorehm.com/blog/2014/09/11/running-gui-apps-with-docker/

Run firefox in Docker container.
Before running, update Dockerfile with your UID and GID.
These can be found here: `id -u loki2302` and `id -g loki2302`

1. `build.sh`
2. `run.sh`

(`run-test.sh` will attach container to "test" network)
