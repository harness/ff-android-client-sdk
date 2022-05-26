# Building ff-android-client-sdk

This document shows the instructions on how to build and contribute to the SDK.

## Requirements
[Android Studio](https://developer.android.com/studio?gclid=CjwKCAjwp7eUBhBeEiwAZbHwkRqdhQkk6wroJeWGu0uGWjW9Ue3hFXc4SuB6lwYU4LOZiZ-MQ4p57BoCvF0QAvD_BwE&gclsrc=aw.ds) or the [Android SDK](docs/dev_environment.md) for CLI only<br>
[Java 11](https://www.oracle.com/java/technologies/downloads/#java11) or newer <br>
[Gradle 7.4.1](https://gradle.org/releases/) or newer <br>

## Setup Android SDK
If you wish to use Android Studio then please follow the [official](https://developer.android.com/studio/install) guide to install and setup your environment.
If you wish to install the minimal CLI tools only, then folow these [steps to install the SDK and an emulator](dev_environment.md#android-sdk)

## Cloning the SDK repository
In order to clone SDK repository properly perform cloning like in the following example:
```shell
git clone --recurse-submodules git@github.com:harness/ff-android-client-sdk.git
```

## Build the SDK
The SDK is built with gradle.  Use the following commands to build

```bash
./gradlew build
```

## Executing tests

```bash
./gradlew test
```

