FROM namazu-socialengine-base:latest

ARG APPLICATION_LIBS
COPY --chown=elements:elements $APPLICATION_LIBS $ELEMENTS_LIB/

ENTRYPOINT ["java", "com.namazustudios.socialengine.setup.Setup"]
