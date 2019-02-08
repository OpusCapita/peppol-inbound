## using multistage docker build for speed
## temp container to build
FROM openjdk:8u141 AS TEMP_BUILD_IMAGE
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
RUN chmod +x ./gradlew
RUN ./gradlew build || return 0

## actual container
FROM openjdk:8u141
LABEL author="Ibrahim Bilge <Ibrahim.Bilge@opuscapita.com>"

ENV ARTIFACT_NAME=peppol-inbound.jar
ENV APP_HOME=/usr/app/

WORKDIR $APP_HOME

COPY --from=TEMP_BUILD_IMAGE $APP_HOME/build/libs/peppol-inbound.jar .

HEALTHCHECK --interval=15s --timeout=3s --retries=15 \
  CMD curl --silent --fail http://localhost:3008/api/health/check || exit 1

EXPOSE 3036
ENTRYPOINT ["java","-jar","peppol-inbound.jar"]