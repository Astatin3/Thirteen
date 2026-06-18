#!/usr/bin/env bash
set -euo pipefail

export JAVA_HOME="/usr/lib/jvm/java-25-openjdk/"

./gradlew assembleRelease "$@"
