ARG SETUP_JAR

FROM namazu-socialengine-base:latest
COPY --chown=elements:elements $SETUP_JAR $ENTRYPOINT_JAR

# When running as command, this drops you into a shell to service the isntance

CMD ["java", "-jar", "$ENTRYPOINT_JAR", "shell"]
