#!/usr/bin/env bash
set -e

MAVEN_CMD="mvn"
if ! command -v mvn >/dev/null 2>&1; then
  echo "Apache Maven is not installed or not in PATH. Please install Maven 3.9+ first."
  exit 1
fi

exec "$MAVEN_CMD" "$@"
