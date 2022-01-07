
ARG BASE_TAG
FROM docker.namazustudios.net/namazu-elements-base:$BASE_TAG

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
EXPOSE 8083

ENTRYPOINT ["java", "com.namazustudios.socialengine.appserve.AppServeMain"]
