FROM apache/superset:latest

USER root
RUN apt-get update && apt-get install -y --no-install-recommends zip && rm -rf /var/lib/apt/lists/*

# Install clickhouse-connect into the Superset venv
RUN pip install --target=/app/.venv/lib/python3.10/site-packages clickhouse-connect

# Copy custom config
COPY superset_config.py /app/superset_config.py
ENV SUPERSET_CONFIG_PATH=/app/superset_config.py

# Dashboards are code - bake them in
COPY bootstrap.sh /app/bootstrap.sh
COPY dashboard_export /app/dashboard_export

USER superset
