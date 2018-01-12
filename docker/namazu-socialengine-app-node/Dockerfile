FROM openjdk:8

ARG APP_NODE_JAR

USER root:root
RUN useradd socialengine
RUN mkdir -p "/opt/socialengine/script-repos"

COPY $APP_NODE_JAR /opt/socialengine/app-node.jar

RUN chown socialengine:socialengine /opt/socialengine

USER socialengine:socialengine
