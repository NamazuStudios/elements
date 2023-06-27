
ARG BASE_TAG
FROM distribution.getelements.dev/elements-base:$BASE_TAG

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
EXPOSE 28883

USER root:root

RUN apt-get update && apt-get install -y build-essential libssl-dev unzip libsasl2-dev lua5.3 lua5.3-dev && \
    wget https://luarocks.org/releases/luarocks-3.9.1.tar.gz && \
    tar zxpf luarocks-3.9.1.tar.gz && \
    cd luarocks-3.9.1 && \
    ./configure && make && make install && \
    cd .. && \
    rm luarocks-3.9.1.tar.gz && \
    rm -rf luarocks-3.9.1 && \
    luarocks ldoc

USER elements:elements

ENTRYPOINT ["java", "dev.getelements.elements.appnode.ApplicationNodeMain"]
