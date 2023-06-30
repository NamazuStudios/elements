
define git
	@echo git submodule foreach git $(1)
	@echo git $(1)
endef

help:
	echo "Manages Semantic Versioning for Elements using Maven and Git."
	echo "patch - Increments the revision (least significant) number."
	echo "commit - Commits all changes, including submodules with a message indicating release."
	echo "push - Pushes all changes, including submodules to the remotes."
	echo "tag - Tags the current Maven version in git."

patch:
	mvn versions:set -DprocessAllModules=true -DnextSnapshot=true

release:
	mvn versions:set -DprocessAllModules=true -DremoveSnapshot=true

tag: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
tag:
	$(call git, tag $(MAVEN_VERSION))

commit: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
commit:
	$(call git, commit -a -m "\"Release $(MAVEN_VERSION)\"")

push:
	$(call git, push)
	$(call git, push --tags)

rollback:
	- find . -name "pom.xml" -exec git checkout {} \;
	- git submodule foreach git checkout .
