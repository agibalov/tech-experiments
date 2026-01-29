# Dashboarding Tools Comparison

Comparison of Superset, Lightdash, and Grafana for ClickHouse-based analytics dashboards.

## Summary

| Criteria | Superset | Lightdash | Grafana |
|----------|----------|-----------|---------|
| GitOps-friendliness | Medium | High | High |
| Setup complexity | Low (1 service) | High (4 services) | Low (1 service) |

## 1. GitOps-Friendliness

### Superset

**Rating: Medium**

**Initial setup:**
- Requires custom Docker image to add `clickhouse-connect` driver
- Bootstrap script runs `superset db upgrade`, creates admin user, initializes app
- Datasource and dashboard imported via `superset import-dashboards` CLI

**Dashboard deployment:**
- Dashboards exported as ZIP containing YAML/JSON files
- Import via CLI: `superset import-dashboards -p dashboard.zip`
- Dashboard JSON is verbose but readable
- No official "dashboards as code" workflow - export/import is the mechanism

**Challenges:**
- Dashboard export includes UUIDs that can cause conflicts on re-import
- Must rebuild ZIP file from directory before import
- No native CLI for granular chart/dashboard management

### Lightdash

**Rating: High**

**Initial setup:**
- Most complex bootstrap: user registration, organization creation, project creation, warehouse connection
- Requires dbt project with metrics defined in YAML
- Bootstrap via custom JavaScript hitting REST API

**Dashboard deployment:**
- Official CLI: `lightdash upload --force`
- Charts and dashboards stored as YAML files
- Native "Dashboards as Code" feature designed for GitOps
- Clean separation: `charts/*.yml` and `dashboards/*.yml`

**Challenges:**
- Initial bootstrap is complex (no CLI for org/project setup)
- Requires dbt project structure even for simple use cases
- CLI requires auth token from bootstrap

### Grafana

**Rating: High**

**Initial setup:**
- Zero bootstrap code required
- Admin user created via environment variables
- Plugins installed via `GF_INSTALL_PLUGINS` environment variable
- Datasources provisioned via YAML files on startup

**Dashboard deployment:**
- Dashboards provisioned from JSON files automatically
- Place files in provisioning directory, Grafana loads them on startup
- No import scripts, no CLI commands, no API calls needed
- Dashboard JSON is verbose but well-documented

**Challenges:**
- Dashboard JSON can be large and hard to edit by hand
- No YAML format for dashboards (JSON only)
- Variable/datasource UIDs must be managed carefully

## 2. Setup Complexity

### Superset

**Services: 1**

```
superset (custom image with clickhouse-connect)
```

- Uses SQLite internally for metadata (sufficient for demos)
- No external database required for basic setup
- Production would add PostgreSQL + Redis, but not required for hello-world

**Resource footprint:** ~500MB RAM

### Lightdash

**Services: 4**

```
lightdash        (application)
lightdash-db     (PostgreSQL for metadata)
minio            (S3-compatible storage)
minio-init       (bucket initialization)
```

- PostgreSQL required for metadata storage
- MinIO/S3 required for query result pagination with ClickHouse
- Cannot simplify further - S3 removal breaks ClickHouse queries

**Resource footprint:** ~1.5GB RAM

### Grafana

**Services: 1**

```
grafana (with clickhouse plugin)
```

- Uses embedded SQLite for metadata
- No external database required
- Plugins downloaded on container startup

**Resource footprint:** ~200MB RAM

## Recommendations

**Choose Superset if:**
- You want rich SQL exploration and ad-hoc queries
- You need 40+ visualization types
- Your team is comfortable with SQL

**Choose Lightdash if:**
- You already use dbt for data transformation
- You want a semantic/metrics layer
- Non-technical users need self-serve analytics
- GitOps workflow is a hard requirement

**Choose Grafana if:**
- You want the simplest possible setup
- Dashboards are mostly time-series focused
- You value operational simplicity over features
- You're already using Grafana for monitoring
