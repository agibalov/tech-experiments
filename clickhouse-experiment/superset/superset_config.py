import os

# Security
SECRET_KEY = os.environ.get('SUPERSET_SECRET_KEY', 'default-secret-key')

# Use simple in-memory cache (no Redis needed for demo)
CACHE_CONFIG = {
    'CACHE_TYPE': 'SimpleCache',
    'CACHE_DEFAULT_TIMEOUT': 300,
}

DATA_CACHE_CONFIG = CACHE_CONFIG

# SQLite for metadata (simple demo setup)
SQLALCHEMY_DATABASE_URI = 'sqlite:////app/superset_home/superset.db'

# Disable CSRF for API (simplifies programmatic access)
WTF_CSRF_ENABLED = False

# Feature flags
FEATURE_FLAGS = {
    'ENABLE_TEMPLATE_PROCESSING': True,
}
