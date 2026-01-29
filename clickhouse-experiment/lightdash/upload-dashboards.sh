#!/bin/bash
set -e

# Upload Lightdash dashboards from YAML files
# Reads bootstrap output from container and runs CLI locally

echo "=== Lightdash Dashboard Upload ==="

# Get bootstrap output from container
BOOTSTRAP_OUTPUT=$(docker compose exec -T lightdash cat /tmp/lightdash-bootstrap-output.json 2>/dev/null || echo "")

if [ -z "$BOOTSTRAP_OUTPUT" ]; then
    echo "Bootstrap output not found. Skipping dashboard upload."
    echo "Run lightdash-bootstrap first."
    exit 0
fi

# Parse JSON output
PROJECT_UUID=$(echo "$BOOTSTRAP_OUTPUT" | jq -r '.projectUuid')
TOKEN=$(echo "$BOOTSTRAP_OUTPUT" | jq -r '.token')
LIGHTDASH_URL=$(echo "$BOOTSTRAP_OUTPUT" | jq -r '.lightdashUrl')

if [ -z "$PROJECT_UUID" ] || [ "$PROJECT_UUID" = "null" ]; then
    echo "No project UUID found in bootstrap output. Skipping."
    exit 0
fi

echo "Project UUID: $PROJECT_UUID"
echo "Lightdash URL: $LIGHTDASH_URL"

# Login to CLI
echo "Logging into Lightdash CLI..."
npx @lightdash/cli login "$LIGHTDASH_URL" --token "$TOKEN" --project "$PROJECT_UUID"

# Upload dashboards (use -p to specify the path to charts/dashboards folders)
echo "Uploading charts and dashboards..."
npx @lightdash/cli upload --force -p lightdash

echo "=== Dashboard Upload Complete ==="
