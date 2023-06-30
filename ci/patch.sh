#!/usr/bin/env bash

maven_version=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [[ "${maven_version}" == "*SNAPSHOT" ]];
then
  echo "Skipping Build. Not snapshot."
  make patch tag commit push || exit $?
else
  echo "Skipping Build. Not snapshot."
fi
