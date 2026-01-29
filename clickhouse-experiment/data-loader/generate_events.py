#!/usr/bin/env python3
# /// script
# requires-python = ">=3.11"
# dependencies = [
#     "clickhouse-connect>=0.7.0",
#     "tqdm>=4.65.0",
# ]
# ///
"""
Generate synthetic web analytics events and load into ClickHouse.

Usage:
    uv run generate_events.py [--start-date YYYY-MM-DD] [--end-date YYYY-MM-DD] [--events-per-day N]
"""

import argparse
import random
import time
import uuid
from datetime import datetime, timedelta
from typing import Iterator

import clickhouse_connect
from tqdm import tqdm

# Configuration
PAGES = [
    "/home",
    "/products",
    "/products/item-1",
    "/products/item-2",
    "/products/item-3",
    "/about",
    "/contact",
    "/blog",
    "/blog/post-1",
    "/blog/post-2",
    "/blog/post-3",
    "/pricing",
    "/signup",
    "/login",
]

REFERRERS = [
    "direct",
    "google",
    "facebook",
    "twitter",
    "linkedin",
    "reddit",
    "bing",
    "duckduckgo",
]

DEVICE_TYPES = ["desktop", "mobile", "tablet"]
DEVICE_WEIGHTS = [0.55, 0.38, 0.07]  # Desktop still leads, mobile growing

COUNTRIES = ["US", "UK", "DE", "FR", "CA", "AU", "JP", "BR", "IN", "NL"]
COUNTRY_WEIGHTS = [0.35, 0.12, 0.10, 0.08, 0.07, 0.06, 0.05, 0.05, 0.07, 0.05]

BROWSERS = ["Chrome", "Firefox", "Safari", "Edge", "Other"]
BROWSER_WEIGHTS = [0.65, 0.10, 0.18, 0.05, 0.02]

EVENT_TYPES = ["page_view", "click", "session_start", "session_end"]
EVENT_WEIGHTS = [0.60, 0.25, 0.08, 0.07]


def weighted_choice(items: list, weights: list) -> str:
    """Select an item based on weights."""
    return random.choices(items, weights=weights, k=1)[0]


def generate_events_for_day(date: datetime, base_events: int) -> Iterator[tuple]:
    """Generate events for a single day."""
    # Add some variance to daily event count (±30%)
    daily_events = int(base_events * random.uniform(0.7, 1.3))

    # Weekday factor (more traffic on weekdays)
    weekday = date.weekday()
    if weekday < 5:  # Monday-Friday
        daily_events = int(daily_events * 1.2)
    else:  # Weekend
        daily_events = int(daily_events * 0.7)

    # Seasonal factor (simulate growth over years + seasonal patterns)
    year = date.year
    month = date.month

    # Year-over-year growth (10% per year from 2015 baseline)
    years_since_2015 = year - 2015
    growth_factor = 1.0 + (years_since_2015 * 0.10)
    daily_events = int(daily_events * growth_factor)

    # Seasonal pattern (higher in Q4, lower in summer)
    seasonal_factors = {
        1: 0.95, 2: 0.90, 3: 0.95, 4: 1.00,
        5: 0.95, 6: 0.85, 7: 0.80, 8: 0.85,
        9: 1.00, 10: 1.05, 11: 1.15, 12: 1.10
    }
    daily_events = int(daily_events * seasonal_factors[month])

    # Generate sessions (group of events from same user)
    num_sessions = daily_events // 5  # Average 5 events per session

    for _ in range(num_sessions):
        user_id = uuid.uuid4()
        session_id = uuid.uuid4()

        # Session properties (consistent within session)
        device = weighted_choice(DEVICE_TYPES, DEVICE_WEIGHTS)
        country = weighted_choice(COUNTRIES, COUNTRY_WEIGHTS)
        browser = weighted_choice(BROWSERS, BROWSER_WEIGHTS)
        referrer = weighted_choice(REFERRERS, [0.30, 0.25, 0.15, 0.08, 0.07, 0.05, 0.05, 0.05])

        # Generate events within this session
        session_events = random.randint(2, 10)
        session_start_hour = random.randint(0, 23)
        session_start_minute = random.randint(0, 59)

        for i in range(session_events):
            # Calculate event time within session
            event_time = date.replace(
                hour=session_start_hour,
                minute=session_start_minute,
                second=random.randint(0, 59)
            ) + timedelta(minutes=i * random.randint(1, 5))

            # Make sure we don't go past midnight
            if event_time.date() != date.date():
                break

            # Determine event type
            if i == 0:
                event_type = "session_start"
            elif i == session_events - 1:
                event_type = "session_end"
            else:
                event_type = weighted_choice(["page_view", "click"], [0.7, 0.3])

            page_url = random.choice(PAGES)

            yield (
                event_time,
                event_type,
                user_id,
                session_id,
                page_url,
                referrer,
                device,
                country,
                browser,
            )


