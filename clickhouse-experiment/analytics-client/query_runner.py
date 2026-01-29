# /// script
# requires-python = ">=3.11"
# dependencies = [
#     "clickhouse-connect",
# ]
# ///
"""
Continuously queries ClickHouse for analytics data.
Demonstrates direct ClickHouse access without a BI tool.
"""

import time
import clickhouse_connect

CLICKHOUSE_HOST = "localhost"
CLICKHOUSE_PORT = 8123
CLICKHOUSE_USER = "default"
CLICKHOUSE_PASSWORD = "clickhouse"
CLICKHOUSE_DATABASE = "analytics"

QUERY_INTERVAL = 5  # seconds

DAYS_OF_WEEK = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]

QUERY = """
SELECT
    toDayOfWeek(event_time) as day_of_week,
    count() as visits
FROM analytics.events
WHERE event_type = 'page_view'
GROUP BY day_of_week
ORDER BY day_of_week
"""


def get_client():
    return clickhouse_connect.get_client(
        host=CLICKHOUSE_HOST,
        port=CLICKHOUSE_PORT,
        username=CLICKHOUSE_USER,
        password=CLICKHOUSE_PASSWORD,
        database=CLICKHOUSE_DATABASE,
    )


def run_query(client):
    result = client.query(QUERY)
    visits_by_day = {row[0]: row[1] for row in result.result_rows}

    print("\n--- Page Views by Day of Week ---")
    for i, day in enumerate(DAYS_OF_WEEK, start=1):
        count = visits_by_day.get(i, 0)
        print(f"  {day}: {count:,}")
    print("---------------------------------")


def main():
    print("Analytics Query Runner")
    print(f"Querying ClickHouse at {CLICKHOUSE_HOST}:{CLICKHOUSE_PORT}")
    print(f"Interval: {QUERY_INTERVAL}s")
    print()

    client = None

    while True:
        try:
            if client is None:
                client = get_client()
                print("Connected to ClickHouse")

            run_query(client)

        except Exception as e:
            print(f"Error: {e}")
            print("Retrying...")
            client = None

        time.sleep(QUERY_INTERVAL)


if __name__ == "__main__":
    main()
