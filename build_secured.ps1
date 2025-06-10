# Script PowerShell per compilare e creare i JAR di cms, plugin e protocol nella cartella secured

# 1. Crea la cartella build se non esiste
if (!(Test-Path -Path "build")) {
    New-Item -ItemType Directory -Path "build"
}

# 2. Compila i sorgenti
$allJava = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac --release 19 -cp "lib/jars/*;secured/*" -d build $allJava

# 3. Crea i JAR
jar cf secured/protocol.jar -C build/protocol .
jar cf secured/cms.jar -C build/cms .
jar cf secured/plugin.jar -C build/plugin .

Write-Host "Compilazione e creazione dei JAR in secured/ completata."
