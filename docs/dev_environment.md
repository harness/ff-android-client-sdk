# Android Development Environment

## Android Studio
If you wish to use Android Studio then please follow the [official](https://developer.android.com/studio/install) guide to install and setup your environment.
If you wish to install the minimal CLI tools only, then read below.

## Android SDK
You can use the android command line tools to install the parts of the SDK required for development.  All the files
will be stored under $ANDROID_HOME, so you can easily remove them later.  

This works by first downloading the command line tools, then using sdkmanager to download and boostrap the other components.

Download the command line tools from the [offical site](https://developer.android.com/studio) then follow these steps:

1) Create a folder that will be the ANDROID_HOME
    ```shell
   mkdir -p /home/bob/android/sdk
    ```
   
2) Set ANDROID_HOME in your shell (you should also add this to your profile)
   ```shell
   export ANDROID_HOME=/home/dave/android/sdk
   ```
   
3) Unpack the command line tools
   ```shell
   unzip commandlinetools-mac-8092744_latest.zip -d $ANDROID_HOME
   ```
   N.B due to an oddity with the command line tools package, after unpacking you need to
   move the binaries into a folder called latest
   ```shell
    cd $ANDROID_HOME/cmdline-tools && mkdir latest && mv NOTICE.txt bin lib source.properties latest
   ```
   
4) Install Platform, Platform-tools, System Images and Emulator
   ```shell
   ## platform-tools
   echo "y" | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install platform-tools
   
   ## platform (android 32)
   $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install "platforms;android-32"
   
   ## System Images (android 32)
   echo "y" | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --install "system-images;android-32;google_apis;x86_64"
   ```
   
5) If everything worked, when you ls the $ANDROID_HOME 
   ```shell
   ls $ANDROID_HOME
   ```
   
   you should see the following output

   ```shell
   cmdline-tools  emulator       licenses       patcher        platform-tools platforms      system-images
   ```
   
6) Setup a virtual device.  In the above steps we installed android-32 so we will use this version when setting up the device.
   ```shell
   $ANDROID_HOME/cmdline-tools/latest/bin/avdmanager --verbose create avd --force --name "Pixel_4.4_API_32" --device "pixel" --package "system-images;android-32;google_apis;x86_64" --tag "google_apis" --abi "google_apis/x86_64"
   ```

## Android Emulator
Before setting up the emulator you should follow the steps above to install the [Android SDK](#Android-SDK)

1) To list what devices have been installed run the following command:
   ```shell
   $ANDROID_HOME/emulator/emulator -list-avds
   ```
   
   which gives output like so:
   ```shell
   Pixel_2_API_30
   Pixel_2_API_30_2
   Pixel_4.4_API_32
   Pixel_5_API_32
   ```

   
2) Start the emulator with desired device (prefix the name with @)
   ```shell
   $ANDROID_HOME/emulator/emulator @Pixel_4.4_API_32
   ```

## Connecting to a Local FF-Server
To connect to a local FF Server, you must enable clear text traffic.
This can be done by adding the following to the project AndroidManifest.xml

```
<application>
        android:usesCleartextTraffic="true"
</application>
```

Because you will be running an emulator, the localhost that your project is using will not be the same as where the FF-Server is running.
You must use the host [10.0.2.2 (read more)](https://developer.android.com/studio/run/emulator-networking.html) which is bridged to your machines localhost.