def main():
    parser = argparse.ArgumentParser(description="Generate web analytics events")
    parser.add_argument(
        "--start-date",
        type=str,
        default="2015-01-01",
        help="Start date (YYYY-MM-DD)",
    )
    parser.add_argument(
        "--end-date",
        type=str,
        default=datetime.now().strftime("%Y-%m-%d"),
        help="End date (YYYY-MM-DD)",
    )
    parser.add_argument(
        "--events-per-day",
        type=int,
        default=1000,
        help="Base number of events per day",
    )
    parser.add_argument(
        "--host",
        type=str,
        default="localhost",
        help="ClickHouse host",
    )
    parser.add_argument(
        "--port",
        type=int,
        default=8123,
        help="ClickHouse HTTP port",
    )
    parser.add_argument(
        "--batch-size",
        type=int,
        default=10000,
        help="Batch size for inserts",
    )
    parser.add_argument(
        "--password",
        type=str,
        default="clickhouse",
        help="ClickHouse password",
    )

    args = parser.parse_args()

    start_date = datetime.strptime(args.start_date, "%Y-%m-%d")
    end_date = datetime.strptime(args.end_date, "%Y-%m-%d")

    print(f"Generating events from {args.start_date} to {args.end_date}")
    print(f"Base events per day: {args.events_per_day}")
    print(f"Connecting to ClickHouse at {args.host}:{args.port}")

    # Connect to ClickHouse with retry
    client = None
    for attempt in range(30):
        try:
            client = clickhouse_connect.get_client(
                host=args.host,
                port=args.port,
                username="default",
                password=args.password,
                database="analytics",
            )
            break
        except Exception as e:
            if attempt < 29:
                print(f"Connection attempt {attempt + 1} failed, retrying in 2s...")
                time.sleep(2)
            else:
                raise e

    # Check if table exists and has data
    result = client.query("SELECT count() FROM events")
    existing_count = result.result_rows[0][0]
    if existing_count > 0:
        print(f"Table already has {existing_count:,} rows. Skipping data generation.")
        print("To regenerate, run: just clickhouse-client then TRUNCATE TABLE analytics.events")
        return

    # Generate and insert data
    total_days = (end_date - start_date).days + 1
    current_date = start_date
    batch = []
    total_inserted = 0

    columns = [
        "event_time",
        "event_type",
        "user_id",
        "session_id",
        "page_url",
        "referrer",
        "device_type",
        "country",
        "browser",
    ]

    with tqdm(total=total_days, desc="Generating events", unit="day") as pbar:
        while current_date <= end_date:
            for event in generate_events_for_day(current_date, args.events_per_day):
                batch.append(event)

                if len(batch) >= args.batch_size:
                    client.insert("events", batch, column_names=columns)
                    total_inserted += len(batch)
                    batch = []

            current_date += timedelta(days=1)
            pbar.update(1)
            pbar.set_postfix({"inserted": f"{total_inserted:,}"})

    # Insert remaining batch
    if batch:
        client.insert("events", batch, column_names=columns)
        total_inserted += len(batch)

    print(f"\nTotal events inserted: {total_inserted:,}")

    # Verify
    result = client.query("SELECT count() FROM events")
    print(f"Verified row count: {result.result_rows[0][0]:,}")


if __name__ == "__main__":
    main()
