from logging.config import dictConfig
import structlog
import logging
import sys

def init_logging():
    structlog.configure(
        processors=[
            structlog.contextvars.merge_contextvars,
            structlog.processors.StackInfoRenderer(),
            structlog.processors.dict_tracebacks,
            structlog.stdlib.render_to_log_kwargs,
        ],
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=structlog.make_filtering_bound_logger(logging.DEBUG),
        cache_logger_on_first_use=True,
    )

    dictConfig({
        "version": 1,
        "formatters": {
            "json": {
                "()": "pythonjsonlogger.jsonlogger.JsonFormatter",
                "format": "{asctime} {levelname} {name} {message}",
                "style": "{",
                "datefmt": "%Y-%m-%dT%H:%M:%S",
                "rename_fields": {
                    "asctime": "timestamp",
                    "levelname": "level",
                }
            }
        },
        "handlers": {
            "default": {
                "level": "INFO",
                "class": "logging.StreamHandler",
                "stream": sys.stdout,
                "formatter": "json",
            },
        },
        "root": {"level": "INFO", "handlers": ["default"]},
        "loggers": {
            "uvicorn":         {"level": "INFO", "propagate": True},
            "uvicorn.error":   {"level": "INFO", "propagate": True},
            "uvicorn.access":  {"level": "INFO", "propagate": True},
            "fastapi":         {"level": "INFO", "propagate": True},
            "starlette":       {"level": "INFO", "propagate": True},
            "apscheduler":     {"level": "INFO", "propagate": True},
        },
    })
