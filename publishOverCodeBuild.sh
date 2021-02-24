#!/bin/bash -e

# Get run directory of script.
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
REPO_DIR="${SCRIPT_DIR}"
BIN_DIR=${REPO_DIR}/bin

echo REPO_DIR "${REPO_DIR}"
# Prüfen, ob Release-Branch
export RELEASE=0

if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"abasV2019"* ]]; then
  export RELEASE=1
fi

if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"abasV2018"* ]]; then
  export RELEASE=1
fi
echo Branch ${CODEBUILD_WEBHOOK_HEAD_REF}
echo Release ${RELEASE}

#abas Version prüfen
if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"2019"* ]]; then
  export ABAS_VERSION=2019
fi

if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"2018"* ]]; then
  export ABAS_VERSION=2018
fi

if [[ $CODEBUILD_WEBHOOK_HEAD_REF == *"2017"* ]]; then
  export ABAS_VERSION=2017
fi


APP_JAR_NAME_PATTERN=importit*-standalone-app.jar
APP_VERSION=$(cat APPVERSION)
REPO_NAME=importit
ARTIFACTORY_JAR_TARGET_PATH_SNAPSHOT=abas.snapshots/de/abas/importit
ARTIFACTORY_JAR_TARGET_PATH_RELEASE=abas.releases/de/abas/importit
ARTIFACTORY_TESTPROTOCOL_TARGET_PATH=test-archive/importit
BUILD_NAME="${REPO_NAME}-${CODEBUILD_WEBHOOK_HEAD_REF}"
ARTIFACTORY_SERVER_ID=registry.abas.sh

echo APP_VERSION ${APP_VERSION}
echo ABAS_VERSION ${ABAS_VERSION}
echo BUILD_NAME ${BUILD_NAME}
echo CODEBUILD_BUILD_ID ${CODEBUILD_BUILD_ID}
echo DOCKER_REGISTRY_URL ${DOCKER_REGISTRY_URL}


zipTestAndUploadToArtifactory()
{
  echo "upload Test-Protokoll"
  FILENAME='test-report.'${APP_VERSION}'.zip'
  zip -r ${REPO_DIR}/"${FILENAME}" ${REPO_DIR}/build/test-results/
  ${REPO_DIR}/jfrog rt upload ${FILENAME} "${ARTIFACTORY_TESTPROTOCOL_TARGET_PATH}/${ABAS_VERSION}/" --build-name="${BUILD_NAME}" --build-number=$CODEBUILD_BUILD_ID --flat=false --server-id registry.abas.sh
  ${REPO_DIR}/jfrog rt build-collect-env "${BUILD_NAME}" $CODEBUILD_BUILD_ID
  ${REPO_DIR}/jfrog rt set-props "${APP_JAR_NAME_PATTERN}" VERSION=${APP_VERSION} --build ${BUILD_NAME}/${CODEBUILD_BUILD_ID}
  ${REPO_DIR}/jfrog rt build-publish "${BUILD_NAME}" $CODEBUILD_BUILD_ID
}

uploadInstallerSnapshotJarToArtifactory()
{
   echo "jfroog upload snapshots"
   cd ${REPO_DIR}/build/libs/
   ${REPO_DIR}/jfrog rt upload ${APP_JAR_NAME_PATTERN} "${ARTIFACTORY_JAR_TARGET_PATH_SNAPSHOT}/${APP_VERSION}/" --build-name="${BUILD_NAME}" --build-number=$CODEBUILD_BUILD_ID --flat=false --server-id=${ARTIFACTORY_SERVER_ID} --target-props=VERSION=$APP_VERSION
   echo "jfroog  build-collect-env"
   jfrog rt build-collect-env "$APP_JAR_NAME_PATTERN" $CODEBUILD_BUILD_ID
   echo "jfroog  set-props"
   ${REPO_DIR}/jfrog rt set-props "${APP_JAR_NAME_PATTERN}" VERSION=${APP_VERSION} --build ${BUILD_NAME}/${CODEBUILD_BUILD_ID}
   echo "jfroog  build-publish"
   ${REPO_DIR}/jfrog rt build-publish "${BUILD_NAME}" ${CODEBUILD_BUILD_ID}
   cd ${REPO_DIR}
}

