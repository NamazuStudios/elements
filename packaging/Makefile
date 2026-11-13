# OS packaging for Namazu Elements: one staged runtime tree, two thin packagers.
#
#   make stage   - build the staged tree under build/packaging/root (runs Maven)
#   make deb     - namazu-elements_<version>_all.deb                (dpkg-deb)
#   make rpm     - namazu-elements-<version>-<release>.noarch.rpm   (rpmbuild)
#
# VERSION defaults to the <revision> property of the root pom (the same value
# the release tag carries), so CI needs no explicit pass. The revision ->
# package version mapping lives in version.sh.
#
# The .deb and .rpm are deliberately built from the SAME staged tree and the
# SAME layout/ files - any layout change lands in both packages. The stamp
# keyed on VERSION makes `make package` stage once for both packagers.
#
# Paths: this Makefile runs with cwd = packaging/ (make -C packaging ...); the
# staged tree, stamps, and dist/ live at the repo root's build/packaging/.

ifeq ($(strip $(VERSION)),)
  VERSION := $(shell sed -n 's/.*<revision>\(.*\)<\/revision>.*/\1/p' ../pom.xml | head -n 1)
endif
ifeq ($(strip $(VERSION)),)
  $(error could not read <revision> from ../pom.xml; invoke as 'make -C packaging <target>' from the repo root, or pass VERSION=x.y.z)
endif

STAMP := ../build/packaging/.staged-$(VERSION)

$(STAMP): Makefile stage.sh version.sh $(wildcard layout/*)
	./stage.sh "$(VERSION)"
	touch "$@"

.PHONY: stage deb rpm clean

stage: $(STAMP)

deb: $(STAMP)
	deb/build.sh "$(VERSION)"

rpm: $(STAMP)
	rpm/build.sh "$(VERSION)"

clean:
	rm -rf ../build/packaging
