
ARG BASE_TAG
FROM docker.namazustudios.net/namazu-elements-base:$BASE_TAG

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
EXPOSE 28883

USER root:root

RUN apt-get install -y cmake libssl-dev libsasl2-dev lua5.3 lua5.3-dev luarocks && \
    wget https://github.com/mongodb/mongo-c-driver/releases/download/1.21.1/mongo-c-driver-1.21.1.tar.gz && \
    tar xzf mongo-c-driver-1.21.1.tar.gz && \
    cd mongo-c-driver-1.21.1 && \
    mkdir cmake-build && \
    cd cmake-build && \
    cmake -DENABLE_AUTOMATIC_INIT_AND_CLEANUP=OFF .. && \
    make install && \
    luarocks install lua-mongo && \
    apt-get remove -y luarocks lua5.3-dev lua5.3 libsasl2-dev libssl-dev cmake && \
    apt autoremove && \
    cd ../.. && \
    rm -f mongo-c-driver-1.21.1.tar.gz && \
    rm -rf mongo-c-driver-1.21.1

USER elements:elements

ENTRYPOINT ["java", "com.namazustudios.socialengine.appnode.ApplicationNodeMain"]
