
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
		--tag namazu-elements-$(2) \
		--tag namazu-elements-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-$(2) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(TAG_SHORT) \
		--tag namazu-socialengine-$(2) \
		--tag namazu-socialengine-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(TAG_SHORT)
endef

define DOCKER_PUSH_ALL
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-elements-$(1)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-elements-$(1):$(TAG)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-elements-$(1):$(TAG_SHORT)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-$(1)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-$(1):$(TAG)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-$(1):$(TAG_SHORT)
endef

push:
	$(call DOCKER_PUSH_ALL,base)
	$(call DOCKER_PUSH_ALL,api)
	$(call DOCKER_PUSH_ALL,app-node)
	$(call DOCKER_PUSH_ALL,app-serve)
	$(call DOCKER_PUSH_ALL,code-serve)
	$(call DOCKER_PUSH_ALL,setup)
	$(call DOCKER_PUSH_ALL,ui)
	$(call DOCKER_PUSH_ALL,cdn-serve)

revision: login
	$(DOCKER_BUILD) namazu-socialengine-base \
		--tag namazu-elements-base \
		--tag namazu-elements-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-base:$(TAG_SHORT) \
		--tag namazu-socialengine-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-base:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--build-arg WEB_UI_DIR=$(WEB_UI_DIR) \
		--tag namazu-elements-ui \
		--tag namazu-elements-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-ui:$(TAG_SHORT) \
		--tag namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG_SHORT)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-api,api)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-app-node,app-node)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-app-serve,app-serve)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-code-serve,code-serve)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-setup,setup)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-cdn-serve,cdn-serve)

login:
	- echo $(DOCKER_PASS) | $(DOCKER_LOGIN) --username $(DOCKER_USER) --password-stdin docker.namazustudios.net
