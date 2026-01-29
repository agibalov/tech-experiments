# ClickHouse + Superset + Lightdash + Grafana Experiment

Web analytics dashboard with ClickHouse as the data store, with three BI tools:
- **Apache Superset** - Direct SQL queries to ClickHouse
- **Lightdash** - dbt-first semantic layer approach
- **Grafana** - Observability-focused dashboards

## Start

```bash
just up
```

This starts Tilt, which:
1. Starts ClickHouse, Superset, Lightdash, and Grafana containers
2. Loads ~3.6M synthetic web analytics events (10 years of data)
3. Imports the pre-configured Superset dashboard
4. Bootstraps Lightdash (user, organization, project, charts, and dashboard)
5. Provisions Grafana with ClickHouse datasource and dashboard
6. Starts analytics-client that continuously queries ClickHouse

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

### Grafana (ready to use)
- **URL**: http://localhost:3001
- **Login**: admin / admin
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

### Grafana Dashboard
- Daily Events (time series)
- Traffic by Device (pie chart)
- Top Pages (table)
- Traffic by Country (table)
- Big numbers: Total Events, Unique Users

### Analytics Client
A Python script that queries ClickHouse directly every 5 seconds, demonstrating programmatic access without a BI tool. Outputs page views by day of week:

```
--- Page Views by Day of Week ---
  Monday: 264,123
  Tuesday: 263,891
  Wednesday: 264,012
  ...
---------------------------------
```

View output in Tilt UI under "analytics-client" resource.

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
