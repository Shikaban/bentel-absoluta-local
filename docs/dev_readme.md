# Comunicazione locale con absoluta

Utilizzando il protocollo ITv2 comunichiamo con la centrale e riceviamo lo
stato dei sensori, delle partizioni e dell'allarme. Possiamo inoltre armare o
disattivare l'antifurto.

## Struttura folder

- `src`: contenente il codice sorgente
- `lib`: contenente le librerie utilizzate

## Creare immagine container docker

Per creare l'immagine Docker, eseguire lo script di build. Lo script compila il
codice sorgente e prepara l'applicazione Java.

### Requisiti

- Java JDK 19 o superiore
- Docker installato
- Docker Compose installato
- Terminale aperto nella cartella del progetto

In PowerShell:

```powershell
.\build.ps1
```

Nel terminale Linux o macOS:

```bash
chmod +x build.sh
./build.sh
```

Dopo aver creato il JAR dell'applicazione, creare l'immagine Docker:

```bash
docker build -t bentel-absoluta . --no-cache
```

Per esportare l'immagine in un file tar:

```bash
docker save -o bentel-absoluta.tar bentel-absoluta:latest
```

## Eseguire il container

Modificare `docker-compose.yml` per impostare le variabili d'ambiente
necessarie.

Per avviare il container:

```bash
docker-compose up
```
