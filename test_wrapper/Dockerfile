FROM androidsdk/android-30:latest

ARG PORT
ARG API_KEY
ARG SELF_TEST

ENV PORT "$PORT"
ENV API_KEY "$API_KEY"
ENV SELF_TEST "$SELF_TEST"

RUN apt install -y gradle && \
    mkdir CFAndroidSDK && cd ./CFAndroidSDK && \
    git clone https://github.com/drone/ff-android-client-sdk.git . && \
    git checkout FFM-1005 && \
    gradle wrapper --gradle-version 6.7.1 --distribution-type all


EXPOSE "$PORT"