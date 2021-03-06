
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
ELEMENTS_CLASSPATH=target/lib/*.jar

define DOCKER_BUILD_TAG
	$(DOCKER_BUILD) $(1) \
		--build-arg ELEMENTS_CLASSPATH=target/lib/*.jar \
		--tag $(1):$(TAG) \
		--tag docker.namazustudios.net/$(1):$(TAG) \
		--tag docker.namazustudios.net/$(1):$(TAG_SHORT)
endef

define DOCKER_BUILD_LATEST
	$(DOCKER_BUILD) $(1) \
		--build-arg ELEMENTS_CLASSPATH=target/lib/*.jar \
		--tag $(1):latest \
		--tag docker.namazustudios.net/$(1):latest
endef

push:
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-api
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-node
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-code-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-ui
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-cdn-serve

revision: login
	$(DOCKER_BUILD) namazu-socialengine-base \
		--tag namazu-socialengine-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-base:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-base:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--build-arg WEB_UI_DIR=$(WEB_UI_DIR) \
		--tag namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG_SHORT)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-api)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-app-node)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-app-serve)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-code-serve)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-setup)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-cdn-serve)

latest: login
	$(DOCKER_BUILD) namazu-socialengine-base \
		--tag namazu-socialengine-base:latest \
		--tag docker.namazustudios.net/namazu-socialengine-base:latest
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--build-arg WEB_UI_DIR=$(WEB_UI_DIR) \
		--tag namazu-socialengine-ui:latest \
		--tag docker.namazustudios.net/namazu-socialengine-ui:latest
	$(call DOCKER_BUILD_LATEST,namazu-socialengine-api)
	$(call DOCKER_BUILD_LATEST,namazu-socialengine-app-node)
	$(call DOCKER_BUILD_LATEST,namazu-socialengine-app-serve)
	$(call DOCKER_BUILD_LATEST,namazu-socialengine-code-serve)
	$(call DOCKER_BUILD_LATEST,namazu-socialengine-setup)
	$(call DOCKER_BUILD_LATEST,namazu-socialengine-cdn-serve)

login:
	- echo $(DOCKER_PASS) | $(DOCKER_LOGIN) --username $(DOCKER_USER) --password-stdin docker.namazustudios.net
