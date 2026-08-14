
.DEFAULT=internal
.PHONY=login,tag,push,clean

REV ?= $(shell git rev-parse --verify HEAD)
TAG ?= $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
TAG_SHORT ?= $(shell git rev-parse --short --verify HEAD)

BASE_REPO ?= elements-docker
DOCKER_HUB_BASE = elementalcomputing

# Every image is pushed to all of these registry/org prefixes in a single
# multi-arch buildx invocation (not one build per registry -- the multi-arch
# build itself is the expensive part, so it only happens once per image).
# FULL_REPO_BASE below intentionally still pulls the base image FROM
# DOCKER_HUB_BASE specifically -- that's just where the derived image's
# Dockerfile fetches its FROM layer, independent of how many registries the
# resulting images get pushed/tagged to.
REGISTRIES ?= elementalcomputing ghcr.io/namazustudios

MAVEN_OPTIONS?=

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push
DOCKER_BUILD=docker buildx build
DEPLOYMENT_REPO_URL ?=

MAVEN_COPY_DEPS=mvn -Pdocker $(MAVEN_OPTIONS) package

TARGET_ARCHITECTURES="linux/amd64,linux/arm64/v8"

# $(call tag_flags,<versions>,<image-name>) -> one --tag flag per
# (registry x version) pair, e.g. "--tag elementalcomputing/elements-base:3.9.0
# --tag ghcr.io/namazustudios/elements-base:3.9.0 ..."
tag_flags = $(foreach registry,$(REGISTRIES),$(foreach ver,$(1),--tag $(registry)/$(2):$(ver)))

define DOCKER_HUB_BUILD_TAG_BASE
	@echo "Building Base Image for $(1)"
	$(DOCKER_BUILD) \
		--pull \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		$(call tag_flags,$(REV) $(TAG) $(TAG_SHORT),$(1)) \
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
		$(call tag_flags,$(REV) $(TAG) $(TAG_SHORT),$(1)) \
		$(1)
endef

hub: deps
	$(call DOCKER_HUB_BUILD_TAG_BASE,elements-base)
	$(call DOCKER_HUB_BUILD_TAG_DERIVED,elements-jetty-ws)

internal: deps
	$(call DOCKER_HUB_BUILD_TAG_BASE,elements-base)
	$(call DOCKER_HUB_BUILD_TAG_DERIVED,elements-jetty-ws)

deps:
	$(MAVEN_COPY_DEPS)

clean:
	mvn clean
