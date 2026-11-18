#!/usr/bin/env bash
#
# Builds the eppic docker images, tags them as "latest" and "master" and pushes both tags to harbor.
# Note that eppic-rest uses the master tag for prod.
#
# Usage: ./build-and-push.sh [--no-push] [module ...]
#   --no-push   only build and tag, don't push
#   module      one or more of: eppic-cli eppic-dbtools eppic-rest (default: all 3)
#
# If pushing fails due to auth, log in first with:
#   docker login harbor.devops.k8s.rcsb.org

set -euo pipefail

REGISTRY="harbor.devops.k8s.rcsb.org"
PROJECT="rcsb"
ALL_MODULES="eppic-cli eppic-dbtools eppic-rest"
TAGS="latest master"

log() { echo "[$(date '+%H:%M:%S')] $*"; }
die() { echo "ERROR: $*" >&2; exit 1; }

push=true
modules=""
for arg in "$@"; do
    case "$arg" in
        --no-push) push=false ;;
        -h|--help) sed -n '3,11p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
        eppic-cli|eppic-dbtools|eppic-rest) modules="$modules $arg" ;;
        *) die "Unknown argument '$arg'. Valid modules: $ALL_MODULES" ;;
    esac
done
modules="${modules:-$ALL_MODULES}"

# Build from the root of the git repo we're called from (where the Dockerfile is), so the script
# can live anywhere in the PATH and be run from any subdirectory of the eppic repo
repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || repo_root=$(pwd)
cd "$repo_root"
[ -f Dockerfile ] || die "Dockerfile not found in $repo_root. Run this from within the eppic repo"

command -v docker >/dev/null 2>&1 || die "docker not found in PATH"
docker info >/dev/null 2>&1 || die "Cannot talk to the docker daemon. Is docker running?"

if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
    log "WARNING: git working tree has uncommitted changes, they will be included in the images"
fi

# Check harbor credentials up front, so that we don't find out after a long build.
# With stored credentials 'docker login' re-validates them against the registry; with none it
# would prompt, which fails immediately because stdin is not a terminal.
if $push; then
    log "Checking login to $REGISTRY"
    if ! docker login "$REGISTRY" </dev/null >/dev/null 2>&1; then
        die "Not logged in to $REGISTRY (or credentials expired). Run: docker login $REGISTRY"
    fi
fi

# Build and tag everything first, so that a failed build doesn't leave a partial set of images pushed
for module in $modules; do
    image="$REGISTRY/$PROJECT/$module"
    log "Building $image"
    tag_args=""
    for tag in $TAGS; do
        tag_args="$tag_args -t $image:$tag"
    done
    # shellcheck disable=SC2086
    docker build --build-arg MODULE="$module" $tag_args . || die "Build of $module failed"
done

if ! $push; then
    log "Done (not pushed)"
    exit 0
fi

for module in $modules; do
    for tag in $TAGS; do
        ref="$REGISTRY/$PROJECT/$module:$tag"
        log "Pushing $ref"
        # Capture output so that we can give a useful hint on auth failures, while still showing progress
        if ! output=$(docker push "$ref" 2>&1 | tee /dev/stderr); then
            if echo "$output" | grep -qiE "unauthorized|authentication required|denied|403"; then
                die "Push of $ref was rejected by the registry. Check you are logged in (docker login $REGISTRY) and have push rights on project '$PROJECT'"
            fi
            die "Push of $ref failed"
        fi
    done
done

log "Done. Pushed tags [$TAGS] for:$(printf ' %s' $modules)"
