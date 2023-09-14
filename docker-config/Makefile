
.DEFAULT=revision
.PHONY=login,tag,push,clean

REV ?= $(shell git rev-parse --verify HEAD)
TAG ?= $(shell git describe --tags 2> /dev/null || git rev-parse --verify HEAD)
TAG_SHORT ?= $(shell git rev-parse --short --verify HEAD)

MAVEN_OPTIONS?=

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push 
DOCKER_BUILD=docker buildx build
DOCKER_LOGIN=docker login

MAVEN_COPY_DEPS=mvn -Pdocker $(MAVEN_OPTIONS) package

TARGET_ARCHITECTURES="linux/amd64,linux/arm64/v8"

define DOCKER_BUILD_TAG_BASE
	@echo "Building Base Image for $(1)"
	$(DOCKER_BUILD) \
		--pull \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--tag distribution.getelements.dev/$(1):$(REV) \
		--tag distribution.getelements.dev/$(1):$(TAG) \
		--tag distribution.getelements.dev/$(1):$(TAG_SHORT) \
		$(1)
endef

define DOCKER_BUILD_TAG_DERIVED
	@echo "Building Java Image for $(1)"
	$(DOCKER_BUILD) \
		--pull \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--build-arg BASE_TAG=$(TAG) \
		--tag distribution.getelements.dev/$(1):$(REV) \
		--tag distribution.getelements.dev/$(1):$(TAG) \
		--tag distribution.getelements.dev/$(1):$(TAG_SHORT) \
		$(1)
endef

revision: deps
	$(call DOCKER_BUILD_TAG_BASE,elements-base)
	$(call DOCKER_BUILD_TAG_DERIVED,elements-app-node)
	$(call DOCKER_BUILD_TAG_DERIVED,elements-jetty-ws)
	$(call DOCKER_BUILD_TAG_DERIVED,elements-setup)

deps:
	$(MAVEN_COPY_DEPS)

clean:
	mvn clean
