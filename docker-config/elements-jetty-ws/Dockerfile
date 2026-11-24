
ARG BASE_TAG
ARG FULL_REPO_BASE
FROM $FULL_REPO_BASE/elements-base:$BASE_TAG

COPY --chown=elements:elements target/lib/*.jar $ELEMENTS_LIB/
EXPOSE 8080

# -XX:-UseBASE64Intrinsics mitigates Java4Shell (CVE-2026-114938) at the JVM level,
# set here as well as in elements-base so a deployment overriding JAVA_TOOL_OPTIONS
# cannot silently remove it. See https://github.com/NamazuStudios/elements/issues/246
USER elements:elements
ENTRYPOINT ["java", "-XX:-UseBASE64Intrinsics", "jettyws"]
