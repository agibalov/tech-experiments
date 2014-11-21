sudo docker run --rm --name pg1 -v `pwd`:/opt/test -p 5432 ubuntu /bin/sh -c "\
apt-get update &&\
apt-get install -y puppet &&\
puppet module install puppetlabs/postgresql
puppet apply /opt/test/postgres.pp &&\
sudo -u postgres psql -c '\l' &&\
sudo -u postgres psql -c 'select 1+2' &&\
sleep infinity &&\
echo 'done'\
"
