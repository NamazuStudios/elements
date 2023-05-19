#!/usr/bin/env bash

MAVEN_PROJECTS="common,common-util,dao,service,rt-server-lua"

JAVA_TMPDIR=${JAVA_TMPDIR:-"$(mktemp -d)"}
MAVEN_REPO_LOCAL=${MAVEN_REPO_LOCAL:-"$(mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout)"}

MAVEN_OPTIONS="-Pjavadoc -DskipTests -Djava.io.tmpdir=${JAVA_TMPDIR} -Dmaven.repo.local=${MAVEN_REPO_LOCAL}"

mvn "${MAVEN_OPTIONS}" --projects "${MAVEN_PROJECTS}" clean package install
