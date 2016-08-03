FROM qlik/gradle
ARG GRADLE_VERSION=2.14
COPY ./ /tmp/test
WORKDIR /tmp/test
ENTRYPOINT gradle integration-test:clean integration-test:test
