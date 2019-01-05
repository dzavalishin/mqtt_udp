#!/bin/sh
tcpdump -i eno1 'udp and port 1883'
