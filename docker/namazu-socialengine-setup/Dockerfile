
ARG BASE_TAG
FROM docker.namazustudios.net/namazu-elements-base:$BASE_TAG

USER root:root

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
COPY bin/sshd.sh $ELEMENTS_BIN
COPY bin/hostkeys.sh $ELEMENTS_BIN
COPY conf/sshd_config $ELEMENTS_CONF

RUN echo exit 0 > /usr/sbin/policy-rc.d && \
    RUNLEVEL=1 apt-get install -f -y ssh python3-minimal- && \
    touch "$ELEMENTS_CONF/authorized_keys" && \
    chmod u+x "$ELEMENTS_BIN/sshd.sh" && \
    chmod u+x "$ELEMENTS_BIN/hostkeys.sh" && \
    "$ELEMENTS_BIN/hostkeys.sh"

EXPOSE 2022

ENTRYPOINT ["bash", "-c", "$ELEMENTS_BIN/sshd.sh"]
