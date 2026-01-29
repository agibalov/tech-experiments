{{ config(materialized='view') }}

SELECT
    event_time,
    event_type,
    user_id,
    session_id,
    page_url,
    referrer,
    device_type,
    country,
    browser
FROM analytics.events
