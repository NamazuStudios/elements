FROM namazu-socialengine-setup:latest

RUN apt-get update && apt-get install -y wget

ENV DOCKERIZE_VERSION v0.5.0

RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

COPY socialengine.properties /opt/socialengine/socialengine.properties

ENTRYPOINT ["dockerize",\
 			"-wait", "tcp://mongo:27017",\
			"java", \
			"-Dorg.slf4j.simpleLogger.defaultLogLevel=debug", \
			"-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties", \
			"-jar", "/opt/socialengine/setup.jar", \
			"add-user", \
			"-email=root@namazustudios.net", \
			"-user=root", \
			"-password=root", \
			"-level=SUPERUSER"]
