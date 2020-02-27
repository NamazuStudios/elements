
.DEFAULT=push
.PHONY=login,tag,push

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push
DOCKER_LOGIN=docker login

BRANCH_TAG=$(echo $(GIT_LOCAL_BRANCH) | tr -cd '[:alnum:]._-')


push: tag
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-api
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-node
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-app-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-code-serve
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-ui

tag: login
	$(DOCKER_TAG) namazu-socialengine-api:latest docker.namazustudios.net/namazu-socialengine-api
	$(DOCKER_TAG) namazu-socialengine-app-node:latest docker.namazustudios.net/namazu-socialengine-app-node
	$(DOCKER_TAG) namazu-socialengine-app-serve:latest docker.namazustudios.net/namazu-socialengine-app-serve
	$(DOCKER_TAG) namazu-socialengine-code-serve:latest docker.namazustudios.net/namazu-socialengine-code-serve
	$(DOCKER_TAG) namazu-socialengine-setup:latest docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_TAG) namazu-socialengine-ui:latest docker.namazustudios.net/namazu-socialengine-ui
	$(DOCKER_TAG) namazu-socialengine-api:$(BRANCH_TAG) docker.namazustudios.net/namazu-socialengine-api
	$(DOCKER_TAG) namazu-socialengine-app-node:$(BRANCH_TAG) docker.namazustudios.net/namazu-socialengine-app-node
	$(DOCKER_TAG) namazu-socialengine-app-serve:$(BRANCH_TAG) docker.namazustudios.net/namazu-socialengine-app-serve
	$(DOCKER_TAG) namazu-socialengine-code-serve:$(BRANCH_TAG) docker.namazustudios.net/namazu-socialengine-code-serve
	$(DOCKER_TAG) namazu-socialengine-setup:$(BRANCH_TAG) docker.namazustudios.net/namazu-socialengine-setup
	$(DOCKER_TAG) namazu-socialengine-ui:$(BRANCH_TAG) docker.namazustudios.net/namazu-socialengine-ui

login:
	echo $(DOCKER_PASS) | $(DOCKER_LOGIN) --username $(DOCKER_USER) --password-stdin docker.namazustudios.net
