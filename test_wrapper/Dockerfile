FROM fedora:34

ARG PORT
ARG API_KEY
ARG SELF_TEST

ENV PORT "$PORT"
ENV API_KEY "$API_KEY"
ENV SELF_TEST "$SELF_TEST"

RUN dnf group install -y "Development Tools" && \
    sudo dnf install -y https://mirrors.rpmfusion.org/free/fedora/rpmfusion-free-release-$(rpm -E %fedora).noarch.rpm \
        https://mirrors.rpmfusion.org/nonfree/fedora/rpmfusion-nonfree-release-$(rpm -E %fedora).noarch.rpm && \
    dnf install -y wget java java-devel && \
    wget "https://dl.google.com/android/repository/commandlinetools-linux-7302050_latest.zip" && \
    unzip "commandlinetools-linux-7302050_latest.zip" && \
    mkdir -p /Android && cd /cmdline-tools/bin && \
    echo y | ./sdkmanager "platform-tools" "platforms;android-30" "sources;android-30" \
        "build-tools;30.0.3" "add-ons;addon-google_apis-google-24" "cmdline-tools;latest" --sdk_root="/Android"

#    dnf install -y gradle && \
#    cd / && \
#    mkdir CFAndroidSDK && cd ./CFAndroidSDK && \
#    git clone https://github.com/drone/ff-android-client-sdk.git . && \
#    git checkout FFM-1005 && \
#    gradle wrapper --gradle-version 6.7.1 --distribution-type all


EXPOSE "$PORT"