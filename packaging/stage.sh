#!/usr/bin/env bash
# Builds the shared runtime tree consumed by BOTH packagers (deb/ and rpm/).
#
# It mirrors exactly what the Docker images ship:
#   - docker-config/elements-jetty-ws with the `docker` profile stages
#     target/lib/*.jar (the jetty-ws and migrate jars plus their runtime
#     dependencies) - the same set the ws image COPYies into $ELEMENTS_LIB.
#   - docker-config/elements-base's resources are the packaged classpath -
#     the same files the base image COPYies into $ELEMENTS_CLASSPATH.
#
# The FHS mapping (vs. the image's /opt/elements ELEMENTS_HOME):
#   /usr/share/namazu-elements   ELEMENTS_HOME (lib/, classpath/)
#   /etc/elements                operator configuration
#   /var/lib/namazu-elements     ELEMENTS_TEMP, CDN_REPOS, SCRIPT_*, state
#   /var/log/namazu-elements     logs

set -euo pipefail
cd "$(dirname "$0")/.."

revision="${1:?usage: stage.sh <maven-revision>}"
pkgroot="build/packaging/root"

echo "packaging: staging the runtime tree for ${revision}"
# The javadoc/sources jar pass is skipped: the packages ship only the binary
# jars from target/lib, and that pass is most of the wall time (the repo's own
# install_no_tests target skips it the same way). The web UI build is NOT
# skipped - the packages must ship what the Docker images ship.
mvn --no-transfer-progress -B -q -Pdocker -pl docker-config/elements-jetty-ws -am -DskipTests \
    -Dmaven.javadoc.skip=true -Dmaven.source.skip=true package

jars=(docker-config/elements-jetty-ws/target/lib/*.jar)
if [ ! -e "${jars[0]}" ]; then
  echo "packaging: no jars staged under docker-config/elements-jetty-ws/target/lib" >&2
  echo "packaging: was the docker profile activated?" >&2
  exit 1
fi

rm -rf build/packaging
install -d -m 0755 \
  "$pkgroot/usr/bin" \
  "$pkgroot/usr/share/namazu-elements/lib" \
  "$pkgroot/usr/share/namazu-elements/classpath" \
  "$pkgroot/usr/share/doc/namazu-elements" \
  "$pkgroot/etc/elements/classpath" \
  "$pkgroot/etc/default" \
  "$pkgroot/etc/logrotate.d" \
  "$pkgroot/usr/lib/systemd/system" \
  "$pkgroot/usr/lib/sysusers.d" \
  "$pkgroot/usr/lib/tmpfiles.d"

cp "${jars[@]}" "$pkgroot/usr/share/namazu-elements/lib/"

cp docker-config/elements-base/src/main/resources/logback.xml \
   docker-config/elements-base/src/main/resources/logback-debug.xml \
   "$pkgroot/usr/share/namazu-elements/classpath/"

# Layout files are shared verbatim by the .deb and the .rpm.
install -m 0755 packaging/layout/namazu-elements.launcher \
        "$pkgroot/usr/bin/namazu-elements"
install -m 0644 packaging/layout/namazu-elements.service \
        "$pkgroot/usr/lib/systemd/system/namazu-elements.service"
install -m 0644 packaging/layout/namazu-elements.sysusers \
        "$pkgroot/usr/lib/sysusers.d/namazu-elements.conf"
install -m 0644 packaging/layout/namazu-elements.tmpfiles \
        "$pkgroot/usr/lib/tmpfiles.d/namazu-elements.conf"
install -m 0644 packaging/layout/namazu-elements.logrotate \
        "$pkgroot/etc/logrotate.d/namazu-elements"
install -m 0644 packaging/layout/namazu-elements.default \
        "$pkgroot/etc/default/namazu-elements"
install -m 0644 packaging/layout/elements.properties.example \
        "$pkgroot/usr/share/namazu-elements/elements.properties.example"

cp LICENSE.txt "$pkgroot/usr/share/doc/namazu-elements/copyright"

echo "packaging: staged tree ready under $pkgroot"
