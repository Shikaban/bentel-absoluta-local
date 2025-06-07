# Script PowerShell per compilare e creare il JAR eseguibile

# 1. Crea la cartella build se non esiste
if (!(Test-Path -Path "build")) {
    New-Item -ItemType Directory -Path "build"
}

# 2. Compila tutti i file .java (output in build/)
$javaFiles = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac --release 19 -cp "lib/jars/*;secured/*" -d build $javaFiles

# 3. Crea il file MANIFEST.MF per specificare la Main-Class
@"
Main-Class: Application
Class-Path: lib/*
"@ | Set-Content build/MANIFEST.MF

# 4. Crea il JAR eseguibile
jar cfm build/app.jar build/MANIFEST.MF -C build .

# 5. Copia il JAR anche nella cartella dell'addon
Copy-Item -Path build/app.jar -Destination "absoluta-addon/app.jar" 

Write-Host "Compilazione e creazione JAR completata. Il file app.jar è pronto."
