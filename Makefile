


help:
	echo "Makes a release using Maven"

major:

ifndef MAJOR_VERSION
	$(error MAJOR_VERSION is not set)
endif

	mvn release:branch \
		--batch-mode \
		-0activate-profiles \
		-Ddry-run=true \
		-DautoVersionSubmodules=true \
		-DbranchName=release/$MAJOR_VERSION
