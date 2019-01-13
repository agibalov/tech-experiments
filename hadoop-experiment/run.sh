./gradlew clean build
docker build -t loki2302/hadoop-experiment .
docker run -it loki2302/hadoop-experiment
