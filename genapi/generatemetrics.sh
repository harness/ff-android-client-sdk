#!/usr/bin/env bash

if [[ ! -f metrics-v1.yaml ]] ; then
    echo 'metrics-v1.yaml missing. Aborting.'
    exit
fi


npx -d openapi-generator-cli version-manager set 7.0.1

npx -d openapi-generator-cli generate -i metrics-v1.yaml -g java -o _temp_metric_api \
	--api-package=io.harness.cfsdk.cloud.openapi.metric.api \
	--model-package=io.harness.cfsdk.cloud.openapi.metric.model \
	--additional-properties hideGenerationTimestamp=true

grep -rl "javax.annotation.Generated" _temp_metric_api | xargs sed -i "" -e 's/@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")//g'
grep -rl "javax.annotation.Nullable" _temp_metric_api | xargs sed -i "" -e 's/@javax.annotation.Nullable//g'
grep -rl "javax.annotation.Nonnull" _temp_metric_api | xargs sed -i "" -e 's/@javax.annotation.Nonnull//g'

cp -R _temp_metric_api/src/main/java/io/harness/cfsdk/cloud/openapi ../cfsdk/src/main/java/io/harness/cfsdk/cloud/
rm -rf _temp_metric_api
