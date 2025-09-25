# otel-experiment

This is a demo/test stand that I use to learn OpenTelemetry. It consists of:

* An app (Python, uvicorn, FastAPI, APScheduler) with intentional delays and occasional failures (experiment-app)
* A pair of dummy web clients (hey)
* A telemetry web UI (otel-lgtm)

![](masala.drawio.svg)

The idea is to launch all this, and then see what's happening in Grafana (otel-lgtm).

## Prerequisites

* Docker and Docker Compose working without root.
* NodeJS
* Python
* uv
* hey

## How to do things

* `npx pm2 start` to start everything.

  The 2 endpoints of dummy web app are:
  
  * http://localhost:8000
  * http://localhost:8000/hello
  
  Grafana: http://localhost:3000

* `npx pm2 stop all` to stop everything. Note that this will not delete the `otel-lgtm` container. To delete manually: `docker rm otel-lgtm`.
* `npx pm2 list` to see what's up.
