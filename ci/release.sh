#!/usr/bin/env bash

git submodule update --init --recursive

comment=$(git log -1 --pretty=%B)
mvn_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [[ "${mvn_version}" != *"-SNAPSHOT" ]]
then
  echo "Skipping release build. Not snapshot ${mvn_version}"
elif [[ "${comment}" == *"[notag]"* ]]
then
  echo "Skipping release build. Contains [notag] ${mvn_version}"
else
  echo "Processing release build for version ${mvn_version}"
  make release tag commit push || exit $?
fi
