#!/usr/bin/env bash

git submodule update --init --recursive
git submodule foreach git checkout master
git submodule foreach git remote --verbose


comment=$(git log -1 --pretty=%B)
mvn_version=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

if [[ "${mvn_version}" != *"-SNAPSHOT" ]]
then
  echo "Skipping patch build. Not snapshot ${mvn_version}"
elif [[ "${comment}" == *"[notag]"* ]]
then
  echo "Skipping patch build. Contains [notag] ${mvn_version}"
else
  echo "Processing patch build for version ${mvn_version}"
  make git
  make tag || exit $?
  make patch || exit $?
  make commit push || exit $?
fi
