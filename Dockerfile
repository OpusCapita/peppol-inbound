FROM openjdk:8u141
LABEL author="Ibrahim Bilge <Ibrahim.Bilge@opuscapita.com>"

# custom persistence module
ADD ["build/libs/peppol-inbound.jar", "peppol-inbound.jar"]

EXPOSE 8080

#HEALTHCHECK --interval=15s --timeout=3s --retries=12 \
#  CMD curl --silent --fail http://localhost:3008/api/health/check || exit 1

ENTRYPOINT ["java","-jar","peppol-inbound.jar"]