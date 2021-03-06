FROM namazu-socialengine-base:latest

ARG ELEMENTS_CLASSPATH
COPY --chown=elements:elements $ELEMENTS_CLASSPATH $ELEMENTS_LIB/

ENTRYPOINT ["java", "com.namazustudios.socialengine.setup.Setup"]
