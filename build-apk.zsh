#!/bin/zsh

rm -v -rf ./app/pumpkin/

./gradlew --no-build-cache --no-configuration-cache :app:assemblePumpkin

