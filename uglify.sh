#!/usr/bin/env bash
#./gradlew packageJs && \
uglifyjs `ls build/kotlin-js-min/client-js/main/*.js` -o build/html/ugly.js

rootDir=$(pwd)

rm -rf build/html/uglify-js/
mkdir build/html/uglify-js/
FILES=build/kotlin-js-min/client-js/main/*.js
for f in $FILES
do
  echo "Processing $f file..."
  b=$(basename $f)
  DIR=$(dirname "$f}")
  cd $DIR
#  uglifyjs $b -o $PWD/build/html/uglify-js/$b
  uglifyjs $b --source-map "filename=build/kotlin-js-min/client-js/main/$b.map" -o $rootDir/build/html/uglify-js/$b
  cd $rootDir
done
#rsync -Avr build/html/ nikky@shell.c-base.org:public_html/pentagame/
