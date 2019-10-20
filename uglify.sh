#!/usr/bin/env bash

./gradlew runDceClient-jsKotlin
OUTPUT=build/html/js/
rm -rf $OUTPUT
mkdir $OUTPUT
FILES=build/kotlin-js-min/client-js/main/*.js
#FILES=build/client-js/*.js
for f in $FILES
do
  if [[ "$f" == *.meta.js ]]; then continue; fi
  echo "Processing $f file..."
  filename=$(basename $f)
  terser $f --source-map "content='$f.map',url='$b.map'" -c -o $OUTPUT/$filename
done
du -h build/html/js
#rsync -Avr build/html/ nikky@shell.c-base.org:public_html/pentagame/
