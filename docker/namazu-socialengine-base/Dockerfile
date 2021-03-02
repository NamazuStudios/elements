FROM openjdk:11.0-jdk

ENV DOCKERIZE_VERSION v0.6.1

# Home Directory
ENV ELEMENTS_HOME "/opt/elements"

# Sub Directories
ENV ELEMENTS_BIN "$ELEMENTS_HOME/bin"
ENV ELEMENTS_LIB "$ELEMENTS_HOME/lib"
ENV ELEMENTS_CONF "$ELEMENTS_HOME/conf"
ENV ELEMENTS_CDN_REPOS "$ELEMENTS_HOME/cdn-repos"
ENV ELEMENTS_SCRIPT_REPOS "$ELEMENTS_HOME/script-repos"
ENV ELEMENTS_SCRIPT_STORAGE "$ELEMENTS_HOME/script-storage"

# Specifies the classpath as ELEMENTS_HOME/classpath
ENV CLASSPATH "$ELEMENTS_LIB/*"

USER root:root

RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

RUN mkdir -p "$ELEMENTS_BIN" && \
    mkdir -p "$ELEMENTS_LIB" && \
    mkdir -p "$ELEMENTS_CONF" && \
    mkdir -p "$ELEMENTS_CDN_REPOS" && \
    mkdir -p "$ELEMENTS_SCRIPT_REPOS" && \
    mkdir -p "$ELEMENTS_SCRIPT_STORAGE" && \
    useradd elements && chown -R elements:elements "$ELEMENTS_HOME"

USER elements:elements

WORKDIR $ELEMENTS_HOME
