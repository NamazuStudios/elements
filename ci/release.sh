#!/usr/bin/env bash

mvn_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [[ ${mvn_version} == *-SNAPSHOT ]]
then
  echo "Processing release build for version ${mvn_version}."
  make release tag commit push || exit $?
else
  echo "Skipping release build. Not snapshot ${mvn_version}."
fi
