
ARG BASE_TAG
ARG FULL_REPO_BASE
FROM $FULL_REPO_BASE/elements-base:$BASE_TAG

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
EXPOSE 8080

USER elements:elements
ENTRYPOINT ["java", "jettyws"]
