#!/bin/bash
set -e

if [ -z "${REPO_URL}" ] || [ -z "${RUNNER_TOKEN}" ]; then
  echo "Error: REPO_URL and RUNNER_TOKEN environment variables must be set."
  echo "Example: docker run -e REPO_URL=https://github.com/androcraftman/Android-Indentity-Architecture -e RUNNER_TOKEN=XXXX ..."
  exit 1
fi

RUNNER_NAME=${RUNNER_NAME:-android-ci-runner}
RUNNER_LABELS=${RUNNER_LABELS:-self-hosted,android-ci-runner}

echo "Configuring GitHub Actions Runner: ${RUNNER_NAME}..."

./config.sh \
  --url "${REPO_URL}" \
  --token "${RUNNER_TOKEN}" \
  --name "${RUNNER_NAME}" \
  --labels "${RUNNER_LABELS}" \
  --work "_work" \
  --unattended \
  --replace

cleanup() {
  echo "Removing runner..."
  ./config.sh remove --token "${RUNNER_TOKEN}" || true
}
trap cleanup EXIT INT TERM

echo "GitHub Actions Runner registered. Starting runner loop..."
./run.sh
