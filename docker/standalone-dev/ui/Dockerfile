FROM namazu-socialengine-ui:latest

COPY socialengine.properties /opt/socialengine/socialengine.properties

ENV JAVA_OPTIONS "-Dcom.namazustudios.socialengine.configuration.properties=/opt/socialengine/socialengine.properties"
ENTRYPOINT ["/docker-entrypoint.sh"]
