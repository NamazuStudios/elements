FROM namazu-socialengine-app-serve:latest

ENV DOCKERIZE_VERSION v0.5.0

RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

COPY socialengine.properties /opt/socialengine/socialengine.properties

COPY socialengine.properties /opt/socialengine/socialengine.properties

ENV JAVA_OPTIONS "-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties"

ENTRYPOINT ["dockerize",\
 			"-wait", "tcp://redis:6379",\
 			"-wait", "tcp://mongo:27017",\
			"java", \
			"-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties", \
			"-jar", "/opt/socialengine/app-serve.jar" ]
