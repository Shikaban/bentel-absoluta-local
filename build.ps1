# PowerShell script to compile and create the executable JAR
$ErrorActionPreference = "Stop"

if (Test-Path "build") { Remove-Item -Path "build" -Recurse -Force }
New-Item -ItemType Directory -Path "build" | Out-Null

$javaFiles = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac --release 19 -cp "lib/jars/*" -d build $javaFiles
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Get-ChildItem -Path src -Recurse -Filter *.properties | ForEach-Object {
    $dest = Join-Path "build" $_.FullName.Substring((Resolve-Path src).Path.Length + 1)
    $destDir = Split-Path $dest -Parent
    if (!(Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
    Copy-Item $_.FullName $dest -Force
}

"Main-Class: Application`nClass-Path: lib/*" | Set-Content MANIFEST.MF
jar cfm build/app.jar MANIFEST.MF -C build .
Remove-Item MANIFEST.MF

Write-Host "Build completed: build/app.jar"
