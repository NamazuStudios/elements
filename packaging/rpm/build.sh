#!/usr/bin/env bash
# Builds the .noarch.rpm from the staged tree.
#
# Prefers to build inside an amazonlinux:2023 container so the spec is
# exercised against the target rpm version and macro set (AL2023's
# systemd-rpm-macros). Falls back to a local rpmbuild when docker is not
# available - the scriptlets use the %{?macro:} guarded form so that build
# still succeeds where the systemd macros are not defined; the produced rpm
# is then best verified by installing it on the target distribution.

set -euo pipefail
cd "$(dirname "$0")/../.."

revision="${1:?usage: rpm/build.sh <maven-revision>}"
{ read -r version; read -r release; } < <(packaging/version.sh "$revision")

pkgroot="build/packaging/root"
dist="build/packaging/dist"
top="build/packaging/rpmbuild"

[ -d "$pkgroot/usr" ] || { echo "rpm/build.sh: staged tree missing - run 'make -C packaging stage' first" >&2; exit 1; }

rm -rf "$top"
install -d "$top/SPECS" "$top/SOURCES" "$top/BUILD" "$top/RPMS" "$top/SRPMS" "$dist"

sed -e "s/@@VERSION@@/${version}/" -e "s/@@RELEASE@@/${release}/" \
    packaging/rpm/namazu-elements.spec.in > "$top/SPECS/namazu-elements.spec"

# Source tarball whose single top-level directory `root/` is the staged tree.
tar -C build/packaging -czf "$top/SOURCES/namazu-elements-${version}.tar.gz" root

if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  echo "packaging: building the rpm inside amazonlinux:2023 (target rpm + macros)"
  docker run --rm -v "$PWD:/ws" -w /ws amazonlinux:2023 \
    bash -ec 'dnf install -y rpm-build && \
              rpmbuild -ba --define "_topdir /ws/build/packaging/rpmbuild" \
                       /ws/build/packaging/rpmbuild/SPECS/namazu-elements.spec'
else
  echo "packaging: docker unavailable - building with local rpmbuild" >&2
  command -v rpmbuild >/dev/null || { echo "rpm/build.sh: rpmbuild not found (apt-get install rpm)" >&2; exit 1; }
  rpmbuild -ba --define "_topdir $PWD/$top" "$top/SPECS/namazu-elements.spec"
fi

find "$top/RPMS" -name '*.rpm' -exec cp {} "$dist/" \;
echo "packaging: built $(echo "$dist"/*.rpm)"
