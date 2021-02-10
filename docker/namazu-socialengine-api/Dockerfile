FROM namazu-socialengine-base:latest

ARG ELEMENTS_CLASSPATH
COPY --chown=elements:elements $ELEMENTS_CLASSPATH $ELEMENTS_LIB/

EXPOSE 8081

ENTRYPOINT ["java", "com.namazustudios.socialengine.rest.RestAPIMain"]
