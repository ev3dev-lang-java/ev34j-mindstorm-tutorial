#!/bin/sh

kill $(ps aux | grep 'java' | awk '{print $2}')