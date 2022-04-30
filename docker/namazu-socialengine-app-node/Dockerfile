
ARG BASE_TAG
FROM docker.namazustudios.net/namazu-elements-base:$BASE_TAG

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
EXPOSE 28883

RUN apt-get install -y luarocks && apt-get install libmongoc-1.0-0 && luarocks install lua-mongo

ENTRYPOINT ["java", "com.namazustudios.socialengine.appnode.ApplicationNodeMain"]
