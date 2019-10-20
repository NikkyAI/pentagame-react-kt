#!/usr/bin/env bash
./gradlew clean client-jsTerseJs shadowJarServer && \
  rsync -Avr build/libs/penta-server.jar nikky@nikky.moe:pentagame/ && \
  rsync -Avr build/html/ nikky@nikky.moe:public_html/pentagame/
