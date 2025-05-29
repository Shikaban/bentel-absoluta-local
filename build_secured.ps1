# Script PowerShell per compilare e creare i JAR di cms, plugin e protocol nella cartella secured

# 1. Crea la cartella build se non esiste
if (!(Test-Path -Path "build")) {
    New-Item -ItemType Directory -Path "build"
}

# 2. Compila e crea cms.jar
$cmsJava = Get-ChildItem -Path src/cms -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "lib/jars/*;secured/*" -d build $cmsJava
jar cf secured/cms.jar -C build/cms .

# 3. Compila e crea plugin.jar
$pluginJava = Get-ChildItem -Path src/plugin -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "lib/jars/*;secured/*;build" -d build $pluginJava
jar cf secured/plugin.jar -C build/plugin .

# 4. Compila e crea protocol.jar
$protocolJava = Get-ChildItem -Path src/protocol -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -cp "lib/jars/*;secured/*;build" -d build $protocolJava
jar cf secured/protocol.jar -C build/protocol .

Write-Host "Compilazione e creazione dei JAR in secured/ completata."