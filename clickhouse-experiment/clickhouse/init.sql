CREATE DATABASE IF NOT EXISTS analytics;

CREATE TABLE IF NOT EXISTS analytics.events (
    event_time DateTime,
    event_type LowCardinality(String),
    user_id UUID,
    session_id UUID,
    page_url String,
    referrer LowCardinality(String),
    device_type LowCardinality(String),
    country LowCardinality(String),
    browser LowCardinality(String)
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_time)
ORDER BY (event_time, user_id);
