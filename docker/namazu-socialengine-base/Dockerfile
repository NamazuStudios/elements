FROM openjdk:11.0-jdk

ENV DOCKERIZE_VERSION v0.6.1

ENV ELEMENTS_HOME "/opt/elements"
ENV ENTRYPOINT_JAR "$ELEMENTS_HOME/bin/main.jar"

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

ENTRYPOINT ["java", "-jar", "$ENTRYPOINT_JAR"]
