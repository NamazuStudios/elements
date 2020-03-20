
.DEFAULT=push
.PHONY=login,tag,push

TAG ?= $(shell git rev-parse --short --verify HEAD)

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push
DOCKER_BUILD=docker build
DOCKER_LOGIN=docker login

push:
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-api
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-node
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-code-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-ui

revision: login
	$(DOCKER_BUILD) namazu-socialengine-api \
		--tag namazu-socialengine-api:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-api:$(TAG)
	$(DOCKER_BUILD) namazu-socialengine-app-node \
		--tag namazu-socialengine-app-node:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-app-node:$(TAG)
	$(DOCKER_BUILD) namazu-socialengine-app-serve \
		--tag namazu-socialengine-app-serve:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-app-serve:$(TAG)
	$(DOCKER_BUILD) namazu-socialengine-code-serve \
		--tag namazu-socialengine-code-serve:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-code-serve:$(TAG)
	$(DOCKER_BUILD) namazu-socialengine-setup \
		--tag namazu-socialengine-setup:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-setup:$(TAG)
	$(DOCKER_BUILD) namazu-socialengine-ui \
		--tag namazu-socialengine-ui:$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-ui:$(TAG)

latest: login
	$(DOCKER_BUILD) namazu-socialengine-api --tag docker.namazustudios.net/namazu-socialengine-api
	$(DOCKER_BUILD) namazu-socialengine-app-node --tag docker.namazustudios.net/namazu-socialengine-app-node
	$(DOCKER_BUILD) namazu-socialengine-app-serve --tag docker.namazustudios.net/namazu-socialengine-app-serve
	$(DOCKER_BUILD) namazu-socialengine-code-serve --tag docker.namazustudios.net/namazu-socialengine-code-serve
	$(DOCKER_BUILD) namazu-socialengine-setup --tag docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_BUILD) namazu-socialengine-ui --tag docker.namazustudios.net/namazu-socialengine-ui

login:
	- echo $(DOCKER_PASS) | $(DOCKER_LOGIN) --username $(DOCKER_USER) --password-stdin docker.namazustudios.net