uploadInstallerReleaseJarToArtifactory()
{
  echo "upload Release"
  cd build/libs/
  ${REPO_DIR}/jfrog rt upload ${APP_JAR_NAME_PATTERN} "${ARTIFACTORY_JAR_TARGET_PATH_RELEASE}/${APP_VERSION}/" --build-name="${BUILD_NAME}" --build-number=$CODEBUILD_BUILD_ID --flat=false --server-id=${ARTIFACTORY_SERVER_ID} --target-props=VERSION=$APP_VERSION
   echo "jfroog  build-collect-env"
   ${REPO_DIR}/jfrog rt build-collect-env "$APP_JAR_NAME_PATTERN" $CODEBUILD_BUILD_ID
   echo "jfroog  set-props"
   ${REPO_DIR}/jfrog rt set-props "${APP_JAR_NAME_PATTERN}" VERSION=${APP_VERSION} --build ${BUILD_NAME}/${CODEBUILD_BUILD_ID}
   echo "jfroog  build-publish"
   ${REPO_DIR}/jfrog rt build-publish "${BUILD_NAME}" ${CODEBUILD_BUILD_ID}
   cd ${REPO_DIR}
}

initJfrog()
{
  echo "initJfrog "
  # CI is set, because this script runs on code-build
  export CI=true
  ${REPO_DIR}/jfrog --version
  ${REPO_DIR}/jfrog rt config ${ARTIFACTORY_SERVER_ID} --url=$DOCKER_REGISTRY_URL --apikey=$DOCKER_REGISTRY_APIKEY --user=$DOCKER_REGISTRY_USERNAME  --interactive=false
  ${REPO_DIR}/jfrog rt config show
}

publishDocumentationRelease()
{
  echo "upload documentation to documentation.abas.cloud "
  export AWS_DEFAULT_REGION="us-east-1"
  temp_role=$(aws sts assume-role --role-arn "arn:aws:iam::754342080068:role/ci/documentation/UpdateAbasImportitDocumentation" --role-session-name "bitbucket-pipelines" --output text | tail -n 1)
  export AWS_ACCESS_KEY_ID=$(echo $temp_role | awk '{print $2;}')
  export AWS_SECRET_ACCESS_KEY=$(echo $temp_role | awk '{print $4;}')
  export AWS_SESSION_TOKEN=$(echo $temp_role | awk '{print $5;}')
  cd ~
  aws s3 sync ${REPO_DIR}/build/asciidoc/html5 s3://abas-documentation-prod-us-east-1/de/importit

}

publishDocumentationSnapshot()
{
  echo "upload documentation to documentation.abas.ninja"
  export AWS_DEFAULT_REGION="us-east-1"
  temp_role=$(aws sts assume-role --role-arn "arn:aws:iam::374056703733:role/ci/documentation/UpdateAbasImportitDocumentation" --role-session-name "bitbucket-pipelines" --output text | tail -n 1)
  export AWS_ACCESS_KEY_ID=$(echo $temp_role | awk '{print $2;}')
  export AWS_SECRET_ACCESS_KEY=$(echo $temp_role | awk '{print $4;}')
  export AWS_SESSION_TOKEN=$(echo $temp_role | awk '{print $5;}')
  aws s3 sync ${REPO_DIR}/asciidoc/html5 s3://abas-documentation-test-us-east-1/de/importit

}


#Upload to Artifactory
initJfrog
if [ $RELEASE -eq 0 ] ; then
  uploadInstallerSnapshotJarToArtifactory
  publishDocumentationSnapshot
else
  uploadInstallerReleaseJarToArtifactory
  zipTestAndUploadToArtifactory
  publishDocumentationRelease
fi
