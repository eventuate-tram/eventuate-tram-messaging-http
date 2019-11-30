#! /bin/bash

set -e

docker="./gradlew ${DATABASE?}${MODE?}Compose"

. ./set-env-${DATABASE?}.sh

./gradlew testClasses assemble

${docker}Down
${docker}Build
${docker}Up

./wait-for-services.sh $DOCKER_HOST_IP "8099"

./gradlew build

${docker}Down
