FROM namazu-socialengine-base:latest

ARG APPLICATION_LIBS
COPY --chown=elements:elements $APPLICATION_LIBS $ELEMENTS_LIB/

EXPOSE 28883

ENTRYPOINT ["java", "com.namazustudios.socialengine.cdnserve.CdnServeMain"]
