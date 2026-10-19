# Local developer commands: versioning, git helpers, and basic builds.
# CI build/publish targets live in ci.mk (included below).

.PHONY: help clean patch release version tag commit push push_tags detach checkout rollback

help:
	@echo "Manages Semantic Versioning for Elements using Maven and Git."
	@echo "patch - Increments the revision (least significant) number (Snapshot Builds Only)"
	@echo "release - Drops -SNAPSHOT from the current revision number (Snapshot Builds Only)"
	@echo "version - Sets an explicit version (VERSION=x.y.z required)"
	@echo "commit - Commits all changes with a message indicating release (COMMIT_MSG=... required)"
	@echo "tag - Tags the current Maven version in git"
	@echo "push - Pushes all changes to the remote"
	@echo "push_tags - Pushes all tags to the remote"
	@echo "checkout - Checks out the specified tag/revision/branch (BRANCH=... required)"
	@echo "rollback - Restores all pom.xml files from git"
	@echo ""
	@echo "Build and publish targets (used by GitHub Actions) are defined in ci.mk."

clean:
	mvn --no-transfer-progress -B -q clean

# --- Versioning ---

# patch/release compute the next version the normal Maven way (letting versions:set's own
# nextSnapshot/removeSnapshot semver logic decide it), then discard the literal per-module rewrite
# that versions:set always produces and re-apply just the computed value to the single <revision>
# property via versions:set-property -- versions:set does not honor a property-based version even
# with -DprocessAllModules=true, it always writes a literal version into every module.
patch:
	@NEXT=$$(mvn -q versions:set -DnextSnapshot=true > /dev/null && mvn -q help:evaluate -Dexpression=project.version -DforceStdout) && \
	find . -path "*/target" -prune -o -name "pom.xml" -exec git checkout {} \; && \
	mvn versions:set-property -Dproperty=revision -DnewVersion=$$NEXT

release:
	@NEXT=$$(mvn -q versions:set -DremoveSnapshot=true > /dev/null && mvn -q help:evaluate -Dexpression=project.version -DforceStdout) && \
	find . -path "*/target" -prune -o -name "pom.xml" -exec git checkout {} \; && \
	mvn versions:set-property -Dproperty=revision -DnewVersion=$$NEXT

version:

ifndef VERSION
	$(error VERSION is not set)
endif

	mvn versions:set-property -Dproperty=revision -DnewVersion=$(VERSION)

tag: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
tag:
	@echo "Tagging Release"
	git tag $(MAVEN_VERSION)

commit: MAVEN_VERSION=$(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
commit:

ifndef COMMIT_MSG
	$(error COMMIT_MSG is not set)
endif

	git commit -a -m "\"$(COMMIT_MSG) $(MAVEN_VERSION)\""

# --- Git ---

push:
	git push

push_tags:
	git push --tags

detach:
	git checkout --detach

checkout:

ifndef BRANCH
	$(error BRANCH is not set)
endif

	git checkout $(BRANCH)

rollback:
	- find . -name "pom.xml" -exec git checkout {} \;

include ci.mk
