
.PHONY=help,patch,release,tag,commit,push,git,rollback

GIT_USER?="Continuous Integration"
GIT_EMAIL?="ci@getelements.dev"

define git
	git submodule foreach git $(1)
	git $(1)
endef

help:
	echo "Manages Semantic Versioning for Elements using Maven and Git."
	echo "patch - Increments the revision (least significant) number (Snapshot Builds Only)"
	echo "release - Drops -SNAPSHOT from the current revision number (Snapshot Builds Only)"
	echo "commit - Commits all changes, including submodules with a message indicating release."
	echo "git - Configures git with email and name. Supplying defaults if environment variable aren't set."
	echo "push - Pushes all changes, including submodules to the remotes."
	echo "tag - Tags the current Maven version in git."

patch:
	mvn versions:set -DprocessAllModules=true -DnextSnapshot=true

release:
	mvn versions:set -DprocessAllModules=true -DremoveSnapshot=true

tag: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
tag:
	$(call git, tag $(MAVEN_VERSION))

git:
	git config --global user.name $(GIT_USER)
	git config --global user.email $(GIT_EMAIL)

commit: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
commit:
	$(call git, commit -a -m "\"[notag] CI Generated Release $(MAVEN_VERSION)\"")

push:
	$(call git, push)
	$(call git, push --tags)

rollback:
	- find . -name "pom.xml" -exec git checkout {} \;
	- git submodule foreach git checkout .
