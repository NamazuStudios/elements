FROM openjdk:8

ARG SETUP_JAR

RUN useradd elements &&
    mkdir -p "/opt/elements/bin" &&
    mkdir -p "/opt/elements/conf" &&
    mkdir -p "/opt/elements/script-repos" &&
    mkdir -p "/opt/elements/script-storage" &&
    chown -R elements:elements "/opt/elements"

USER elements:elements

RUN mkdir -p "/opt/socialengine"
COPY $SETUP_JAR /opt/elements/bin/main.jar
