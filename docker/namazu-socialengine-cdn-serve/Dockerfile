FROM openjdk:8

ARG CDN_SERVE_JAR

USER root:root
RUN useradd socialengine
RUN mkdir -p "/opt/socialengine/cdn-repos"
RUN chown -R socialengine:socialengine /opt/socialengine

COPY $CDN_SERVE_JAR /opt/socialengine/cdn-serve.jar
USER socialengine:socialengine
