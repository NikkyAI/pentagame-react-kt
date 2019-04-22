#!/usr/bin/env bash
./gradlew packageJs && rsync -Avr build/html/ nikky@shell.c-base.org:public_html/pentagame/
