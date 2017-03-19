sudo docker run --rm ubuntu /bin/sh -c "\
apt-get update && \
apt-get -y install git default-jdk && \
mkdir /opt/test && \
cd /opt/test && \
git clone https://github.com/loki2302/spring-jpa-and-sql-experiment.git app && \
cd app && \
chmod +x ./gradlew && \
./gradlew clean test && \
echo 'DONE!'"
