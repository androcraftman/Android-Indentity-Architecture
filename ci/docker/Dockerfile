# Base image: Eclipse Temurin OpenJDK 21 on Ubuntu 22.04 LTS (Jammy)
FROM --platform=linux/amd64 eclipse-temurin:21-jdk-jammy

ENV DEBIAN_FRONTEND=noninteractive

# Install essential dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    unzip \
    zip \
    git \
    wget \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Set up Android SDK paths and environment variables
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/build-tools/35.0.0:${PATH}

# Download and install Android command line tools
# Commandline-tools release: 11076708
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    curl -o /tmp/cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

# Programmatically accept all Android SDK licenses
RUN yes | sdkmanager --licenses

# Install platform-tools, build-tools, and API platforms (36 and 37 for compileSdk 37)
RUN sdkmanager \
    "platform-tools" \
    "build-tools;35.0.0" \
    "platforms;android-36" \
    "platforms;android-37"

WORKDIR /app
