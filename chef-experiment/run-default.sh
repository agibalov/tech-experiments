#!/usr/bin/env bash
chef-client --local-mode --override-runlist first_cookbook -l debug
