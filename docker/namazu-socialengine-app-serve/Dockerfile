FROM openjdk:8

ARG APP_SERVE_JAR

USER root:root
RUN useradd socialengine
RUN mkdir -p "/opt/socialengine/script-repos"

COPY $APP_SERVE_JAR /opt/socialengine/app-serve.jar

RUN chown socialengine:socialengine /opt/socialengine

USER socialengine:socialengine
