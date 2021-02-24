#!/bin/bash
#$DOCKER_ENV_HOST=0.0.0.0
echo 1: DOCKER_ENV_HOST: $DOCKER_ENV_HOST
if [ ! $DOCKER_ENV_HOST ]; then
    if [ Msys = '$(uname -o)2>/dev/null' ]; then #git for windows
        DOCKER_ENV_HOST=$(docker-machine ip default)
    else #other
        DOCKER_ENV_HOST=$(hostname -i)
    fi
fi
echo 2: DOCKER_ENV_HOST: $DOCKER_ENV_HOST


#abas Version prüfen und Docker-Version festlegen.
if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"2019"* ]]; then
  export ABAS_VERSION=2019
  export USE_ERP_VERSION="2019r4n20p04"
fi

if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"2018"* ]]; then
  export ABAS_VERSION=2018
  export USE_ERP_VERSION="2018r4n14p45"
fi

if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"2017"* ]]; then
  export ABAS_VERSION=2017
  export USE_ERP_VERSION="2017r4n16p39"
fi

sed "s+2019r4n20p01+$USE_ERP_VERSION+g" -i "./Dockerfile-erp-overrides"

echo ABAS_VERSION ${ABAS_VERSION}
echo USE_ERP_VERSION ${USE_ERP_VERSION}



if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"feature"* ]]; then
  export SNAPSHOT="-SNAPSHOT"
else
  export SNAPSHOT=""
fi

# Version festlegen
if [ -f version ]; then
      VERSION=$(cat version)
      export APP_VERSION=${VERSION}.${ABAS_VERSION}${SNAPSHOT}-${CODEBUILD_BUILD_NUMBER}
      APP_VERSION_WITHOUT_SNAPSHOT=${VERSION}.${ABAS_VERSION}-${CODEBUILD_BUILD_NUMBER}
      echo ${APP_VERSION_WITHOUT_SNAPSHOT} > APPVERSION
fi

echo APP_VERSION: $APP_VERSION

if [ ! $APP_VERSION ]; then
  echo "APP_VERSION not set"
  exit 1
fi


pwd=$(pwd)
find . -name "*.properties.template" | while IFS= read -r pathname; do
    dirname=$(dirname "$pathname")
    sed "s+DOCKER_ENV_HOST+$DOCKER_ENV_HOST+g" "$pathname" > "$dirname/gradle.properties"
    sed "s,APP_VERSION,${APP_VERSION},g" -i "$dirname/gradle.properties"
done
