FROM openjdk:8

ARG APP_SERVE_JAR

USER root:root
RUN useradd socialengine
RUN mkdir -p "/opt/socialengine/script-repos"
RUN chown -R socialengine:socialengine /opt/socialengine

COPY $APP_SERVE_JAR /opt/socialengine/app-serve.jar
USER socialengine:socialengine
