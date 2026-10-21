#!/usr/bin/env bash
set -euo pipefail

VERSION_INPUT=${1:-}

if [[ -z "$VERSION_INPUT" ]]; then
	echo "Usage: .github/release.sh <version>"
	echo "Example: .github/release.sh 3.4.0"
	exit 1
fi

VERSION="${VERSION_INPUT#v}"

if [[ ! "$VERSION" =~ ^[1-9][0-9]*\.[0-9]\.[0-9]$ ]]; then
	echo "Error: invalid version '$VERSION_INPUT'"
	echo "Example: .github/release.sh 3.4.0"
	exit 1
fi

TAG="v$VERSION"

if [[ -n "$(git status --porcelain)" ]]; then
	echo "Error: working tree not clean"
	exit 1
fi

BRANCH=$(git rev-parse --abbrev-ref HEAD)
if [[ "$BRANCH" != "main" ]]; then
	echo "Error: not on main branch (currently on $BRANCH)"
	exit 1
fi

git pull --ff-only

if ! command -v git-cliff &>/dev/null; then
	echo "Error: git-cliff not installed"
	exit 1
fi

PREV_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "none")
COMMIT_COUNT=$(git rev-list "${PREV_TAG}..HEAD" --count 2>/dev/null || git rev-list HEAD --count)

echo "Releasing ${PREV_TAG} → ${TAG} (${COMMIT_COUNT} commits)"
echo ""
git cliff --config .github/cliff.toml --tag "$TAG" --unreleased
echo ""

VERSION_CODE="${VERSION//./}"
CHANGELOG_FILE="fastlane/metadata/android/en-US/changelogs/${VERSION_CODE}.txt"

if [[ ! -f "$CHANGELOG_FILE" ]]; then
	echo "Warning: missing fastlane changelog: $CHANGELOG_FILE"
	read -rp "Continue anyway? [y/N] " skip_changelog
	if [[ ! "$skip_changelog" =~ ^[Yy]$ ]]; then
		echo "Aborted - create changelog first"
		exit 1
	fi
fi

read -rp "Push and release? [y/N] " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
	echo "Aborted"
	exit 1
fi

sed -i -E "s/^(\s+versionCode = ).*/\1${VERSION_CODE}/; s/^(\s+versionName = ).*/\1\"${VERSION}\"/" app/build.gradle.kts
RELEASE_DATES="app/src/main/assets/release-dates.txt"
sed -i "1i ${TAG} $(date -u +%F)" "$RELEASE_DATES"

git add app/build.gradle.kts "$RELEASE_DATES"
git commit -s -S -m "chore(build): bump version to ${VERSION}"

git tag -a "$TAG" -m "Release $TAG"
git push origin main "$TAG"

echo ""
echo "Released $TAG"
