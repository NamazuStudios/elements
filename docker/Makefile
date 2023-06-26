
.DEFAULT=revision
.PHONY=login,tag,push,clean

REV ?= $(shell git rev-parse --verify HEAD)
TAG ?= $(shell git rev-parse --verify HEAD)
TAG_SHORT ?= $(shell git rev-parse --short --verify HEAD)
TAG_MAVEN_VERSION = $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

MAVEN_OPTIONS?=

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push 
DOCKER_BUILD=docker buildx build
DOCKER_LOGIN=docker login

MAVEN_COPY_DEPS=mvn -Pdocker $(MAVEN_OPTIONS) package
MAVEN_PROJECT_VERSION:=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)

TARGET_ARCHITECTURES="linux/amd64,linux/arm64"

define DOCKER_BUILD_TAG
	@echo "Building Base Image for $(1)"
	$(DOCKER_BUILD) $(1) \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--tag distribution.getelements.dev/$(1):$(REV) \
		--tag distribution.getelements.dev/$(1):$(TAG) \
		--tag distribution.getelements.dev/$(1):$(TAG_SHORT) \
		--tag distribution.getelements.dev/$(1):$(MAVEN_PROJECT_VERSION)
endef

define DOCKER_BUILD_TAG_JAVA
	@echo "Building Java Image for $(1)"
	$(DOCKER_BUILD) $(1) \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--build-arg BASE_TAG=$(TAG) \
		--tag distribution.getelements.dev/$(1):$(REV) \
		--tag distribution.getelements.dev/$(1):$(TAG) \
		--tag distribution.getelements.dev/$(1):$(TAG_SHORT) \
		--tag distribution.getelements.dev/$(1):$(MAVEN_PROJECT_VERSION)
endef

revision: deps
	$(call DOCKER_BUILD_TAG,elements-base)
	$(call DOCKER_BUILD_TAG,elements-app-node)
	$(call DOCKER_BUILD_TAG,elements-jetty-ws)
	$(call DOCKER_BUILD_TAG,elements-setup)

deps:
	$(MAVEN_COPY_DEPS)

clean:
	mvn clean
