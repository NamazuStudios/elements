


help:
	echo "Makes a release using Maven"

major:

ifndef MAJOR_VERSION
	$(error MAJOR_VERSION is not set)
endif

	mvn --batch-mode \
		--dry-run=true