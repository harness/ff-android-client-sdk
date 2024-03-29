#!/usr/bin/env bash

# for generator options:
#
# https://openapi-generator.tech/docs/generators/java
# or
# npx -d openapi-generator-cli config-help -g java

if [[ ! -f client-v1.yaml ]] ; then
    echo 'client-v1.yaml missing. Aborting.'
    exit
fi


npx -d openapi-generator-cli version-manager set 7.0.1

npx -d openapi-generator-cli generate -i client-v1.yaml -g java -o _temp_api \
	--api-package=io.harness.cfsdk.cloud.openapi.client.api \
	--model-package=io.harness.cfsdk.cloud.openapi.client.model \
	--additional-properties hideGenerationTimestamp=true



# remove non-android annotations
grep -rl "javax.annotation.Generated" _temp_api | xargs sed -i "" -e 's/@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")//g'


grep -rl "javax.annotation.Nullable" _temp_api | xargs sed -i "" -e 's/@javax.annotation.Nullable//g'


grep -rl "javax.annotation.Nonnull" _temp_api | xargs sed -i "" -e 's/@javax.annotation.Nonnull//g'



# this patch removes insecure code and some newer API levels in the auto-generated code
# if you upgrade the openapi version you may need to create a new patch
git apply generateapi.patch

rm -rf cfsdk/src/main/java/io/harness/cfsdk/cloud/openapi
cp -R _temp_api/src/main/java/io/harness/cfsdk/cloud/openapi ../cfsdk/src/main/java/io/harness/cfsdk/cloud/
rm -rf _temp_api

