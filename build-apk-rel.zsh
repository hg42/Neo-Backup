#!/bin/zsh

rm -v -rf ./app/pumprel/

./gradlew --no-build-cache --no-configuration-cache :app:assemblePumprel
