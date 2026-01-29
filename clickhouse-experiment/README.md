# ClickHouse + Superset + Lightdash Experiment

Web analytics dashboard with ClickHouse as the data store, with two BI tools:
- **Apache Superset** - Direct SQL queries to ClickHouse
- **Lightdash** - dbt-first semantic layer approach

## Start

```bash
just up
```

This starts Tilt, which:
1. Starts ClickHouse, Superset, and Lightdash containers
2. Loads ~3.6M synthetic web analytics events (10 years of data)
3. Imports the pre-configured Superset dashboard
4. Bootstraps Lightdash (user, organization, project, charts, and dashboard)

Wait for all resources to turn green in Tilt (~2-3 minutes on first run).

## Access

### Superset (ready to use)
- **URL**: http://localhost:8088
- **Login**: admin / admin
- **Dashboard**: "Web Analytics Overview" (pre-configured)

### Lightdash (ready to use)
- **URL**: http://localhost:3000
- **Login**: admin@example.com / admin123
- **Project**: "Analytics" with ClickHouse warehouse (auto-configured)
- **Dashboard**: "Web Analytics Overview" (pre-configured)

## What to Expect

### Superset Dashboard
- Daily page views (line chart)
- Top pages (table)
- Traffic by device type (pie chart)
- Traffic by country (table)
- Big numbers: Total Events, Unique Users
- Events by Day of Week (bar chart)

### Lightdash Dashboard
- Daily Events (line chart)
- Top Pages (table)
- Traffic by Device (pie chart)
- Traffic by Country (table)
- Big numbers: Total Events, Unique Users

The `events` dbt model includes:
- Dimensions: event_time, event_type, page_url, device_type, country, browser
- Metrics: Total Events, Unique Users, Unique Sessions

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
just lightdash          # Open Lightdash in browser
just lightdash-logs     # View Lightdash logs
```
