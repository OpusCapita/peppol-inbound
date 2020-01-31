## using multistage docker build for speed
## temp container to build
FROM openjdk:8 AS TEMP_BUILD_IMAGE

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

ADD libs/oxalis-as4.tar.gz $APP_HOME/libs
COPY . $APP_HOME
RUN rm $APP_HOME/libs/oxalis-as4.tar.gz

RUN chmod +x ./gradlew
RUN ./gradlew -q build || return 0

## actual container
FROM openjdk:8
LABEL author="Ibrahim Bilge <Ibrahim.Bilge@opuscapita.com>"

## setting heap size automatically to the container memory limits
ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -XX:MaxRAMFraction=1 -XshowSettings:vm"

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY --from=TEMP_BUILD_IMAGE $APP_HOME/oxalis oxalis
COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/peppol-inbound.jar .

HEALTHCHECK --interval=15s --timeout=30s --start-period=40s --retries=15 \
  CMD curl --silent --fail http://localhost:3037/api/health/check || exit 1

EXPOSE 3037
ENTRYPOINT exec java $JAVA_OPTS -jar peppol-inbound.jar