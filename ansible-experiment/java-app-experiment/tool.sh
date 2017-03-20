#!/usr/bin/env bash
ansible-playbook -u root -i hosts deployment/$1.yml
