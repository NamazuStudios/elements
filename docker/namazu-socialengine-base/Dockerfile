
FROM ubuntu:20.04

RUN apt-get update && \
    apt-get -y install openjdk-11-jdk="11.0.11+9-0ubuntu2~20.04" && \
    apt-get -y install wget="1.20.3-1ubuntu1"

ENV DOCKERIZE_VERSION v0.6.1

# Home Directory
ENV ELEMENTS_HOME "/opt/elements"

# Sub Directories

# Application Directories. Used by the deployment scripts.
# - ELEMENTS_BIN holds any binaries the application may need. Added to the OS PATH
# - ELEMENTS_LIB holds any number of jar files to be included on the Java classpath
# - ELEMETNS_CONF houes any configuration fciles used by the service (Including elements.properties)
# - ELEMETNS_CLASSPATH a directory which is added to the Java classpath (eg Logging Configuration)


ENV ELEMENTS_BIN "$ELEMENTS_HOME/bin"
ENV ELEMENTS_LIB "$ELEMENTS_HOME/lib"
ENV ELEMENTS_TEMP "$ELEMENTS_HOME/tmp"
ENV ELEMENTS_CONF "$ELEMENTS_HOME/conf"
ENV ELEMENTS_CLASSPATH "$ELEMENTS_HOME/classpath"

# Variable/working directories which each service may or may not use
# These are pre-created for convienience and follow the standard
# naming convention for the application. Ideally these should be mounted
# to external storage where appropraite. Not all services use every directory
# - ELEMENTS_CDN_REPOS stores the CDN git repositotires as well as the CDN content
# - ELEMENTS_SCRIPT_REPOS stores the code-serve script repositorires
# - ELEMENTS_SCRIPT_STORAGE stores the state of the running scripts
ENV ELEMENTS_CDN_REPOS "$ELEMENTS_HOME/cdn-repos"
ENV ELEMENTS_SCRIPT_REPOS "$ELEMENTS_HOME/script-repos"
ENV ELEMENTS_SCRIPT_STORAGE "$ELEMENTS_HOME/script-storage"

# Specifies the classpath as ELEMENTS_HOME/classpath
ENV CLASSPATH "$ELEMENTS_CLASSPATH:$ELEMENTS_LIB/*"

USER root:root

RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

ENV PATH $ELEMENTS_BIN:$PATH

RUN mkdir -p "$ELEMENTS_BIN" && \
    mkdir -p "$ELEMENTS_LIB" && \
    mkdir -p "$ELEMENTS_TEMP" && \
    mkdir -p "$ELEMENTS_CONF" && \
    mkdir -p "$ELEMENTS_CLASSPATH" && \
    mkdir -p "$ELEMENTS_CDN_REPOS" && \
    mkdir -p "$ELEMENTS_SCRIPT_REPOS" && \
    mkdir -p "$ELEMENTS_SCRIPT_STORAGE" && \
    useradd elements -M --shell /usr/bin/bash --base-dir /opt --home "$ELEMENTS_HOME" && \
    chown -R elements:elements "$ELEMENTS_HOME" && \
    apt-get update && apt-get -y install dnsutils curl

WORKDIR $ELEMENTS_HOME
USER elements:elements

VOLUME ["${ELEMENTS_CDN_REPOS}", "${ELEMENTS_SCRIPT_REPOS}", "${ELEMENTS_SCRIPT_STORAGE}"]

COPY --chown=elements:elements src/main/resources/* $ELEMENTS_CLASSPATH/
