


help:
	echo "Makes a release using Maven"

major:

ifndef MAJOR_VERSION
	$(error MAJOR_VERSION is not set)
endif

	mvn release:branch \
		--batch-mode \
		-Ddry-run=true \
		-DcommitByProject=true \
		-DautoVersionSubmodules=true \
		-DbranchName=release/$MAJOR_VERSION
