#!/bin/bash
set -euo pipefail

# GHCR Package Cleanup Script
# Deletes older container image versions, keeping only the most recent ones

# Configuration
PACKAGE_NAME="${PACKAGE_NAME:-vokabel-server}"
PACKAGE_TYPE="${PACKAGE_TYPE:-container}"
KEEP_COUNT="${KEEP_COUNT:-10}"
ORG_OR_USER="${ORG_OR_USER:-$(gh api user --jq .login)}"

echo "🧹 Cleaning up GHCR packages..."
echo "Package: ${PACKAGE_NAME}"
echo "Type: ${PACKAGE_TYPE}"
echo "Keep last: ${KEEP_COUNT} versions"
echo "Owner: ${ORG_OR_USER}"
echo ""

# Get all package versions sorted by created date (newest first)
echo "📦 Fetching package versions..."
VERSIONS=$(gh api \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "/users/${ORG_OR_USER}/packages/${PACKAGE_TYPE}/${PACKAGE_NAME}/versions" \
  --paginate \
  --jq 'sort_by(.created_at) | reverse | .[] | {id: .id, name: .name, tags: .metadata.container.tags, created: .created_at}')

# Count total versions
TOTAL_COUNT=$(echo "$VERSIONS" | jq -s 'length')
echo "Found ${TOTAL_COUNT} versions"

if [ "$TOTAL_COUNT" -le "$KEEP_COUNT" ]; then
  echo "✅ Nothing to delete. Current versions (${TOTAL_COUNT}) <= Keep count (${KEEP_COUNT})"
  exit 0
fi

DELETE_COUNT=$((TOTAL_COUNT - KEEP_COUNT))
echo "🗑️  Will delete ${DELETE_COUNT} old versions"
echo ""

# Get versions to delete (skip the first KEEP_COUNT versions)
VERSIONS_TO_DELETE=$(echo "$VERSIONS" | jq -s ".[$KEEP_COUNT:]")

# Delete old versions
DELETED=0
FAILED=0

echo "$VERSIONS_TO_DELETE" | jq -c '.[]' | while read -r version; do
  VERSION_ID=$(echo "$version" | jq -r '.id')
  VERSION_NAME=$(echo "$version" | jq -r '.name // "untagged"')
  VERSION_TAGS=$(echo "$version" | jq -r '.tags[]? // "untagged"' | tr '\n' ',' | sed 's/,$//')
  CREATED_AT=$(echo "$version" | jq -r '.created')

  echo "Deleting version ${VERSION_ID} (${VERSION_NAME}) [${VERSION_TAGS}] created at ${CREATED_AT}..."

  if gh api \
    --method DELETE \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "/users/${ORG_OR_USER}/packages/${PACKAGE_TYPE}/${PACKAGE_NAME}/versions/${VERSION_ID}" \
    > /dev/null 2>&1; then
    echo "  ✅ Deleted"
    DELETED=$((DELETED + 1))
  else
    echo "  ❌ Failed to delete"
    FAILED=$((FAILED + 1))
  fi
done

echo ""
echo "✨ Cleanup complete!"
echo "   Deleted: ${DELETED}"
if [ "$FAILED" -gt 0 ]; then
  echo "   Failed: ${FAILED}"
  exit 1
fi

