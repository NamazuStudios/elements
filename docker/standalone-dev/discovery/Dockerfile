FROM alpine:3.13

ENV DOCKERIZE_VERSION v0.6.1

RUN apk add wget=1.21.1-r1 && apk add dnsmasq=2.84-r0

RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-linux-amd64-$DOCKERIZE_VERSION.tar.gz

EXPOSE 53/udp

ENTRYPOINT [ \
    "dockerize", "-timeout", "1m", \
    "-wait", "tcp://redis:6379", \
    "-wait", "tcp://mongo:27017", \
    "dnsmasq", "--keep-in-foreground", \
    "-W", "_elements._tcp.internal,app-node-0,28883,1", \
    "-W", "_elements._tcp.internal,app-node-1,28883,1"\
]
