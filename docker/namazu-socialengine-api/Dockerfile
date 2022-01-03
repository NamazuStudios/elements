
ARG BASE_TAG
FROM docker.namazustudios.net/namazu-elements-base:$BASE_TAG

COPY --chown=elements:elements target/dependency/*.jar $ELEMENTS_LIB/
EXPOSE 8081

ENTRYPOINT ["java", "com.namazustudios.socialengine.rest.RestAPIMain"]
