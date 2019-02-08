FROM openjdk:8u141
LABEL author="Ibrahim Bilge <Ibrahim.Bilge@opuscapita.com>"

ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME

COPY build.gradle settings.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
COPY . $APP_HOME

RUN chmod +x ./gradlew
RUN ./gradlew build || return 0

HEALTHCHECK --interval=15s --timeout=3s --retries=15 \
  CMD curl --silent --fail http://localhost:8080/api/health/check || exit 1

EXPOSE 3036
ENTRYPOINT ["java", "-jar", "build/libs/peppol-inbound.jar"]