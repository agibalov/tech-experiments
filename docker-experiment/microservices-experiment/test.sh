#docker-compose up --abort-on-container-exit

docker-compose up --build --abort-on-container-exit

rm -rf ./reports
docker cp microservicesexperiment_tester_1:/tmp/test/integration-test/build/reports ./reports
