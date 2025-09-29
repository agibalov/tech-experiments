import asyncio
from time import sleep
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from contextlib import asynccontextmanager
import random
from fastapi import FastAPI
from opentelemetry import trace, metrics

from log import init_logging

import structlog

init_logging()
log: structlog.stdlib.BoundLogger = structlog.get_logger()
tracer = trace.get_tracer(__name__)
meter = metrics.get_meter("experiment-app")
request_counter = meter.create_counter("app.my.request.count", unit="1")

async def do_processing(seconds: int):
    with tracer.start_as_current_span("do_processing") as root_span:
        root_span.set_attribute("do_processing.seconds", seconds)
        try:
            with tracer.start_as_current_span("preprocessing") as pre_span:
                pre_span.set_attribute("preprocessing.some_info", "this has been set in preprocessing")
                log.info("Preprocessing...")
                await asyncio.sleep(0.1)

            with tracer.start_as_current_span("processing") as proc_span:
                for i in range(seconds):
                    if random.random() > 0.95:
                        raise Exception("Some random error during processing!")

                    proc_span.add_event("processing_happening", {"second": i + 1})
                    log.info(f"Processing for {i + 1} seconds...", second=i + 1)
                    await asyncio.sleep(1)

            with tracer.start_as_current_span("postprocessing") as post_span:
                post_span.set_attribute("postprocessing.some_info", "this has been set in postprocessing")
                log.info("Postprocessing...")
                await asyncio.sleep(0.1)

            log.info(f"Processing done.")
        except Exception as ex:
            root_span.record_exception(ex)
            root_span.set_status(trace.status.Status(trace.status.StatusCode.ERROR, str(ex)))
            log.exception("do_processing failed")
            raise


@asynccontextmanager
async def lifespan(app: FastAPI):
    log.info("Starting up...")

    scheduler = AsyncIOScheduler(
        job_defaults={
            'coalesce': True,
            'max_instances': 1
        })
    scheduler.add_job(
        do_processing,
        kwargs={'seconds': 5},
        trigger='interval',
        seconds=10)
    scheduler.start()
    app.state.scheduler = scheduler

    yield

    log.info("Shutting down...")

app = FastAPI(lifespan=lifespan)

@app.get("/")
def index():
    r = random.randint(1, 100)
    log.info("Handling request for index", r=r)

    request_counter.add(1, {"my.handler": "index"})

    sleep(random.random()*2)

    if random.random() > 0.95:
        raise Exception("Random error!")

    return {"Hello": "World"}

@app.get("/hello")
def hello():
    r = random.randint(1, 100)
    log.info("Handling request for hello", r=r)

    request_counter.add(1, {"my.handler": "hello"})

    sleep(random.random()*5)

    if random.random() > 0.90:
        raise Exception("Random error!")

    return {"Hello": "World"}
