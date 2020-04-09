
.DEFAULT=push
.PHONY=login,tag,push

TAG ?= $(shell git rev-parse --verify HEAD)
TAG_SHORT ?= $(shell git rev-parse --short --verify HEAD)

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push
DOCKER_BUILD=docker build
DOCKER_LOGIN=docker login

MAVEN_PROJECT_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

REST_WAR=target/lib/rest-war-$(MAVEN_PROJECT_VERSION).war
APP_NODE_JAR=target/lib/app-node-$(MAVEN_PROJECT_VERSION)-jar-with-dependencies.jar
APP_SERVE_JAR=target/lib/app-serve-$(MAVEN_PROJECT_VERSION)-jar-with-dependencies.jar
CODE_SERVE_WAR=target/lib/code-serve-war-$(MAVEN_PROJECT_VERSION).war
SETUP_JAR=target/lib/setup-$(MAVEN_PROJECT_VERSION)-jar-with-dependencies.jar
WEB_UI_DIR=target/lib/dist

push:
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-api
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-node
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-code-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-ui

revision: login
	$(DOCKER_BUILD) namazu-socialengine-api \
		--build-arg REST_WAR=$(REST_WAR) \
		--tag namazu-socialengine-api:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-api:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-api:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-app-node \
		--build-arg APP_NODE_JAR=$(APP_NODE_JAR) \
		--tag namazu-socialengine-app-node:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-app-node:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-app-node:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-app-serve \
		--build-arg APP_SERVE_JAR=$(APP_SERVE_JAR) \
		--tag namazu-socialengine-app-serve:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-app-serve:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-app-serve:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-code-serve \
		--build-arg CODE_SERVE_WAR=$(CODE_SERVE_WAR) \
		--tag namazu-socialengine-code-serve:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-code-serve:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-code-serve:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-setup \
		--build-arg SETUP_JAR=$(SETUP_JAR) \
		--tag namazu-socialengine-setup:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-setup:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-setup:$(TAG_SHORT)
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--build-arg WEB_UI_DIR=$(WEB_UI_DIR) \
		--tag namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG_SHORT)

latest: login
	$(DOCKER_BUILD) namazu-socialengine-api \
		--build-arg REST_WAR=$(REST_WAR) \
		--tag docker.namazustudios.net/namazu-socialengine-api:latest
	$(DOCKER_BUILD) namazu-socialengine-app-node \
		--build-arg APP_NODE_JAR=$(APP_NODE_JAR) \
		--tag docker.namazustudios.net/namazu-socialengine-app-node:latest
	$(DOCKER_BUILD) namazu-socialengine-app-serve \
		--build-arg APP_SERVE_JAR=$(APP_SERVE_JAR) \
		--tag docker.namazustudios.net/namazu-socialengine-app-serve:latest
	$(DOCKER_BUILD) namazu-socialengine-code-serve \
		--build-arg CODE_SERVE_WAR=$(CODE_SERVE_WAR) \
		--tag docker.namazustudios.net/namazu-socialengine-code-serve:latest
	$(DOCKER_BUILD) namazu-socialengine-setup \
		--build-arg SETUP_JAR=$(SETUP_JAR) \
		--tag docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--build-arg WEB_UI_DIR=$(WEB_UI_DIR) \
		--tag docker.namazustudios.net/namazu-socialengine-ui

login:
	- echo $(DOCKER_PASS) | $(DOCKER_LOGIN) --username $(DOCKER_USER) --password-stdin docker.namazustudios.net
