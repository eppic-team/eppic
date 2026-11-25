#!/usr/bin/env bash
#
# Builds the eppic docker images, tags them as "latest" and "master" and pushes both tags to harbor.
# Note that eppic-rest uses the master tag for prod.
#
# Usage: ./build-and-push.sh [--no-push] [--release VERSION] [module ...]
#   --no-push          only build and tag, don't push
#   --release VERSION  do a release, e.g. --release 3.5.6:
#                        1. set the maven version to VERSION, commit "Release VERSION", tag vVERSION
#                        2. build the images, also tagging them with VERSION, and push them
#                        3. set the maven version to the next snapshot (e.g. 3.5.7-SNAPSHOT) and commit
#                        4. push the branch and the tag to the upstream git remote
#                      If building or pushing images fails, the release commit and tag are undone.
#                      Requires a clean working tree, up to date with its upstream branch.
#   module             one or more of: eppic-cli eppic-dbtools eppic-rest (default: all 3)
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
usage() { sed -n '3,18p' "$0" | sed 's/^# \{0,1\}//'; }

push=true
modules=""
release=""
while [ $# -gt 0 ]; do
    case "$1" in
        --no-push) push=false ;;
        --release) [ $# -ge 2 ] || die "--release needs a version, e.g. --release 3.5.6"; release="$2"; shift ;;
        --release=*) release="${1#--release=}" ;;
        -h|--help) usage; exit 0 ;;
        eppic-cli|eppic-dbtools|eppic-rest) modules="$modules $1" ;;
        *) die "Unknown argument '$1'. Valid modules: $ALL_MODULES" ;;
    esac
    shift
done
modules="${modules:-$ALL_MODULES}"

# Build from the root of the git repo we're called from (where the Dockerfile is), so the script
# can live anywhere in the PATH and be run from any subdirectory of the eppic repo
repo_root=$(git rev-parse --show-toplevel 2>/dev/null) || repo_root=$(pwd)
cd "$repo_root"
[ -f Dockerfile ] || die "Dockerfile not found in $repo_root. Run this from within the eppic repo"

command -v docker >/dev/null 2>&1 || die "docker not found in PATH"
docker info >/dev/null 2>&1 || die "Cannot talk to the docker daemon. Is docker running?"

# Release pre-flight checks: everything that can fail is checked before anything is modified
if [ -n "$release" ]; then
    [[ "$release" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || die "Release version '$release' must be of the form X.Y.Z"
    $push || die "--release can't be combined with --no-push"
    command -v mvn >/dev/null 2>&1 || die "mvn not found in PATH (needed to set versions)"
    [ -z "$(git status --porcelain)" ] || die "Working tree has uncommitted changes. Commit or stash them before releasing"

    branch=$(git symbolic-ref --short -q HEAD) || die "Not on a branch (detached HEAD)"
    upstream=$(git rev-parse --abbrev-ref --symbolic-full-name '@{u}' 2>/dev/null) \
        || die "Branch '$branch' has no upstream. Set one with: git branch --set-upstream-to=<remote>/<branch>"
    remote="${upstream%%/*}"
    remote_branch="${upstream#*/}"
    tag="v$release"

    log "Fetching $remote"
    git fetch --quiet --tags "$remote"
    [ "$(git rev-list --count "HEAD..$upstream")" -eq 0 ] || die "Branch '$branch' is behind $upstream. Pull first"
    git rev-parse -q --verify "refs/tags/$tag" >/dev/null && die "Tag $tag already exists"

    current_version=$(mvn -q help:evaluate -Dexpression=project.version -DforceStdout -N)
    [[ "$current_version" == *-SNAPSHOT ]] || die "Current version '$current_version' is not a SNAPSHOT"
    next_version="${release%.*}.$(( ${release##*.} + 1 ))-SNAPSHOT"
    TAGS="$TAGS $release"
    log "Releasing $release (current $current_version, next $next_version), pushing to $remote/$remote_branch"
elif [ -n "$(git status --porcelain 2>/dev/null)" ]; then
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

set_version() {
    log "Setting maven version to $1"
    mvn -q versions:set -DnewVersion="$1" -DgenerateBackupPoms=false -DprocessAllModules=true \
        || die "mvn versions:set $1 failed"
}

if [ -n "$release" ]; then
    start_commit=$(git rev-parse HEAD)
    # Undo the release commit and tag if anything fails before the images are pushed.
    # The tree was clean at start_commit, so the hard reset loses nothing.
    rollback() {
        echo "Rolling back release commit and tag" >&2
        git tag -d "$tag" >/dev/null 2>&1 || true
        git reset --quiet --hard "$start_commit"
    }
    trap rollback EXIT

    set_version "$release"
    git commit --quiet -am "Release $release"
    git tag "$tag"
    log "Committed and tagged $tag"
fi

# Build and tag everything first, so that a failed build doesn't leave a partial set of images pushed
for module in $modules; do
    image="$REGISTRY/$PROJECT/$module"
    log "Building $image"
    tag_args=""
    for t in $TAGS; do
        tag_args="$tag_args -t $image:$t"
    done
    # shellcheck disable=SC2086
    docker build --build-arg MODULE="$module" $tag_args . || die "Build of $module failed"
done

if ! $push; then
    log "Done (not pushed)"
    exit 0
fi

for module in $modules; do
    for t in $TAGS; do
        ref="$REGISTRY/$PROJECT/$module:$t"
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

log "Pushed tags [$TAGS] for:$(printf ' %s' $modules)"

if [ -n "$release" ]; then
    # Images are out, from here on the release commit and tag must be kept
    trap - EXIT
    not_pushed="Release $tag is committed and tagged locally but not pushed to git"
    log "Setting maven version to $next_version"
    mvn -q versions:set -DnewVersion="$next_version" -DgenerateBackupPoms=false -DprocessAllModules=true \
        || die "mvn versions:set $next_version failed. $not_pushed"
    git commit --quiet -am "Next dev cycle" || die "Committing $next_version failed. $not_pushed"
    log "Pushing $branch and $tag to $remote"
    git push --atomic "$remote" "HEAD:refs/heads/$remote_branch" "refs/tags/$tag" \
        || die "git push failed. Push manually with: git push --atomic $remote HEAD:$remote_branch $tag"
    log "Released $release"
fi

log "Done"
