#!/usr/bin/env bash


docker \
	run \
	--rm \
	--name registry-browser \
	-p 8080:8080 \
	--env DOCKER_REGISTRY_URL=https://distribution.getelements.dev \
	klausmeyer/docker-registry-browser

