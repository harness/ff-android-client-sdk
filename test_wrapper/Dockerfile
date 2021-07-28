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
    dnf install -y wget && \
    wget "https://redirector.gvt1.com/edgedl/android/studio/ide-zips/4.2.2.0/android-studio-ide-202.7486908-linux.tar.gz" && \
         tar -xf "android-studio-ide-202.7486908-linux.tar.gz" && \
    dnf install gradle

#    dnf install -y gradle && \
#    cd / && \
#    mkdir CFAndroidSDK && cd ./CFAndroidSDK && \
#    git clone https://github.com/drone/ff-android-client-sdk.git . && \
#    git checkout FFM-1005 && \
#    gradle wrapper --gradle-version 6.7.1 --distribution-type all


EXPOSE "$PORT"