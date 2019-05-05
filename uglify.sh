#!/usr/bin/env bash
#./gradlew packageJs && \
uglifyjs `ls build/kotlin-js-min/js/main/*.js` -o build/ugly.js
#rsync -Avr build/html/ nikky@shell.c-base.org:public_html/pentagame/
