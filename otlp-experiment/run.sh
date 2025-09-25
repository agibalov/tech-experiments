OTEL_LOGS_EXPORTER=console,otlp \
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4318 \
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf \
OTEL_SERVICE_NAME=experiment-app \
OTEL_PYTHON_LOGGING_AUTO_INSTRUMENTATION_ENABLED=true \
OTEL_PYTHON_LOG_CORRELATION=true \
uv run opentelemetry-instrument uvicorn main:app #--reload

# NOTE: doesn't work with --reload
