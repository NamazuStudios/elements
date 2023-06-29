


help:
	echo "Makes a release using Maven"

major:

ifndef MAJOR_VERSION
	$(error MAJOR_VERSION is not set)
endif

	mvn release:prepare \
		--batch-mode \
		-DskipTests=true \
		-DdryRun=true \
		-DcommitByProject=true \
		-DautoVersionSubmodules=true \
		-DbranchName=release/$MAJOR_VERSION
