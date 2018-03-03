#!/usr/bin/env bash
mvn versions:set -DnewVersion=1.0-SNAPSHOT
mvn clean deploy
