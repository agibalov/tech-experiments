# seq-experiment

While structured logging is the industry standard these days, there are almost not tools that allow developers to analyze their app's logs during local development. While local ELK installation is sure an option, it may not be the most convenient choice. [Seq](https://datalust.co/seq) is a lightweight alternative to the ELK.

### Test app

There's a dummy Java app that produces some logs:

* Go to http://localhost:8080/hello (always success)
* Go to http://localhost:8080/crash (always 500)
* Also, an asyn task runs every 1 s and randomly succeeds or fails

### Launch Seq using Docker Compose

* `docker-compose up --build --no-start seq` to build container.
* `docker-compose start seq` to start container in the background.
* `docker-compose stop seq` to stop container.

Seq UI will be available at `http://localhost:180` and ingestion endpoint at `http://localhost:15341`.

### Experiment 1: custom Seq appender for Logback

I've built a custom Logback appender inspired by [seq-logback-appender](https://github.com/sswayney/seq-logback-appender). It's a very native synchronous implementation, and while it works, I didn't check the performance impact.

Launch the app like this:

```
SPRING_PROFILES_ACTIVE=seq-appender-logging \
./gradlew clean bootRun
```

### Experiment 2: jsonl log output + seqcli

`seqcli` is a CLI client for Seq. One of the cool features it has is the ability to ingest JSONL from stdin and send it to the Seq server. 

Looks like there's no official installation one-liner, so:
* Go to [Releases](https://github.com/datalust/seqcli/releases) and pick the appropriate version ([v2021.1.439](https://github.com/datalust/seqcli/releases/tag/v2021.1.439))
* Extract somewhere
* Add to PATH
* Make sure `seqcli` is now available

Launch the app like this:

```
SPRING_PROFILES_ACTIVE=console-logging \
./gradlew clean bootRun | \
seqcli ingest \
  --server=http://localhost:15341 \
  --invalid-data=ignore \
  --json
```
