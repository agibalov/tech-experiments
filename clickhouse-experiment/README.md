# ClickHouse + Superset Experiment

Web analytics dashboard with ClickHouse as the data store and Apache Superset for visualization.

## Start

```bash
docker compose up -d
```

Wait for Superset to initialize (~30 seconds on first run). Check logs:

```bash
docker compose logs superset -f
```

Look for `=== Bootstrap Complete ===` followed by `Starting gunicorn`.

## Access

- **Superset**: http://localhost:8088
- **Login**: admin / admin
- **Dashboard**: "Web Analytics Overview" (pre-configured)

## What to Expect

The dashboard shows:
- Daily page views (line chart)
- Top pages (table)
- Traffic by device type (pie chart)
- Traffic by country (table)

Note: Charts will be empty until you load sample data into ClickHouse.

## Load Sample Data

```bash
cd data-loader
pip install -r requirements.txt
python generate_events.py
```

This generates ~3.6M synthetic web analytics events (10 years of data).

## Stop

```bash
docker compose down
```

## Clean (remove all data)

```bash
docker compose down -v
sudo rm -rf clickhouse/data
```
