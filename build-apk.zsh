#!/bin/zsh

rm -v -rf ./app/hg42/release/

./gradlew --no-build-cache --no-configuration-cache :app:assembleHg42

