#! /bin/bash

set -e

./gradlew ${DATABASE?}${MODE?}ComposeBuild
./gradlew ${DATABASE?}${MODE?}ComposeUp