sudo docker run --rm -P ubuntu /bin/sh -c "\
apt-get update && \
apt-get -y install apache2 && \
apache2ctl -D FOREGROUND
echo 'DONE!'"
