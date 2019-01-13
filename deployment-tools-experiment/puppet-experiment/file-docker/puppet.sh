sudo docker run --rm -v `pwd`:/opt/test ubuntu /bin/sh -c "\
apt-get update &&\
apt-get install -y puppet &&\
puppet apply /opt/test/test.pp &&\
cat /opt/1.txt
echo 'done'\
"