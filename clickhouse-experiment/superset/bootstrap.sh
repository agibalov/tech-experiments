#!/bin/bash
set -e

echo "=== Superset Bootstrap ==="

# Initialize the database
echo "Upgrading database..."
superset db upgrade

# Create admin user if not exists
echo "Creating admin user..."
superset fab create-admin \
    --username "${ADMIN_USERNAME:-admin}" \
    --firstname Admin \
    --lastname User \
    --email "${ADMIN_EMAIL:-admin@example.com}" \
    --password "${ADMIN_PASSWORD:-admin}" \
    || echo "Admin user may already exist, continuing..."

# Initialize Superset
echo "Initializing Superset..."
superset init

# Import dashboards from YAML
if [ -d /app/dashboard_export ]; then
    echo ""
    echo "=== Importing Dashboards ==="

    # Create ZIP bundle for import
    rm -rf /tmp/export_bundle /tmp/dashboard.zip
    mkdir -p /tmp/export_bundle
    cp -r /app/dashboard_export/* /tmp/export_bundle/
    cd /tmp
    zip -r dashboard.zip export_bundle

    echo "Importing from YAML..."
    superset import-dashboards -p /tmp/dashboard.zip -u "${ADMIN_USERNAME:-admin}" \
        || echo "Import failed"

    rm -rf /tmp/export_bundle /tmp/dashboard.zip
fi

echo ""
echo "=== Bootstrap Complete ==="
