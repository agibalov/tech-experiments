# flutter-experiment

A Flutter hello world.

## Prerequisites

* [Flutter](https://docs.flutter.dev/get-started/install/linux#install-flutter-using-snapd)
* Docker Compose

## How to run

* `flutter run --device-id chrome` to run as web app.
* `flutter run --device-id linux` to run as Linux desktop app.
* `flutter build web` to build for web. Results will appear under `/build/web`. Run with `docker-compose up --build`.
* `flutter build linux` to build for Linux. Run with `./build/linux/x64/release/bundle/flutter_experiment`.
* `flutter test` to run tests.
