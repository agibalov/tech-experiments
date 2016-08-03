FROM aglover/java8-pier
COPY build/libs/calculator-service.jar /tmp/app.jar
ENTRYPOINT java -jar /tmp/app.jar
EXPOSE 8080
ENV CALC_ADD_SERVICE_URL <bad adder service url>
ENV CALC_SUB_SERVICE_URL <bad subtractor service url>
