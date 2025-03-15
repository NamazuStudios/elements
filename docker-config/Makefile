
.DEFAULT=internal
.PHONY=login,tag,push,clean

REV ?= $(shell git rev-parse --verify HEAD)
TAG ?= $(shell git describe --tags 2> /dev/null || git rev-parse --verify HEAD)
TAG_SHORT ?= $(shell git rev-parse --short --verify HEAD)

BASE_REPO ?= elements-docker
FULL_REPO_BASE = $(JF_HOST)/$(BASE_REPO)
DOCKER_HUB_BASE = elementalcomputing

MAVEN_OPTIONS?=

DOCKER_TAG=jf docker tag
DOCKER_PUSH=jf docker push
DOCKER_BUILD=jf docker buildx build
DEPLOYMENT_REPO_URL ?=

MAVEN_COPY_DEPS=mvn -Pdocker $(MAVEN_OPTIONS) package

TARGET_ARCHITECTURES="linux/amd64,linux/arm64/v8"

define DOCKER_HUB_BUILD_TAG_BASE
	@echo "Building Base Image for $(1)"
	$(DOCKER_BUILD) \
		--pull \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--tag $(DOCKER_HUB_BASE)/$(1):$(REV) \
		--tag $(DOCKER_HUB_BASE)/$(1):$(TAG) \
		--tag $(DOCKER_HUB_BASE)/$(1):$(TAG_SHORT) \
		$(1)
endef

define DOCKER_HUB_BUILD_TAG_DERIVED
	@echo "Building Java Image for $(1)"
	$(DOCKER_BUILD) \
		--pull \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--build-arg BASE_TAG=$(TAG) \
		--build-arg FULL_REPO_BASE=$(DOCKER_HUB_BASE) \
		--tag $(DOCKER_HUB_BASE)/$(1):$(REV) \
		--tag $(DOCKER_HUB_BASE)/$(1):$(TAG) \
		--tag $(DOCKER_HUB_BASE)/$(1):$(TAG_SHORT) \
		$(1)
endef

define DOCKER_JFROG_BUILD_TAG_BASE
	@echo "Building Base Image for $(1)"
	$(DOCKER_BUILD) \
		--pull \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--tag $(FULL_REPO_BASE)/$(1):$(REV) \
		--tag $(FULL_REPO_BASE)/$(1):$(TAG) \
		--tag $(FULL_REPO_BASE)/$(1):$(TAG_SHORT) \
		$(1)
endef

define DOCKER_JFROG_BUILD_TAG_DERIVED
	@echo "Building Java Image for $(1)"
	$(DOCKER_BUILD) \
		--pull \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--build-arg BASE_TAG=$(TAG) \
		--build-arg FULL_REPO_BASE=$(FULL_REPO_BASE) \
		--tag $(FULL_REPO_BASE)/$(1):$(REV) \
		--tag $(FULL_REPO_BASE)/$(1):$(TAG) \
		--tag $(FULL_REPO_BASE)/$(1):$(TAG_SHORT) \
		$(1)
endef

hub: deps
	$(call DOCKER_HUB_BUILD_TAG_BASE,elements-base)
	$(call DOCKER_HUB_BUILD_TAG_DERIVED,elements-jetty-ws)

internal: deps
	$(call DOCKER_JFROG_BUILD_TAG_BASE,elements-base)
	$(call DOCKER_JFROG_BUILD_TAG_DERIVED,elements-jetty-ws)

deps:
	$(MAVEN_COPY_DEPS)

clean:
	mvn clean
