# PexoraProxy

Ein modulares Velocity-Proxy-Plugin-System, das zentralisierte Modulverwaltung, Konfiguration, Messaging und API-Funktionalität für Velocity-Proxy-Server bietet.

## Übersicht

PexoraProxy ist ein modulares Plugin-System für Velocity-Proxy-Server, das folgende Funktionen bietet:

- Automatisches Laden und Verwalten von Modul-Plugins
- Zentralisierte Konfigurationsverwaltung
- Integrierte Datenbankunterstützung (MySQL)
- Server-übergreifende Kommunikation und Handshakes
- Statusüberwachung aller Module und Backend-Server
- Spielerdatenverwaltung und -tracking

## Funktionen

### 📁 Modul-System (ModuleLoader)
- Lädt automatisch alle Module aus dem Unterverzeichnis `/modules`
- Unterstützt Subplugins im Format PexoraXYZ.jar
- Jedes Modul wird wie ein Plugin behandelt
- Abhängigkeit zu PexoraProxy wird geprüft

### 💬 MessageConfig (messages.yml)
- Lädt benutzerdefinierte Nachrichten (Fehler, Systemmeldungen)
- Ersetzt Platzhalter wie %prefix%
- Nutzt MiniMessage (Adventure) zur Farb- und Formatkontrolle
- Unterstützt Live-Aktualisierung für Updates

### ⚙️ CoreConfig (config.yml)
- Zentrale Konfigurationsdatei für systemweite Optionen
- Konfigurierbarer Debug-Modus
- Modul-Auto-Reload-Option
- Datenbankeinstellungen konfigurierbar
- Live aktualisierbar

### 🗃️ Datenbankanbindung
- HikariCP-Verbindungspool für optimale Performance
- Automatische Tabellenerstellung
- Spieler-Tracking: Login/Logout, Server-Wechsel
- Server-Status-Erfassung
- Modul-Status und -Verwaltung

### 🔗 Messaging-System (MessagingManager)
- Benutzerdefinierter Messaging-Kanal (pexora:core)
- Bidirektionale Kommunikation zwischen Proxy und Servern
- Unterstützt Authentifizierung & Status-Updates
- Nachrichtentypen und -handler erweiterbar

### 📡 StatusAPI
- Registriert alle laufenden Module im Speicher
- Verwaltet Karte mit Ladezuständen
- Überwacht Server-Status und Verbindungen
- Speichert Daten in der Datenbank

### 🎨 [PX]-Prefix
- Anpassbar über messages.yml
- Standard: `<gradient:#ff55ff:#aa00ff>[PX]</gradient>`
- Kann in jeder Nachricht mit %prefix% verwendet werden
- Verfügbar als Component

### 🧾 Logging / LoggerService
- Saubere Ausgaben mit [PX]-Prefix im Log
- Unterstützt: INFO, WARN, ERROR
- Optional farbig im Konsolelog

### 🧠 API für andere Pexora-Plugins
- `PexoraProxyAPI.get()` liefert Singleton-Instanz
- Bietet Zugriff auf:
  - Konfigurationen
  - Logger
  - Datenbankmanager
  - Registrierte Module
  - Nachrichtenkomponenten
  - Messaging-System

### 🧪 Debug-Modus
- Konfigurierbar über config.yml
- Aktiviert zusätzliche Konsolenlogs
- Hilfreich für Entwicklung und Fehlersuche

## Installation

1. Lade die neueste PexoraProxy.jar von der Releases-Seite herunter
2. Platziere sie im plugins-Verzeichnis deines Velocity-Servers
3. Starte deinen Server - dies generiert die Konfigurationsdateien
4. Passe deine Konfiguration nach Bedarf an
5. Platziere Modul-Plugins im Verzeichnis `plugins/PexoraProxy/modules`

## Datenbankeinrichtung

1. Erstelle eine MySQL-Datenbank für PexoraProxy
2. Konfiguriere die Datenbankeinstellungen in der config.yml:
   ```yaml
   database:
     enabled: true
     type: "mysql"
     host: "localhost"
     port: 3306
     name: "deine_datenbank"
     user: "dein_benutzer"
     password: "dein_passwort"
   ```
3. Starte den Server neu oder verwende den Befehl `/pexora reload`
4. PexoraProxy erstellt automatisch alle notwendigen Tabellen

## Erstellen eines Moduls

Module sind Plugins, die von PexoraProxy abhängen. Um ein Modul zu erstellen:

1. Erstelle ein Standard-Velocity-Plugin
2. Mache PexoraProxy zu einer Abhängigkeit, indem du es zu deiner velocity-plugin.json hinzufügst:
   ```json
   "dependencies": [
     {
       "id": "pexoraproxy",
       "optional": false
     }
   ]
   ```
3. Benenne dein Plugin mit dem "Pexora"-Präfix (z.B. PexoraChat)
4. Verwende die PexoraProxyAPI in deinem Plugin:
   ```java
   import de.pexora.proxy.api.PexoraProxyAPI;
   
   // API-Instanz holen
   PexoraProxyAPI api = PexoraProxyAPI.get();
   
   // Eine Nachricht loggen
   api.info("Hallo von meinem Modul!");
   
   // Datenbankzugriff
   try (Connection conn = api.getDatabaseManager().getConnection()) {
       // Datenbankoperationen
   }
   ```

## Befehle

- `/pexora reload` - Lädt das Plugin und alle Module neu
- `/pexora status` - Zeigt den Status des Plugins und aller Module
- `/pexora help` - Zeigt das Hilfemenü

## Berechtigungen

- `pexora.admin` - Erlaubt Zugriff auf alle PexoraProxy-Befehle

## Kommunikation mit PexoraCore (Spigot)

PexoraProxy kann mit dem PexoraCore-Plugin auf deinen Spigot-Servern kommunizieren:

1. Verwende dasselbe Messaging-Kanal in beiden Konfigurationen
2. Die Server authentifizieren sich automatisch beim Proxy
3. Status- und Spielerdaten werden synchronisiert
4. Plugins können benutzerdefinierte Nachrichten senden

## Module finden

Schau im Verzeichnis `example-modules` für Beispielmodule, die PexoraProxy verwenden.