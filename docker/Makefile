
.DEFAULT=revision
.PHONY=login,tag,push,clean

TAG ?= $(shell git rev-parse --verify HEAD)
TAG_SHORT ?= $(shell git rev-parse --short --verify HEAD)
TAG_MAVEN_VERSION = $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)

MAVEN_OPTIONS?=

DOCKER_TAG=docker tag
DOCKER_PUSH=docker push 
DOCKER_BUILD=docker buildx build
DOCKER_LOGIN=docker login

MAVEN_COPY_DEPS=mvn $(MAVEN_OPTIONS) clean package
MAVEN_PROJECT_VERSION:=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout 2>/dev/null)

TARGET_ARCHITECTURES="linux/amd64"

UI_VERSION_JSON=namazu-socialengine-ui/target/lib/dist/assets/version.json

define DOCKER_BUILD_TAG
	@echo "Building Base Image for $(1) -> $(2)"
	$(DOCKER_BUILD) $(1) \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(TAG_SHORT) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(MAVEN_PROJECT_VERSION) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(TAG_SHORT) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(MAVEN_PROJECT_VERSION)
endef

define DOCKER_BUILD_TAG_JAVA
	@echo "Building Java Image for  $(1) -> $(2)"
	$(DOCKER_BUILD) $(1) \
		--push \
		--platform $(TARGET_ARCHITECTURES) \
		--build-arg BASE_TAG=$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(TAG_SHORT) \
		--tag docker.namazustudios.net/namazu-elements-$(2):$(MAVEN_PROJECT_VERSION) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(TAG) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(TAG_SHORT) \
		--tag docker.namazustudios.net/namazu-socialengine-$(2):$(MAVEN_PROJECT_VERSION)
endef

define DOCKER_PUSH_ALL
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-elements-$(1):$(TAG)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-elements-$(1):$(TAG_SHORT)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-elements-$(1):$(MAVEN_PROJECT_VERSION)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-$(1):$(TAG)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-$(1):$(TAG_SHORT)
	$(DOCKER_PUSH) docker.namazustudios.net/namazu-socialengine-$(1):$(MAVEN_PROJECT_VERSION)
endef

revision: login deps
	$(call DOCKER_BUILD_TAG,namazu-socialengine-ui,ui)
	$(call DOCKER_BUILD_TAG,namazu-socialengine-base,base)
	$(call DOCKER_BUILD_TAG_JAVA,namazu-socialengine-api,api)
	$(call DOCKER_BUILD_TAG_JAVA,namazu-socialengine-app-node,app-node)
	$(call DOCKER_BUILD_TAG_JAVA,namazu-socialengine-app-serve,app-serve)
	$(call DOCKER_BUILD_TAG_JAVA,namazu-socialengine-code-serve,code-serve)
	$(call DOCKER_BUILD_TAG_JAVA,namazu-socialengine-setup,setup)
	$(call DOCKER_BUILD_TAG_JAVA,namazu-socialengine-cdn-serve,cdn-serve)

deps:
	$(MAVEN_COPY_DEPS)
	- rm -f $(UI_VERSION_JSON)
	@echo '{' >> $(UI_VERSION_JSON)
	@echo '  version:"$(TAG_MAVEN_VERSION)",' >> $(UI_VERSION_JSON)
	@echo '  revision:"$(shell git rev-parse --verify HEAD)",' >> $(UI_VERSION_JSON)
	@echo '  timestamp:"$(shell date "+%m-%d-%Y %T")"' >> $(UI_VERSION_JSON)
	@echo '}' >> $(UI_VERSION_JSON)

clean:
	mvn clean

login:
	- echo $(DOCKER_PASS) | $(DOCKER_LOGIN) --username $(DOCKER_USER) --password-stdin docker.namazustudios.net
