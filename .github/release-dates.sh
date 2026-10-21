#!/usr/bin/env bash
set -euo pipefail

cd "$(git rev-parse --show-toplevel)"
gh api 'repos/{owner}/{repo}/releases' --paginate \
	--jq '.[] | select((.draft or .prerelease) | not) | "\(.tag_name) \(.published_at[:10])"' >app/src/main/assets/release-dates.txt
