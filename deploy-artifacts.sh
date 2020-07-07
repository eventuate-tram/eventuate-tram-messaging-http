#! /bin/bash -e

BRANCH=$(git rev-parse --abbrev-ref HEAD)

if [  $BRANCH == "master" ] ; then
  VERSION=$(sed -e '/^version=/!d' -e 's/^version=\(.*\)-SNAPSHOT$/\1.BUILD-SNAPSHOT/' < gradle.properties)
  echo master: publishing $VERSION
  ./gradlew -P version=$VERSION -P deployUrl=${S3_REPO_DEPLOY_URL} uploadArchives
else

  if ! [[  $BRANCH =~ ^[0-9]+ ]] ; then
    echo Not release $BRANCH - no PUSH
    exit 0
  elif [[  $BRANCH =~ RELEASE$ ]] ; then
    BINTRAY_REPO_TYPE=release
  elif [[  $BRANCH =~ M[0-9]+$ ]] ; then
      BINTRAY_REPO_TYPE=milestone
  elif [[  $BRANCH =~ RC[0-9]+$ ]] ; then
      BINTRAY_REPO_TYPE=rc
  else
    echo cannot figure out bintray for this branch $BRANCH
    exit -1
  fi

  echo BINTRAY_REPO_TYPE=${BINTRAY_REPO_TYPE}

  VERSION=$BRANCH

  $PREFIX ./gradlew -P version=${VERSION} \
    -P bintrayRepoType=${BINTRAY_REPO_TYPE} \
    -P deployUrl=https://dl.bintray.com/eventuateio-oss/eventuate-maven-${BINTRAY_REPO_TYPE} \
    testClasses assemble bintrayUpload

fi

docker login -u ${DOCKER_USER_ID?} -p ${DOCKER_PASSWORD?}

$PREFIX ./gradlew -P version=${VERSION} assemble mysqlbinlogComposeBuild

$PREFIX ./gradlew -P version=${VERSION} mysqlbinlogComposePush

