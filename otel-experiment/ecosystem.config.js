module.exports = {
    apps: [
        {
            name: "hey-root",
            script: "hey",
            args: "-c 1 -q 2 -z 24h -m GET http://localhost:8000/",
        },
        {
            name: "hey-hello",
            script: "hey",
            args: "-c 1 -q 1 -z 24h -m GET http://localhost:8000/hello",
        },
        {
            name: "app-1",
            script: "uv",
            args: "run opentelemetry-instrument uvicorn main:app",
            env: {
                OTEL_LOGS_EXPORTER: "console,otlp",
                OTEL_EXPORTER_OTLP_ENDPOINT: "http://localhost:4318",
                OTEL_EXPORTER_OTLP_PROTOCOL: "http/protobuf",
                OTEL_SERVICE_NAME: "experiment-app",
                OTEL_PYTHON_LOGGING_AUTO_INSTRUMENTATION_ENABLED: "true",
                OTEL_SEMCONV_STABILITY_OPT_IN: "http"
            }
        },
        {
            name: "otel-lgtm",
            script: "bash",
            args: ["-lc", "docker compose up"]
        }
    ]
};
