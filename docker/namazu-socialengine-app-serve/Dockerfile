FROM openjdk:8-jre

ARG APP_SERVE_JAR

RUN mkdir -p "/opt/socialengine/script-repos"
RUN mkdir -p "/opt/socialengine/jar"

COPY $APP_SERVE_JAR /opt/socialengine/jar/app-serve.jar

ENTRYPOINT ["java", "-jar", "/opt/socialengine/jar/app-serve.jar"]
