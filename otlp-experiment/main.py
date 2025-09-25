import asyncio
from time import sleep
from apscheduler.schedulers.asyncio import AsyncIOScheduler
from contextlib import asynccontextmanager
import logging
import random
from fastapi import FastAPI

from instrumentation import instrument_app, setup_instrumentation

APPLY_INSTRUMENTATION_PROGRAMMATICALLY = True

if APPLY_INSTRUMENTATION_PROGRAMMATICALLY:
    setup_instrumentation()

log = logging.getLogger("demo")

async def do_processing(seconds: int):
    for i in range(seconds):
        log.info(f"Processing for {i + 1} seconds...")
        await asyncio.sleep(1)
    log.info(f"Processing done.")

@asynccontextmanager
async def lifespan(app: FastAPI):
    log.info(f"Starting up...")
    
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
    
    log.info(f"Shutting down...")

app = FastAPI(lifespan=lifespan)

if APPLY_INSTRUMENTATION_PROGRAMMATICALLY:
    instrument_app(app)

@app.get("/")
def index():
    log.info(f"Handling request for index random={random.randint(1, 100)}")
    sleep(random.random()*2)
    if random.random() > 0.95:
        raise Exception("Random error!")
    
    return {"Hello": "World"}
