#!/usr/bin/env bash

# Sets up docker by logging in and ensuring the multi-arch builds are working.

docker buildx create --use
echo $REGISTRY_PASS | docker login --username $REGISTRY_USER --password-stdin distribution.getelements.dev
git submodule update --init --recursive

# The build is in two Maven passes. The first is to do the base build, which builds the Doclet. Once built,
# the Doclet will be installed. The subsequent builds ensure each of the individual javadoc modules get built.
# The reason we do this is because the doclet will crash on some parts of the code and needs more testing. At
# this point it may not be worth it to fix if we are going to move more towards a different scripting system.

# Main Build
mvn --no-transfer-progress -B -DskipTests install

# Second Build Phase. Skips tests as well as activates only projects.

mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects common install
mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects common-util install
mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects dao install
mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects service install
mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects rt-server-lua install

# Makes the Docker images, tags, and pushes to the distribution server.
make -C docker-config
