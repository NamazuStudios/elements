FROM namazu-socialengine-base:latest

ARG APPLICATION_LIBS
COPY --chown=elements:elements $APPLICATION_LIBS $ELEMENTS_LIB/

EXPOSE 8083

ENTRYPOINT ["java", "com.namazustudios.socialengine.appserve.AppServeMain"]
