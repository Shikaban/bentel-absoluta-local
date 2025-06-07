# Script PowerShell per compilare e creare i JAR di cms, plugin e protocol nella cartella secured

# 1. Crea la cartella build se non esiste
if (!(Test-Path -Path "build")) {
    New-Item -ItemType Directory -Path "build"
}

# 2. Compila i sorgenti
$allJava = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac --release 19 -cp "lib/jars/*;secured/*" -d build $allJava

# 3. Crea i JAR
jar cf secured/cms.jar -C build/cms .
jar cf secured/plugin.jar -C build/plugin .
jar cf secured/protocol.jar -C build/protocol .

# 4. Copia i JAR anche nella cartella dell'addon
Copy-Item -Path secured/cms.jar -Destination "absoluta-addon/secured/cms.jar"
Copy-Item -Path secured/plugin.jar -Destination "absoluta-addon/secured/plugin.jar"
Copy-Item -Path secured/protocol.jar -Destination "absoluta-addon/secured/protocol.jar"

Write-Host "Compilazione e creazione dei JAR in secured/ completata."