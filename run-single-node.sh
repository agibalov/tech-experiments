sudo rm -rf ./cassandra1data
docker rm $(docker ps -a -q)
docker-compose -f ./docker-compose-single-node.yml up
