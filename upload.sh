#!/usr/bin/env bash

./gradlew frontend:clean frontend:browserWebpack \
&& rsync -va frontend/build/distributions/* -e ssh shell.c-base.org:~/public_html/pentagame_dev/

