# Build From Source

## Prerequisites

- Java JDK 19+
- PowerShell (Windows) or Bash (Linux/macOS)
- Docker

## Steps

1. Compile the application, including the protocol and panel modules:

```bash
# Linux/macOS
./build.sh

# Windows
.\build.ps1
```

2. Build the Docker image:

```bash
docker build -t bentel-absoluta-local-bridge .
```
