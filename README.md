## Comunicazione locale con absoluta

Utilizzando il protocollo ITv2 comunichiamo con la centrale e riceviamo lo stato dei sensori, delle partizioni e dell'allarme, in più possiamo armare o disattivare l'antifurto

## Struttura folder

- `src`: contenente il codice sorgente
- `lib`: contenente le librerie utilizzate



## Creare immagine container docker

Per creare l'immagine del container docker, è necessario eseguire lo script di build. Questo script compila il codice sorgente e prepara l'eseguibile java.

### Requisiti
- Java 24 installato
- Docker installato
- Docker Compose installato
- Terminale aperto nella cartella del progetto

In powershell:
```powershell
.\build_secured.ps1
.\build.ps1
```
Nel terminale linux o macOS:
```bash
chmod +x build.sh
chmod +x build_secured.sh
./build_secured.sh
./build.sh
```

Una volta creati i file jar, è possibile eseguire il comando per creare l'immagine del container docker:

```bash
docker build -t bentel-absoluta . --no-cache
```

A questo punto si può esportare l'immagine in un file tar per un uso successivo:

```bash
docker save -o bentel-absoluta.tar bentel-absoluta:latest
```

## Eseguire il container

Prima di tutto, modifica il file `docker-compose.yml` per impostare le variabili d'ambiente necessarie.

Per eseguire il container, puoi utilizzare il comando `docker-compose`:

```bash
docker-compose up
```



