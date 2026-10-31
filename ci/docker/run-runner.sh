#!/bin/bash
set -e

# Configuration script to build and launch the self-hosted GitHub Actions Runner container
IMAGE_BUILDER="android-builder:latest"
IMAGE_RUNNER="android-ci-runner:latest"
CONTAINER_NAME="android-ci-runner"

if [ -z "$RUNNER_TOKEN" ]; then
  echo "Error: RUNNER_TOKEN is not set."
  echo "Usage: RUNNER_TOKEN=<TOKEN> ./ci/docker/run-runner.sh"
  echo "Get a token from: https://github.com/androcraftman/Android-Indentity-Architecture/settings/actions/runners/new"
  exit 1
fi

REPO_URL=${REPO_URL:-"https://github.com/androcraftman/Android-Indentity-Architecture"}

echo "Step 1: Building base Android Builder image..."
docker build -t ${IMAGE_BUILDER} -f ci/docker/Dockerfile ci/docker

echo "Step 2: Building GitHub Actions Runner image..."
docker build -t ${IMAGE_RUNNER} -f ci/docker/Dockerfile.runner ci/docker

echo "Step 3: Stopping previous runner if active..."
docker rm -f ${CONTAINER_NAME} 2>/dev/null || true

echo "Step 4: Starting GitHub Actions Runner container..."
docker run -d \
  --name ${CONTAINER_NAME} \
  --restart unless-stopped \
  -e REPO_URL="${REPO_URL}" \
  -e RUNNER_TOKEN="${RUNNER_TOKEN}" \
  -e RUNNER_NAME="${CONTAINER_NAME}" \
  -e RUNNER_LABELS="self-hosted,android-ci-runner" \
  ${IMAGE_RUNNER}

echo "Runner container started successfully!"
echo "Check logs: docker logs -f ${CONTAINER_NAME}"
