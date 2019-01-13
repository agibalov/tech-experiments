#!/usr/bin/env bash
ansible-playbook -vvv -u root -i hosts deployment/$1.yml
