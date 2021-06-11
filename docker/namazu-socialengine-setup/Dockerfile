FROM namazu-socialengine-base:latest

USER root:root

ARG APPLICATION_LIBS
COPY --chown=elements:elements $APPLICATION_LIBS $ELEMENTS_LIB/

COPY sshd.sh $ELEMENTS_BIN
COPY hostkeys.sh $ELEMENTS_BIN
COPY sshd_config $ELEMENTS_CONF

RUN apt-get install -y openssh-server="1:8.2p1-4ubuntu0.2" && \
    touch "$ELEMENTS_CONF/authorized_keys" && chmod 644 "$ELEMENTS_CONF/authorized_keys" && \
    chmod u+x "$ELEMENTS_BIN/sshd.sh" && \
    chmod u+x "$ELEMENTS_BIN/hostkeys.sh" && "$ELEMENTS_BIN/hostkeys.sh"

# TODO: Remove this. This should be generated per-deployment and not baked in to the container.
RUN ssh-keygen -f "$ELEMENTS_CONF/id" -N "" && cat "$ELEMENTS_CONF/id.pub" > "$ELEMENTS_CONF/authorized_keys"

EXPOSE 2022

ENTRYPOINT ["bash", "-c", "$ELEMENTS_BIN/sshd.sh"]
