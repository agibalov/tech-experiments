# otlp-experiment

Learning OpenTelemetry.

## Prerequisites

* Docker and Docker Compose working without root.
* NodeJS
* Python
* uv

## How to do things

* `npx pm2 start` to start everything.
* `npx pm2 stop all` to stop everything. Note that this will not delete the `otel-lgtm` container. To delete manually: `docker rm otel-lgtm`.
* `npx pm2 list` to see what's up.
