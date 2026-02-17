#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IMAGE_NAME="neurokaraoke-linux-builder"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker is required to build Linux artifacts. Please install Docker Desktop." >&2
  exit 1
fi

# Detect OS and set cache paths accordingly
detect_cache_paths() {
  case "$(uname -s)" in
    Darwin)
      EB_CACHE_HOST="$HOME/Library/Caches/electron-builder"
      ELECTRON_CACHE_HOST="$HOME/Library/Caches/electron"
      ;;
    Linux)
      EB_CACHE_HOST="${XDG_CACHE_HOME:-$HOME/.cache}/electron-builder"
      ELECTRON_CACHE_HOST="${XDG_CACHE_HOME:-$HOME/.cache}/electron"
      ;;
    MINGW*|MSYS*|CYGWIN*)
      # Windows via Git Bash/MSYS2
      if [ -n "${LOCALAPPDATA:-}" ]; then
        # Convert Windows path to Unix-style for Docker
        WIN_CACHE="$(cygpath -u "$LOCALAPPDATA" 2>/dev/null || echo "$LOCALAPPDATA")"
        EB_CACHE_HOST="$WIN_CACHE/electron-builder/Cache"
        ELECTRON_CACHE_HOST="$WIN_CACHE/electron/Cache"
      else
        EB_CACHE_HOST=""
        ELECTRON_CACHE_HOST=""
      fi
      ;;
    *)
      EB_CACHE_HOST=""
      ELECTRON_CACHE_HOST=""
      ;;
  esac
}

build_image() {
  local platform="$1"
  docker build --platform "$platform" -t "$IMAGE_NAME" -f "$PROJECT_ROOT/Dockerfile.linux-build" "$PROJECT_ROOT"
}

run_build() {
  local platform="$1"
  local arch="$2"
  local node_modules_volume="neurokaraoke_node_modules_linux_${arch}"

  local cache_mounts=""

  if [ -n "$EB_CACHE_HOST" ] && [ -d "$EB_CACHE_HOST" ]; then
    cache_mounts="$cache_mounts -v $EB_CACHE_HOST:/root/.cache/electron-builder"
  fi
  if [ -n "$ELECTRON_CACHE_HOST" ] && [ -d "$ELECTRON_CACHE_HOST" ]; then
    cache_mounts="$cache_mounts -v $ELECTRON_CACHE_HOST:/root/.cache/electron"
  fi

  # MSYS_NO_PATHCONV prevents Git Bash from converting Unix paths to Windows paths
  MSYS_NO_PATHCONV=1 docker run --rm \
    --platform "$platform" \
    -v "$PROJECT_ROOT:/app" \
    -v "$node_modules_volume:/app/node_modules" \
    $cache_mounts \
    -w /app \
    "$IMAGE_NAME" \
    bash -lc "echo \"Container arch: \$(uname -m)\" && npm_config_platform=linux npm_config_arch=${arch} yarn install --frozen-lockfile && npm_config_platform=linux npm_config_arch=${arch} yarn build:linux --${arch}"
}

detect_cache_paths

echo "Building Linux artifacts via Docker..."

# Build Docker image (amd64 container can cross-compile for arm64)
echo "Building Docker image..."
build_image "linux/amd64"

# Build both x64 and arm64 in a single run
echo "Building x64 and arm64 packages together..."
node_modules_volume="neurokaraoke_node_modules_linux"

cache_mounts=""
if [ -n "$EB_CACHE_HOST" ] && [ -d "$EB_CACHE_HOST" ]; then
  cache_mounts="$cache_mounts -v $EB_CACHE_HOST:/root/.cache/electron-builder"
fi
if [ -n "$ELECTRON_CACHE_HOST" ] && [ -d "$ELECTRON_CACHE_HOST" ]; then
  cache_mounts="$cache_mounts -v $ELECTRON_CACHE_HOST:/root/.cache/electron"
fi

# MSYS_NO_PATHCONV prevents Git Bash from converting Unix paths to Windows paths
MSYS_NO_PATHCONV=1 docker run --rm \
  --platform "linux/amd64" \
  -v "$PROJECT_ROOT:/app" \
  -v "$node_modules_volume:/app/node_modules" \
  $cache_mounts \
  -w /app \
  "$IMAGE_NAME" \
  bash -lc "echo 'Building for x64 and arm64...' && yarn install --frozen-lockfile && yarn build:linux --x64 --arm64"

echo "Done. Artifacts are in ./dist"
