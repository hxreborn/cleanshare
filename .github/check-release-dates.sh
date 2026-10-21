#!/usr/bin/env bash
set -euo pipefail

apk=$(realpath "${1:?Usage: .github/check-release-dates.sh <release apk>}")
cd "$(git rev-parse --show-toplevel)"
if diff <(unzip -p "$apk" assets/release-dates.txt | sort) \
	<(gh api 'repos/{owner}/{repo}/releases' --paginate \
		--jq '.[] | select((.draft or .prerelease) | not) | "\(.tag_name) \(.published_at[:10])"' | sort); then
	echo "PASS release-dates.txt matches published GitHub releases"
else
	echo "FAIL release-dates.txt differs from published GitHub releases"
	exit 1
fi
