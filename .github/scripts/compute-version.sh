#!/usr/bin/env bash
# Computes the next version string for a release-branch transition.
# Each mode validates its own precondition and fails loudly (non-zero exit,
# message on stderr) rather than silently producing a nonsensical version --
# this is the actual enforcement for "cut RC only from a SNAPSHOT" and
# "release only from a release candidate".
set -euo pipefail

usage() {
  echo "Usage: $0 <cut-rc|bump-rc|release|next-rc> <current-version>" >&2
  exit 2
}

fail() {
  echo "error: $1" >&2
  exit 1
}

if [[ $# -ne 2 ]]; then
  usage
fi

mode="$1"
current="$2"

case "$mode" in
  cut-rc)
    # X.Y.Z-SNAPSHOT -> X.Y.Z-rc-1
    if [[ "$current" == *-rc-* ]]; then
      fail "cannot cut-rc: '$current' already has an rc suffix"
    fi
    if [[ "$current" != *-SNAPSHOT ]]; then
      fail "cannot cut-rc: '$current' is not a -SNAPSHOT version"
    fi
    echo "${current%-SNAPSHOT}-rc-1"
    ;;

  bump-rc)
    # X.Y.Z-rc-N -> X.Y.Z-rc-(N+1)
    if [[ "$current" =~ ^(.+)-rc-([0-9]+)$ ]]; then
      echo "${BASH_REMATCH[1]}-rc-$(( BASH_REMATCH[2] + 1 ))"
    else
      fail "cannot bump-rc: '$current' does not match <base>-rc-<N>"
    fi
    ;;

  release)
    # X.Y.Z-rc-N -> X.Y.Z
    if [[ "$current" =~ ^(.+)-rc-([0-9]+)$ ]]; then
      echo "${BASH_REMATCH[1]}"
    else
      fail "cannot release: '$current' is not a release candidate (expected <base>-rc-<N>)"
    fi
    ;;

  next-rc)
    # X.Y.Z -> X.Y.(Z+1)-rc-1 -- only valid straight after a release, from a bare version
    if [[ "$current" == *-SNAPSHOT || "$current" == *-rc-* ]]; then
      fail "cannot next-rc: '$current' is not a bare release version"
    fi
    if [[ "$current" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
      echo "${BASH_REMATCH[1]}.${BASH_REMATCH[2]}.$(( BASH_REMATCH[3] + 1 ))-rc-1"
    else
      fail "cannot next-rc: '$current' is not a plain X.Y.Z version"
    fi
    ;;

  *)
    usage
    ;;
esac
