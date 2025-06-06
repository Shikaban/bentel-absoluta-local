#!/bin/bash
# Script per compilare e creare il JAR eseguibile su Linux

# 1. Crea la cartella build se non esiste
mkdir -p build

# 2. Compila tutti i file .java (output in build/)
find src -name "*.java" > sources.txt
javac --release 21 -cp "lib/jars/*:secured/*" -d build @sources.txt
rm sources.txt

# 3. Crea il file MANIFEST.MF per specificare la Main-Class
echo "Main-Class: Application" > build/MANIFEST.MF
echo "Class-Path: lib/*" >> build/MANIFEST.MF

# 4. Crea il JAR eseguibile
jar cfm build/app.jar build/MANIFEST.MF -C build .

echo "Compilazione e creazione JAR completata. Il file build/app.jar è pronto."
