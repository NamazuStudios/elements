FROM namazu-socialengine-base:latest

ARG APPLICATION_LIBS
COPY --chown=elements:elements $APPLICATION_LIBS $ELEMENTS_LIB/

EXPOSE 8085

ENTRYPOINT ["java", "docserve"]
