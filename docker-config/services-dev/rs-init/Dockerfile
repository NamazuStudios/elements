
# Dockerize Image

FROM ubuntu:22.04 as dockerize

ARG TARGETARCH
ENV DOCKERIZE_VERSION=v0.7.0

RUN apt-get update && apt-get install -y wget
RUN wget -O - https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-linux-$TARGETARCH-$DOCKERIZE_VERSION.tar.gz | tar xzf - -C /usr/local/bin

# Mongo Image

FROM mongo:6.0.9

COPY --from=dockerize /usr/local/bin/dockerize /usr/local/bin/

CMD dockerize -wait tcp://mongo:27017 && \
    mongosh --host mongo --port 27017 \
    --eval 'rs.initiate({ "_id" : "local-test", members: [ {"_id" : 0, host:"mongo:27017"} ]})'
