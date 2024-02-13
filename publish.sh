#!/usr/bin/env bash
set -e

if [[ -z "${GRADLE_DEPLOY_KEYRING}" ]]; then
  echo "GRADLE_DEPLOY_KEYRING needs to be set (e.g ~/.gnupg/keyring.gpg)"
  exit 1
fi


if [[ -z "${GRADLE_DEPLOY_SIGN_PASSWORD}" ]]; then
  echo "GRADLE_DEPLOY_SIGN_PASSWORD needs to be set"
  exit 1
fi

if [[ -z "${GRADLE_DEPLOY_SIGN_KEYID}" ]]; then
  echo "GRADLE_DEPLOY_SIGN_KEYID needs to be set to last 8 hex digits of signing key (see 'gpg --keyring ~/.gnupg/keyring.gpg --list-keys')"
  exit 1
fi

if [[ -z "${OSSRH_USERNAME}" ]]; then
  echo "OSSRH_USERNAME needs to be set for Sonatype"
  exit 1
fi

if [[ -z "${OSSRH_PASSWORD}" ]]; then
  echo "OSSRH_PASSWORD needs to be set for Sonatype user"
  exit 1
fi

# needed for gradle to get the right format
gpg --no-default-keyring --keyring ${GRADLE_DEPLOY_KEYRING} --export-secret-keys -o /tmp/keyring.gpg


echo "This version will be published: "
grep "version('sdk'" settings.gradle

# note if the version in settings.gradle ends in -SNAPSHOT AAR will be released directly to the sonatype snapshot repository
./gradlew publish -Psigning.secretKeyRingFile=/tmp/keyring.gpg -Psigning.password=$GRADLE_DEPLOY_SIGN_PASSWORD -Psigning.keyId=$GRADLE_DEPLOY_SIGN_KEYID

rm /tmp/keyring.gpg
