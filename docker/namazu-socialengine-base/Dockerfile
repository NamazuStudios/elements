FROM openjdk:11.0-jdk

ENV DOCKERIZE_VERSION v0.6.1

# Home Directory
ENV ELEMENTS_HOME "/opt/elements"

# Sub Directories
ENV ELEMENTS_BIN "$ELEMENTS_HOME/bin"
ENV ELEMENTS_CONF "$ELEMENTS_HOME/conf"
ENV ELEMENTS_SCRIPT_REPOS "$ELEMENTS_HOME/script-repos"
ENV ELEMENTS_SCRIPT_STORAGE "$ELEMENTS_HOME/script-storage"

# Entrypoint Jar File
ENV ENTRYPOINT_JAR "$ELEMENTS_BIN/main.jar"

USER root:root

RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

RUN mkdir -p "$ELEMENTS_HOME/bin" && \
    mkdir -p "$ELEMENTS_HOME/conf" && \
    mkdir -p "$ELEMENTS_HOME/script-repos" && \
    mkdir -p "$ELEMENTS_HOME/script-storage" && \
    useradd elements && chown -R elements:elements "$ELEMENTS_HOME"

USER elements:elements

WORKDIR $ELEMENTS_HOME
ENTRYPOINT ["sh", "-c", "java -jar echo $ENTRYPOINT_JAR"]
