FROM namazu-socialengine-base:latest

USER root:root

ARG APPLICATION_LIBS
COPY --chown=elements:elements $APPLICATION_LIBS $ELEMENTS_LIB/

COPY bin/sshd.sh $ELEMENTS_BIN
COPY bin/hostkeys.sh $ELEMENTS_BIN
COPY conf/sshd_config $ELEMENTS_CONF

RUN apt-get update && apt-get install -y openssh-server="1:8.2p1-4ubuntu0.2" && \
    touch "$ELEMENTS_CONF/authorized_keys" && \
    chmod u+x "$ELEMENTS_BIN/sshd.sh" && \
    chmod u+x "$ELEMENTS_BIN/hostkeys.sh" && "$ELEMENTS_BIN/hostkeys.sh"

EXPOSE 2022

ENTRYPOINT ["bash", "-c", "$ELEMENTS_BIN/sshd.sh"]
