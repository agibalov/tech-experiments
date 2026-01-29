# ClickHouse + Superset Experiment

Web analytics dashboard with ClickHouse as the data store and Apache Superset for visualization.

## Start

```bash
just up
```

This starts Tilt, which:
1. Starts ClickHouse and Superset containers
2. Loads ~3.6M synthetic web analytics events (10 years of data)
3. Imports the pre-configured dashboard

Wait for all resources to turn green in Tilt (~1-2 minutes on first run).

## Access

- **Superset**: http://localhost:8088
- **Login**: admin / admin
- **Dashboard**: "Web Analytics Overview"

## What to Expect

The dashboard shows:
- Daily page views (line chart)
- Top pages (table)
- Traffic by device type (pie chart)
- Traffic by country (table)

## Stop

```bash
just down
```

## Clean (remove all data)

```bash
just clean
```

## Other Commands

```bash
just                    # List all commands
just clickhouse-client  # Open ClickHouse CLI
just logs               # View container logs
just rebuild-superset   # Rebuild Superset image after dashboard changes
```
