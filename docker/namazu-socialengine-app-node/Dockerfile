
ARG BASE_TAG
FROM docker.namazustudios.net/namazu-elements-base:$BASE_TAG

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
EXPOSE 28883

RUN apt-get update -y && \
    apt-get install -y cmake libssl-dev libsasl2-dev lua5.3 lua5.3-dev luarocks && \
    wget https://github.com/mongodb/mongo-c-driver/releases/download/1.21.1/mongo-c-driver-1.21.1.tar.gz && \
    tar xzf mongo-c-driver-1.21.1.tar.gz && \
    cd mongo-c-driver-1.21.1 && \
    mkdir cmake-build && \
    cd cmake-build && \
    cmake -DENABLE_AUTOMATIC_INIT_AND_CLEANUP=OFF .. && \
    make install && \
    luarocks install lua-mongo

ENTRYPOINT ["java", "com.namazustudios.socialengine.appnode.ApplicationNodeMain"]
