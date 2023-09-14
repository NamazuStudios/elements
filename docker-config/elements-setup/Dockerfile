
ARG BASE_TAG
FROM distribution.getelements.dev/elements-base:$BASE_TAG

USER root:root

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
COPY bin/sshd.sh $ELEMENTS_BIN
COPY bin/hostkeys.sh $ELEMENTS_BIN
COPY conf/sshd_config $ELEMENTS_CONF

RUN apt-get update && \
    apt-get install -y ssh && \
    touch "$ELEMENTS_CONF/sshd/authorized_keys" && \
    chmod u+x "$ELEMENTS_BIN/sshd.sh" && \
    chmod u+x "$ELEMENTS_BIN/hostkeys.sh" && \
    "$ELEMENTS_BIN/hostkeys.sh"

EXPOSE 2022

ENTRYPOINT ["bash", "-c", "$ELEMENTS_BIN/sshd.sh"]
