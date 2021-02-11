FROM namazu-socialengine-code-serve:latest
COPY elements.properties $ELEMENTS_CONF/elements.properties

ENTRYPOINT [ \
    "dockerize", "-timeout", "1m", \
    "-wait", "tcp://redis:6379", \
    "-wait", "tcp://mongo:27017", \
    "java", "com.namazustudios.socialengine.codeserve.CodeServeMain" \
]
