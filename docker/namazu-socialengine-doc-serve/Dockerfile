ARG BASE_TAG
FROM docker.namazustudios.net/namazu-elements-base:$BASE_TAG

USER root:root
RUN apt-get update && apt-get install lua-ldoc

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/

EXPOSE 8085

USER elements:elements
ENTRYPOINT ["java", "docserve"]
