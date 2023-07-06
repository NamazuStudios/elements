
.PHONY=help,patch,release,tag,commit,push,git,rollback,checkout

GIT_USER?="Continuous Integration"
GIT_EMAIL?="ci@getelements.dev"

help:
	@echo "Manages Semantic Versioning for Elements using Maven and Git."
	@echo "patch - Increments the revision (least significant) number (Snapshot Builds Only)"
	@echo "release - Drops -SNAPSHOT from the current revision number (Snapshot Builds Only)"
	@echo "commit - Commits all changes, including submodules with a message indicating release."
	@echo "git - Configures git with email and name. Run this before all other git commands."
	@echo "push - Pushes all changes, including submodules to the remotes."
	@echo "tag - Tags the current Maven version in git."
	@echo "checkout - Checks out the specified tag/revision/branch for the project as well as submodules."

build:

	# The build is in two Maven passes. The first is to do the base build, which builds the Doclet. Once built,
	# the Doclet will be installed. The subsequent builds ensure each of the individual javadoc modules get built.
	# The reason we do this iss because the doclet will crash on some parts of the code and needs more testing. At
	# this point it may not be worth it to fix if we are going to move more towards a different scripting system.
	# Main Build
	mvn --no-transfer-progress -B install

	# Second Build Phase. Skips tests as well as activates only projects.
	mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects common install
	mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects common-util install
	mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects dao install
	mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects service install
	mvn --no-transfer-progress -B -DskipTests --activate-profiles javadoc --projects rt-server-lua install

docker:
	docker buildx create --use
	make -C docker-config

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
	git diff --exit-code

commit: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
commit:
	git commit -a -m "\"[ci skip] $(MAVEN_VERSION)\""

push:
	git push
	git push --tags

rollback:
	- find . -name "pom.xml" -exec git checkout {} \;
	- git submodule foreach git checkout .
