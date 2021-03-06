FROM namazu-socialengine-base:latest

ARG ELEMENTS_CLASSPATH
COPY --chown=elements:elements $ELEMENTS_CLASSPATH $ELEMENTS_LIB/

EXPOSE 8082

ENTRYPOINT ["java", "com.namazustudios.socialengine.codeserve.CodeServeMain"]
