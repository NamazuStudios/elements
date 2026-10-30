#!/usr/bin/env bash
# Maps a Maven revision (the git tag) to an OS-package version + release.
#
#   3.9.0           -> 3.9.0            / 1
#   3.9.0-rc-1      -> 3.9.0~rc1        / 1   ('~' sorts BEFORE the release
#   x.y.z-SNAPSHOT  -> x.y.z~SNAPSHOT   / 1    on both apt and dnf)
#
# Prints VERSION and RELEASE, one per line, for `read version release < <(...)`.

set -euo pipefail

revision="${1:?usage: version.sh <maven-revision>}"
release="${2:-1}"

case "$revision" in
  [0-9]*.[0-9]*.[0-9]*-rc-[0-9]*)
    version="${revision%-rc-[0-9]*}~rc${revision##*-rc-}"
    ;;
  [0-9]*.[0-9]*.[0-9]*-SNAPSHOT)
    version="${revision%-SNAPSHOT}~SNAPSHOT"
    ;;
  [0-9]*.[0-9]*.[0-9]*)
    version="$revision"
    ;;
  *)
    echo "version.sh: unrecognized revision format: $revision" >&2
    echo "version.sh: expected x.y.z, x.y.z-rc-N, or x.y.z-SNAPSHOT" >&2
    exit 1
    ;;
esac

printf '%s\n%s\n' "$version" "$release"
