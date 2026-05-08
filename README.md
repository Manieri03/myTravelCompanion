# myTravelCompanion

myTravelCompanion è un'applicazione Android sviluppata in Kotlin per il monitoraggio e la gestione dei propri viaggi.

L'app permette di creare viaggi, registrare percorsi, salvare punti di interesse, aggiungere note e associare foto ai luoghi visitati. Il progetto integra mappe, localizzazione, persistenza locale dei dati e notifiche basate sulla posizione.

Il progetto è stato realizzato per il corso di **Laboratorio di Applicazioni Mobili**.

## Funzionalità principali

- Creazione e gestione di viaggi
- Classificazione dei viaggi per tipologia
- Salvataggio delle date di inizio e fine viaggio
- Monitoraggio dei percorsi tramite localizzazione
- Registrazione di tragitti con durata e distanza
- Visualizzazione dei viaggi su mappa
- Salvataggio di punti di interesse
- Associazione di note ai punti salvati
- Associazione di foto ai punti visitati
- Galleria delle immagini salvate durante i viaggi
- Visualizzazione dello storico dei viaggi
- Statistiche e riepiloghi grafici
- Notifiche relative ai viaggi e ai punti di interesse
- Geofencing per ricevere avvisi in prossimità di luoghi salvati
- Persistenza locale dei dati tramite Room Database

## Obiettivo del progetto

L'obiettivo di myTravelCompanion è offrire uno strumento mobile per accompagnare l'utente durante l'esperienza di viaggio.

L'applicazione consente non solo di pianificare e archiviare un viaggio, ma anche di costruire una memoria digitale dell'esperienza, combinando percorsi, luoghi, fotografie e annotazioni personali.

## Tecnologie utilizzate

### Linguaggio e piattaforma

- Kotlin
- Android
- Gradle Kotlin DSL

### Interfaccia utente

- Jetpack Compose
- Material 3
- Navigation Compose

### Persistenza dati

- Room Database
- Room DAO
- Type Converters
- SharedPreferences

### Mappe e localizzazione

- Google Maps SDK
- Maps Compose
- Google Play Services Location
- Maps Utils KTX
- Geofencing

### Gestione immagini

- Coil Compose
- FileProvider
- Camera API

### Background task e notifiche

- WorkManager
- NotificationChannel
- Foreground Service
- BroadcastReceiver

## Architettura del progetto

Il progetto segue una struttura modulare, separando dati, UI, navigazione, servizi e logica applicativa.

```text
myTravelCompanion/
│
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/mytravelcompanion/
│   │       │   ├── data/          # Entity, DAO, database e converter
│   │       │   ├── navigation/    # Gestione della navigazione
│   │       │   ├── services/      # Servizi e receiver
│   │       │   ├── ui/            # Componenti, schermate e tema
│   │       │   ├── util/          # Utility e funzioni di supporto
│   │       │   ├── viewmodel/     # ViewModel dell'applicazione
│   │       │   ├── workers/       # Worker in background
│   │       │   └── MainActivity.kt
│   │       │
│   │       ├── res/               # Risorse Android
│   │       └── AndroidManifest.xml
│   │
│   ├── build.gradle.kts
│   └── proguard-rules.pro
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── .gitignore
