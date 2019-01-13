FROM qlik/gradle
ARG GRADLE_VERSION=2.14
COPY ./ /tmp/test
WORKDIR /tmp/test
ENTRYPOINT gradle -g /.gradle-cache integration-test:clean integration-test:test
ENV ADD_SERVICE_URL <bad adder service url>
ENV SUB_SERVICE_URL <bad subtractor service url>
ENV CALC_SERVICE_URL <bad calc service url>
