sudo rm -rf ./cassandra1data
sudo rm -rf ./cassandra2data
sudo rm -rf ./cassandra3data
docker rm $(docker ps -a -q)
docker-compose -f ./docker-compose-cluster.yml up
