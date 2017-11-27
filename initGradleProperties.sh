#!/bin/bash
if [ ! $DOCKER_ENV_HOST ]; then
    if [ "Msys" == "$(uname -o 2> /dev/null )" ]; then #git for windows
	    DOCKER_ENV_HOST=$(docker-machine ip default)
    else #other
	    DOCKER_ENV_HOST=$(hostname)
    fi
fi
# fallback if DOCKER_ENV_HOST is still empty
if [ ! $DOCKER_ENV_HOST ]; then
	DOCKER_ENV_HOST=$ABAS_HOST
fi
echo using DOCKER_ENV_HOST=$DOCKER_ENV_HOST

pwd=$(pwd)
DOCKER_EDP_PORT=$EDP_TEST_PORT
DOCKER_SSH_PORT=$SSH_TEST_PORT
if [ ! $DOCKER_EDP_PORT ]; then
    DOCKER_EDP_PORT=6560
fi
if [ ! $DOCKER_SSH_PORT ]; then
    DOCKER_SSH_PORT=2205
fi

find . -name "*.properties.template" | while IFS= read -r pathname; do
    dirname=$(dirname "$pathname")
    sed "s+DOCKER_ENV_HOST+$DOCKER_ENV_HOST+g; s+DOCKER_EDP_PORT+$DOCKER_EDP_PORT+g; s+DOCKER_SSH_PORT+$DOCKER_SSH_PORT+g" "$pathname" > "$dirname/gradle.properties"
done
