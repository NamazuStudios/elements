FROM openjdk:11.0-jdk

ARG APP_NODE_JAR

USER root:root

RUN useradd elements &&
    mkdir -p "/opt/elements/bin" &&
    mkdir -p "/opt/elements/conf" &&
    mkdir -p "/opt/elements/script-repos" &&
    mkdir -p "/opt/elements/script-storage" &&
    chown -R elements:elements "/opt/elements"

USER elements:elements

COPY --chown=elements:elements $APP_NODE_JAR /opt/elements/bin/main.jar
