#!/usr/bin/env bash
set -euo pipefail

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-25-openjdk/}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"

BUILD_TOOLS="$ANDROID_SDK_ROOT/build-tools/36.0.0"
KEYSTORE="$PWD/build/keystore.jks"
KEYSTORE_PASS="thirteen"
KEY_ALIAS="thirteen"
KEY_PASS="thirteen"

if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$PWD/build"
    echo "Generating signing key at $KEYSTORE ..."
    keytool -genkey -v \
        -keystore "$KEYSTORE" \
        -alias "$KEY_ALIAS" \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -storepass "$KEYSTORE_PASS" \
        -keypass "$KEY_PASS" \
        -dname "CN=Thirteen, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown"
fi

./gradlew assembleRelease \
    -Pandroid.aapt2FromMavenOverride="$BUILD_TOOLS/aapt2" \
    "$@"

APK="app/build/outputs/apk/release/app-release-unsigned.apk"
SIGNED_APK="app/build/outputs/apk/release/app-release.apk"
"$BUILD_TOOLS/apksigner" sign \
    --ks "$KEYSTORE" \
    --ks-pass "pass:$KEYSTORE_PASS" \
    --key-pass "pass:$KEY_PASS" \
    --ks-key-alias "$KEY_ALIAS" \
    --out "$SIGNED_APK" \
    "$APK"

echo "Signed APK: $SIGNED_APK"
