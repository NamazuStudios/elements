#!/usr/bin/env bash

git submodule update --init --recursive

mvn_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [[ ${mvn_version} == *-SNAPSHOT ]]
then
  echo "Processing patch build for version ${mvn_version}."
  make patch tag commit push || exit $?
else
  echo "Skipping patch build. Not snapshot ${mvn_version}."
fi
