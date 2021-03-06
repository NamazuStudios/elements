FROM namazu-socialengine-setup:latest
COPY elements.properties $ELEMENTS_CONF/elements.properties

ENTRYPOINT [ \
    "dockerize", "-timeout", "1m", \
    "-wait", "tcp://redis:6379", \
    "-wait", "tcp://mongo:27017", \
    "java", "com.namazustudios.socialengine.setup.Setup", \
    "add-user", \
    "-email=root@namazustudios.net", \
    "-user=root", \
    "-password=root", \
    "-level=SUPERUSER" \
]
