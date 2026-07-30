#!/usr/bin/env bash
# Fires after Claude writes a Compose UI file. Advisory only — never blocks.
#
# Two triggers, deliberately narrow so the reminder stays worth reading:
#
#   1. The edit itself contained @Preview
#        -> preview code was just written. The conventions are not guessable,
#           so make it load the skill and check the result.
#
#   2. The file is NEW (untracked by git) and has @Composable but no @Preview
#        -> new UI shipped unpreviewable. This is the failure that left the
#           customPacks screens without previews.
#
# Editing a pre-existing file that happens to lack previews stays silent: that
# is a backlog, not a regression, and warning on it every time trains you to
# ignore the hook.
set -uo pipefail

input=$(cat)
file=$(printf '%s' "$input" | jq -r '.tool_response.filePath // .tool_input.file_path // empty' 2>/dev/null)

[ -n "$file" ] || exit 0

case "$file" in
    *.kt) ;;
    *) exit 0 ;;
esac

# Compose UI source only. ViewModels, data classes and navigation live outside
# app/src/main/**/ui/**, so they never reach here.
case "$file" in
    */app/src/main/*/ui/*) ;;
    *) exit 0 ;;
esac

# Theme plumbing is composable but renders nothing on its own.
case "$file" in
    */ui/theme/*) exit 0 ;;
esac

[ -f "$file" ] || exit 0

name=${file##*/}
written=$(printf '%s' "$input" | jq -r '[.tool_input.content, .tool_input.new_string, (.tool_input.edits[]?.new_string)] | map(select(. != null)) | join("\n")' 2>/dev/null)

# Trigger 1 — preview code was part of this edit.
if printf '%s' "$written" | grep -q '@Preview'; then
    jq -n --arg m "$name: you just wrote @Preview code. Invoke the creating-preview-of-composable skill and check it against the rules there — theme wrapper, annotation parameters, naming, file placement, and the rule about composables that take a ViewModel. Do not rely on remembering the conventions; the skill exists because they are not guessable." \
        '{hookSpecificOutput: {hookEventName: "PostToolUse", additionalContext: $m}}'
    exit 0
fi

# Trigger 2 — brand new UI file with composables and no previews at all.
grep -q '@Composable' "$file" || exit 0
grep -q '@Preview' "$file" && exit 0

if git -C "${file%/*}" ls-files --error-unmatch "$file" >/dev/null 2>&1; then
    exit 0  # pre-existing file, not a regression
fi

jq -n --arg m "$name is a new file declaring @Composable functions with no @Preview. If any of them render UI, they need light and dark previews — invoke the creating-preview-of-composable skill before writing them. If it only holds non-rendering composables (value-returning helpers, side-effect-only composables), no preview is needed and you can disregard this." \
    '{hookSpecificOutput: {hookEventName: "PostToolUse", additionalContext: $m}}'
