FROM java:8-jre
COPY build/libs/sub-service.jar /tmp/app.jar
ENTRYPOINT exec java -jar /tmp/app.jar
EXPOSE 8080