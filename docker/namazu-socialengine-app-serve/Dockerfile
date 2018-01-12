FROM openjdk:8

ARG APP_SERVE_JAR

USER root:root
RUN mkdir -p "/opt/socialengine/script-repos"
RUN chown -R jetty:jetty /opt/socialengine/script-repos

USER jetty:jetty
COPY $APP_SERVE_JAR /opt/socialengine/app-serve.jar
