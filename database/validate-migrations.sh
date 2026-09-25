#!/usr/bin/env bash
# =========================================================================
# TourOS Database & Migration Static Analysis (SQLFluff / Postgres Dialect)
# =========================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Base branch tespiti
BASE_BRANCH=""
if git rev-parse --verify origin/master >/dev/null 2>&1; then
    BASE_BRANCH="origin/master"
elif git rev-parse --verify master >/dev/null 2>&1; then
    BASE_BRANCH="master"
elif git rev-parse --verify origin/main >/dev/null 2>&1; then
    BASE_BRANCH="origin/main"
elif git rev-parse --verify main >/dev/null 2>&1; then
    BASE_BRANCH="main"
fi

TARGET_FILES=()
if [ -n "$BASE_BRANCH" ]; then
    while IFS= read -r rel_file; do
        [ -z "$rel_file" ] && continue
        if [ -f "$PROJECT_ROOT/$rel_file" ]; then
            TARGET_FILES+=("$PROJECT_ROOT/$rel_file")
        fi
    done < <(git -C "$PROJECT_ROOT" diff --name-only --diff-filter=d "${BASE_BRANCH}...HEAD" -- "supabase/migrations/*.sql" "database/migrations/*.sql" 2>/dev/null || true)
fi

# Değişen migration yoksa başarıyla atla (skip)
if [ "${#TARGET_FILES[@]}" -eq 0 ]; then
    echo "========================================================================="
    echo "No changed migrations to validate, skipping."
    echo "========================================================================="
    exit 0
fi

echo "========================================================================="
echo "🔍 Validating changed migrations with SQLFluff (Postgres dialect)..."
echo "Found ${#TARGET_FILES[@]} changed migration file(s):"
for f in "${TARGET_FILES[@]}"; do
    echo "   - $(basename "$f")"
done
echo "========================================================================="

# SQLFluff parse ile doğrudan PostgreSQL sözdizimi (syntax) denetimi
FAILED=0
for file in "${TARGET_FILES[@]}"; do
    FILENAME=$(basename "$file")
    echo -n "   - Parsing $FILENAME (syntax check)... "
    if sqlfluff parse --dialect postgres "$file" > /tmp/parse_out.log 2>&1; then
        echo "OK"
    else
        echo "❌ SYNTAX ERROR!"
        echo "-------------------------------------------------------------------------"
        cat /tmp/parse_out.log
        echo "-------------------------------------------------------------------------"
        FAILED=1
    fi
done

if [ "$FAILED" -ne 0 ]; then
    echo "❌ Migration dosyasında sözdizimi (syntax) hatası tespit edildi!"
    exit 1
fi

echo "========================================================================="
echo "✅ All changed migrations passed static SQL syntax validation successfully!"
echo "========================================================================="
exit 0
