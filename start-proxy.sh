#! /bin/bash

set -e

./gradlew ${DATABASE?}${MODE?}ComposeUp