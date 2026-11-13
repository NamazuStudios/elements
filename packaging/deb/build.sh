#!/usr/bin/env bash
# Assembles build/packaging/deb-root from the staged tree plus DEBIAN/, then
# builds the .deb with dpkg-deb --root-owner-group (no fakeroot needed).

set -euo pipefail
cd "$(dirname "$0")/../.."

revision="${1:?usage: deb/build.sh <maven-revision>}"
{ read -r version; read -r release; } < <(packaging/version.sh "$revision")

pkgroot="build/packaging/root"
debroot="build/packaging/deb-root"
dist="build/packaging/dist"

[ -d "$pkgroot/usr" ] || { echo "deb/build.sh: staged tree missing - run 'make -C packaging stage' first" >&2; exit 1; }
command -v dpkg-deb >/dev/null || { echo "deb/build.sh: dpkg-deb not found" >&2; exit 1; }

rm -rf "$debroot"
install -d "$dist"
cp -a "$pkgroot" "$debroot"
cp -a packaging/deb/DEBIAN "$debroot/DEBIAN"
cp packaging/deb/README.Debian "$debroot/usr/share/doc/namazu-elements/README.Debian"

# Debian changelog (lintian: no-changelog). Rendered per build so the entry
# always carries this build's version; gzip -n keeps it deterministic.
sed "s/@@VERSION@@/${version}-${release}/" packaging/deb/changelog.in \
  | gzip -9n > "$debroot/usr/share/doc/namazu-elements/changelog.Debian.gz"

# Directories can carry a setgid bit from the build filesystem (e.g. a shared
# workspace), which dpkg-deb rejects on the control directory and which has no
# business inside a package anyway.
chmod -R g-s "$debroot"
chmod 0755 "$debroot/DEBIAN"

sed "s/@@VERSION@@/${version}-${release}/" packaging/deb/DEBIAN/control.in > "$debroot/DEBIAN/control"
installed_size="$(du -sk --exclude=DEBIAN "$debroot" | cut -f1)"
printf 'Installed-Size: %s\n' "$installed_size" >> "$debroot/DEBIAN/control"
chmod 0755 "$debroot/DEBIAN/preinst" "$debroot/DEBIAN/postinst" "$debroot/DEBIAN/prerm" "$debroot/DEBIAN/postrm"

(
  cd "$debroot"
  find . -path ./DEBIAN -prune -o -type f -print0 | sort -z | xargs -0 md5sum > DEBIAN/md5sums
)

artifact="namazu-elements_${version}-${release}_all.deb"
dpkg-deb --root-owner-group --build "$debroot" "$dist/$artifact"
echo "packaging: built $dist/$artifact"
