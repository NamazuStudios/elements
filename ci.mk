# CI build and publish targets, called by the GitHub Actions workflows in
# .github/workflows/ (also usable locally). Target suffixes list every Maven
# publish profile activated, so a bare target name activates none.
# Versioning and git helpers live in the top-level Makefile.

.PHONY: build install_no_tests deploy_central deploy_central_github deploy_maven_s3 docker docker_hub package_deb package_rpm package

build:
	mvn --no-transfer-progress -B -q install

install_no_tests:
	mvn -Darchetype.test.skip=true -DskipTests -Dmaven.javadoc.skip=true -Dskip.web-ui-build=true --no-transfer-progress -q install

deploy_central:
	mvn --no-transfer-progress -B -q -Pcentral-publish deploy

deploy_central_github:
	mvn --no-transfer-progress -B -q -Pcentral-publish,github-publish deploy

deploy_maven_s3:
	mvn --no-transfer-progress -B -q -Pgpg-sign deploy -DaltDeploymentRepository=staging::default::file:./staging

docker:
	make -C docker-config internal

docker_hub:
	make -C docker-config hub

# OS packages: one staged runtime tree (packaging/stage.sh), two thin packagers.
# The .deb and .rpm land on the GitHub Release via tag-publish.yml.
package_deb:
	make -C packaging deb

package_rpm:
	make -C packaging rpm

package: package_deb package_rpm
