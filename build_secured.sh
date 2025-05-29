#!/bin/bash
# Script per compilare e creare i JAR di cms, plugin e protocol nella cartella secured (versione Unix)

# 1. Crea la cartella build se non esiste
mkdir -p build

# 2. Compila e crea cms.jar
find src/cms -name "*.java" > cms_sources.txt
javac -cp "lib/jars/*:secured/*" -d build @cms_sources.txt
jar cf secured/cms.jar -C build/cms .
rm cms_sources.txt

# 3. Compila e crea plugin.jar
find src/plugin -name "*.java" > plugin_sources.txt
javac -cp "lib/jars/*:secured/*:build" -d build @plugin_sources.txt
jar cf secured/plugin.jar -C build/plugin .
rm plugin_sources.txt

# 4. Compila e crea protocol.jar
find src/protocol -name "*.java" > protocol_sources.txt
javac -cp "lib/jars/*:secured/*:build" -d build @protocol_sources.txt
jar cf secured/protocol.jar -C build/protocol .
rm protocol_sources.txt

echo "Compilazione e creazione dei JAR in secured/ completata."
