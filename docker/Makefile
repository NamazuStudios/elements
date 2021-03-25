
.DEFAULT=push
.PHONY=login,tag,push

TAG ?= $(shell git rev-parse --verify HEAD)
TAG_SHORT ?= $(shell git rev-parse --short --verify HEAD)

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push 
DOCKER_BUILD=docker build
DOCKER_LOGIN=docker login

MAVEN_PROJECT_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

WEB_UI_DIR=target/lib/dist
APPLICATION_LIBS=target/lib/*.jar

define DOCKER_BUILD_TAG
	$(DOCKER_BUILD) $(1) \
		--build-arg APPLICATION_LIBS=target/lib/*.jar \
		--tag namazu-socialengine-$(1):$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-$(1):$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-$(1):$(TAG_SHORT) \
		--tag namazu-elements-$(1):$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-$(1):$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-$(1):$(TAG_SHORT)
endef

define DOCKER_BUILD_LATEST
	$(DOCKER_BUILD) $(1) \
		--build-arg APPLICATION_LIBS=target/lib/*.jar \
		--tag $(1):latest \
		--tag docker.namazustudios.net/namazu-elements-$(1):latest \
		--tag docker.namazustudios.net/namazu-socialengine-$(1):latest
endef

define DOCKER_PUSH_ALL
	$(DOCKER_PUSH) namazu-elements-$(1)
	$(DOCKER_PUSH) namazu-elements-$(1):$(TAG)
	$(DOCKER_PUSH) namazu-elements-$(1):$(TAG_SHORT)
	$(DOCKER_PUSH) namazu-socialengine-$(1)
	$(DOCKER_PUSH) namazu-socialengine-$(1):$(TAG)
	$(DOCKER_PUSH) namazu-socialengine-$(1):$(TAG_SHORT)
endef

push:
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/base)
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/api)
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/app-node)
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/app-serve)
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/code-serve)
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/setup)
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/ui)
	$(call DOCKER_PUSH_ALL,docker.namazustudios.net/cdn-serve)

revision: login
	$(DOCKER_BUILD) namazu-socialengine-base \
		--tag namazu-elements-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-base:$(TAG_SHORT) \
		--tag namazu-socialengine-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-base:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--build-arg WEB_UI_DIR=$(WEB_UI_DIR) \
		--tag namazu-elements-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-ui:$(TAG_SHORT) \
		--tag namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG_SHORT)
	$(call DOCKER_BUILD_TAG,api)
	$(call DOCKER_BUILD_TAG,app-node)
	$(call DOCKER_BUILD_TAG,app-serve)
	$(call DOCKER_BUILD_TAG,code-serve)
	$(call DOCKER_BUILD_TAG,setup)
	$(call DOCKER_BUILD_TAG,cdn-serve)

latest: login
	$(DOCKER_BUILD) namazu-socialengine-base \
		--tag namazu-elements-base:latest \
		--tag docker.namazustudios.net/namazu-elements-base:latest \
		--tag namazu-socialengine-base:latest \
		--tag docker.namazustudios.net/namazu-socialengine-base:latest
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--build-arg WEB_UI_DIR=$(WEB_UI_DIR) \
		--tag namazu-elements-ui:latest \
		--tag docker.namazustudios.net/namazu-elements-ui:latest \
		--tag namazu-socialengine-ui:latest \
		--tag docker.namazustudios.net/namazu-socialengine-ui:latest
	$(call DOCKER_BUILD_LATEST,api)
	$(call DOCKER_BUILD_LATEST,app-node)
	$(call DOCKER_BUILD_LATEST,app-serve)
	$(call DOCKER_BUILD_LATEST,code-serve)
	$(call DOCKER_BUILD_LATEST,setup)
	$(call DOCKER_BUILD_LATEST,cdn-serve)

login:
	- echo $(DOCKER_PASS) | $(DOCKER_LOGIN) --username $(DOCKER_USER) --password-stdin docker.namazustudios.net
