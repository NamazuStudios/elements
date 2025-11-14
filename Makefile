
.PHONY=help,patch,release,tag,commit,push,git,rollback,checkout,setup

GIT_USER?="Continuous Integration"
GIT_EMAIL?="ci@getelements.dev"

help:
	@echo "Manages Semantic Versioning for Elements using Maven and Git."
	@echo "patch - Increments the revision (least significant) number (Snapshot Builds Only)"
	@echo "release - Drops -SNAPSHOT from the current revision number (Snapshot Builds Only)"
	@echo "commit - Commits all changes, including submodules with a message indicating release."
	@echo "git - Configures git with email and name. Run this before all other git commands."
	@echo "setup - Performs all pre-build setup and checks for all necessary build commands."
	@echo "push - Pushes all changes, including submodules to the remotes."
	@echo "tag - Tags the current Maven version in git."
	@echo "checkout - Checks out the specified tag/revision/branch for the project as well as submodules."

clean:
	mvn --no-transfer-progress -B clean

build: clean
	mvn --no-transfer-progress -B -Pgithub-publish install

deploy: clean
	mvn --no-transfer-progress -B -Pcentral-publish deploy

docker:
	make -C docker-config internal

docker_hub:
	make -C docker-config hub

patch:
	mvn versions:set -DprocessAllModules=true -DnextSnapshot=true

release:
	mvn versions:set -DprocessAllModules=true -DremoveSnapshot=true

version:

ifndef VERSION
	$(error VERSION is not set)
endif

	mvn versions:set -DprocessAllModules=true -DnewVersion=$(VERSION)

tag: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
tag:
	@echo "Tagging Release"
	git tag $(MAVEN_VERSION)

git:
	git config --global user.name $(GIT_USER)
	git config --global user.email $(GIT_EMAIL)
	git remote add github git@github.com:NamazuStudios/elements.git

setup: git
	export DOCKER_HOST=tcp://docker:2375
	export DOCKER_BUILDKIT=1
	export DOCKER_CLI_EXPERIMENTAL=enabled
	ng
	mvn -version
	docker buildx create --use
	echo $(DOCKER_HUB_ACCESS_TOKEN) | docker login --username $(DOCKER_HUB_USER) --password-stdin

setup_release: setup
	- mkdir "$(HOME)/.m2"
	cp -f settings.xml "$(HOME)/.m2"
	@echo "GPG Private Key Is"
	@echo $$GPG_PRIVATE_KEY | base64 -d | head -n 1
	@echo "redacted"
	@echo $$GPG_PRIVATE_KEY | base64 -d | tail -n 1
	@echo $$GPG_PRIVATE_KEY | base64 -d | gpg --batch --import

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

javadoc: JAVADOC_VERSION?=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
javadoc:
	mvn javadoc:aggregate
	aws --delete s3 sync target/site/apidocs s3://$(JAVADOC_S3_BUCKET)/$(JAVADOC_VERSION)

rollback:
	- find . -name "pom.xml" -exec git checkout {} \;
