#! /bin/bash

set -e

docker="./gradlew ${DATABASE?}${MODE?}Compose"

./gradlew testClasses assemble

${docker}Down
${docker}Up

./gradlew build

${docker}Down
