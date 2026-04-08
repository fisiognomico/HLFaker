#!/bin/zsh
build_hlfaker() {
    rsync -avz --delete --exclude='.git/' --exclude='build/' \
        ./ dev:~/HLFaker/
    ssh dev "cd ~/HLFaker && ANDROID_HOME=/opt/android-sdk ./gradlew clean build"
}

build_hlfaker
