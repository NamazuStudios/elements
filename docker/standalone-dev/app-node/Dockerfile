FROM namazu-socialengine-app-node:latest

ENV DOCKERIZE_VERSION v0.5.0

USER root:root
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

USER socialengine:socialengine

COPY socialengine.properties /opt/socialengine/socialengine.properties

ENV JAVA_OPTIONS "-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties"

ENTRYPOINT ["dockerize",\
 			"-wait", "tcp://redis:6379",\
 			"-wait", "tcp://mongo:27017",\
			"java", \
			"-Dlogback.configurationFile=logback-debug.xml", \
			"-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties", \
			"-jar", "/opt/socialengine/app-node.jar" ]
