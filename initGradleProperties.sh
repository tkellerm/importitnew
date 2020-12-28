#!/bin/bash

if [ ! $DOCKER_ENV_HOST ]; then
    if [ Msys = '$(uname -o)2>/dev/null' ]; then #git for windows
        DOCKER_ENV_HOST=$(docker-machine ip default)
    else #other
        DOCKER_ENV_HOST=$(hostname)
    fi
fi

if [[ $BITBUCKET_BRANCH == *"2019"* ]]; then
  export ABAS_VERSION=2019
  export USE_ERP_VERSION=2019r4n18p01
fi

if [[ $BITBUCKET_BRANCH == *"2018"* ]]; then
  export ABAS_VERSION=2018
  export USE_ERP_VERSION="2018r4n14p42"
fi

if [[ $BITBUCKET_BRANCH == *"2017"* ]]; then
  export ABAS_VERSION=2017
  export USE_ERP_VERSION="2017r4n16p39"
fi

sed "s+2019r4n18p01+$USE_ERP_VERSION+g" -i "./Dockerfile-erp-overrides"

echo ABAS_VERSION ${ABAS_VERSION}
echo USE_ERP_VERSION ${USE_ERP_VERSION}

if [[ $BITBUCKET_BRANCH == *"feature"* ]]; then
  export SNAPSHOT="-SNAPSHOT"
else
  export SNAPSHOT=""
fi


if [ -f version ]; then
      VERSION=$(cat version)
      export APP_VERSION=${VERSION}.${ABAS_VERSION}.${BITBUCKET_BUILD_NUMBER}
else
      export APP_VERSION=1.${ABAS_VERSION}.${BITBUCKET_BUILD_NUMBER}
fi
echo $APP_VERSION

if [ ! $APP_VERSION ]; then

  exit 1 "APP_VERSION not set"
fi


pwd=$(pwd)

find . -name "*.properties.template" | while IFS= read -r pathname; do
    dirname=$(dirname "$pathname")
    sed "s+DOCKER_ENV_HOST+$DOCKER_ENV_HOST+g" "$pathname" > "$dirname/gradle.properties"
    sed "s+APP_VERSION+$APP_VERSION$SNAPSHOT+g" -i "$dirname/gradle.properties"
done
