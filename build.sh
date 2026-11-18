#!/bin/bash
set -e

mkdir -p build

find src -name "*.java" > sources.txt
javac --release 19 -cp "lib/jars/*" -d build @sources.txt
rm sources.txt

find src -name "*.properties" | while read f; do
    dest="build/${f#src/}"
    mkdir -p "$(dirname "$dest")"
    cp "$f" "$dest"
done

printf "Main-Class: Application\nClass-Path: lib/*\n" > MANIFEST.MF
jar cfm build/app.jar MANIFEST.MF -C build .
rm MANIFEST.MF

echo "Build completed: build/app.jar"
