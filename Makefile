
.PHONY=help,patch,release,tag,commit,push,git,rollback,checkout,jfrog,setup

GIT_USER?="Continuous Integration"
GIT_EMAIL?="ci@getelements.dev"

help:
	@echo "Manages Semantic Versioning for Elements using Maven and Git."
	@echo "patch - Increments the revision (least significant) number (Snapshot Builds Only)"
	@echo "release - Drops -SNAPSHOT from the current revision number (Snapshot Builds Only)"
	@echo "commit - Commits all changes, including submodules with a message indicating release."
	@echo "git - Configures git with email and name. Run this before all other git commands."
	@echo "jfrog - Configures jfrog by setting up a profile and other options."
	@echo "setup - Performs all pre-build setup and checks for all necessary build commands."
	@echo "push - Pushes all changes, including submodules to the remotes."
	@echo "tag - Tags the current Maven version in git."
	@echo "checkout - Checks out the specified tag/revision/branch for the project as well as submodules."

build:
	jf mvn --no-transfer-progress -B clean deploy

docker:
	make -C docker-config internal

docker_hub:
	make -C docker-config hub

patch:
	jf mvn versions:set -DprocessAllModules=true -DnextSnapshot=true

release:
	jf mvn versions:set -DprocessAllModules=true -DremoveSnapshot=true

version:

ifndef VERSION
	$(error VERSION is not set)
endif

	jf mvn versions:set -DprocessAllModules=true -DnewVersion=$(VERSION)

tag: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
tag:
	@echo "Tagging Release"
	git tag $(MAVEN_VERSION)

git:
	git config --global user.name $(GIT_USER)
	git config --global user.email $(GIT_EMAIL)
	git remote add github git@github.com:Elemental-Computing/elements.git

jfrog:

	jf config add getelements \
		--url=$(JF_URL) \
		--user=$(JF_USER) \
		--access-token=$(JF_ACCESS_TOKEN) \
		--interactive=false

	jf mvnc \
		--server-id-deploy getelements \
		--repo-deploy-releases elements-libs-release \
		--repo-deploy-snapshots elements-libs-snapshot \
		--repo-resolve-releases elements-libs-release \
		--repo-resolve-snapshots elements-libs-snapshot

setup: git jfrog
	ng
	docker buildx create --use
	echo $(JF_ACCESS_TOKEN) | docker login --username $(JF_USER) --password-stdin $(JF_URL)
	echo $(DOCKER_HUB_ACCESS_TOKEN) | docker login --username $(DOCKER_HUB_USER) --password-stdin

commit: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
commit:

ifndef COMMIT_MSG
	$(error COMMIT_MSG is not set)
endif

	git commit -a -m "\"$(COMMIT_MSG) $(MAVEN_VERSION)\""

push:
	git push

push_tags:
	git push --tags

publish_github: CURRENT_BRANCH=$(shell git rev-parse --abbrev-ref HEAD)
publish_github:
	git push github $(CURRENT_BRANCH)

publish_github_tags:
	git push github --tags

detach:
	git checkout --detach

checkout:

ifndef BRANCH
	$(error BRANCH is not set)
endif

	git checkout $(BRANCH)

rollback:
	- find . -name "pom.xml" -exec git checkout {} \;
