# main.py
import logging
from fastapi import FastAPI

from opentelemetry import trace, metrics
from opentelemetry.sdk.resources import Resource

# ---- Traces ----
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter

# ---- Metrics ----
from opentelemetry.sdk.metrics import MeterProvider
from opentelemetry.sdk.metrics.export import PeriodicExportingMetricReader
from opentelemetry.exporter.otlp.proto.http.metric_exporter import OTLPMetricExporter

# ---- Logs ----
from opentelemetry.sdk._logs import LoggerProvider, LoggingHandler
from opentelemetry.sdk._logs.export import BatchLogRecordProcessor
from opentelemetry.exporter.otlp.proto.http._log_exporter import OTLPLogExporter

# ---- FastAPI auto-hooks ----
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

def setup_instrumentation():
    RESOURCE = Resource.create({"service.name": "experiment-app", "deployment.environment": "dev"})
    OTLP_HTTP = "http://localhost:4318"  # use 4317 + gRPC exporters if you prefer gRPC

    # Traces
    tp = TracerProvider(resource=RESOURCE)
    tp.add_span_processor(BatchSpanProcessor(OTLPSpanExporter(endpoint=f"{OTLP_HTTP}/v1/traces")))
    trace.set_tracer_provider(tp)
    tracer = trace.get_tracer(__name__)

    # Metrics
    metrics.set_meter_provider(MeterProvider(
        resource=RESOURCE,
        metric_readers=[PeriodicExportingMetricReader(OTLPMetricExporter(endpoint=f"{OTLP_HTTP}/v1/metrics"))],
    ))

    # Logs
    lp = LoggerProvider(resource=RESOURCE)
    lp.add_log_record_processor(BatchLogRecordProcessor(OTLPLogExporter(endpoint=f"{OTLP_HTTP}/v1/logs")))
    logging.getLogger().addHandler(LoggingHandler(level=logging.INFO, logger_provider=lp))
    logging.getLogger().setLevel(logging.INFO)

def instrument_app(app: FastAPI):
    FastAPIInstrumentor.instrument_app(app)  # adds HTTP spans
