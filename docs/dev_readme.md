# Local communication with Absoluta

Using the ITv2 protocol, the bridge communicates with the panel and receives
the state of zones, partitions, and the alarm system. It can also arm or disarm
the security system.

## Folder structure

- `src`: source code
- `lib`: libraries

## Create the Docker container image

Run the build script to create the Docker image. It compiles the source code
and prepares the Java application.

### Requirements

- Java JDK 19 or newer
- Docker
- Docker Compose
- A terminal opened in the project folder

In PowerShell:

```powershell
.\build.ps1
```

In a Linux or macOS terminal:

```bash
chmod +x build.sh
./build.sh
```

After creating the application JAR, build the Docker image:

```bash
docker build -t bentel-absoluta . --no-cache
```

To export the image to a TAR file:

```bash
docker save -o bentel-absoluta.tar bentel-absoluta:latest
```

## Run the container

Edit `docker-compose.yml` to set the required environment variables.

To start the container:

```bash
docker-compose up
```
