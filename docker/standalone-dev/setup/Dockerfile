FROM namazu-socialengine-setup:latest

COPY socialengine.properties /opt/socialengine/socialengine.properties

ENTRYPOINT ["java", \
			"-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties", \
			"-jar", "/opt/socialengine/setup.jar", \
			"add-user", \
			"-email=root@namazustudios.net", \
			"-user=root", \
			"-password=root", \
			"-level=SUPERUSER"]
