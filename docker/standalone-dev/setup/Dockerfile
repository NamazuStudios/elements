FROM namazu-socialengine-setup:latest

COPY socialengine.properties /opt/socialengine/socialengine.properties

ENTRYPOINT ["java", \
			"-Dorg.slf4j.simpleLogger.defaultLogLevel=debug", \
			"-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties", \
			"-jar", "/opt/socialengine/setup.jar", \
			"add-user", \
			"-email=root@namazustudios.net", \
			"-user=root", \
			"-password=root", \
			"-level=SUPERUSER"]
