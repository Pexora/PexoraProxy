package de.pexora.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.pexora.proxy.api.PexoraProxyAPI;
import de.pexora.proxy.commands.PexoraCommand;
import de.pexora.proxy.config.CoreConfig;
import de.pexora.proxy.config.MessageConfig;
import de.pexora.proxy.database.DatabaseManager;
import de.pexora.proxy.logger.LoggerService;
import de.pexora.proxy.messaging.MessagingManager;
import de.pexora.proxy.module.ModuleLoader;
import de.pexora.proxy.status.StatusAPI;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Hauptklasse für das PexoraProxy-Plugin.
 * Dieses Plugin dient als Kernsystem für ein modulares Proxy-Plugin-System.
 */
@Plugin(
    id = "pexoraproxy",
    name = "PexoraProxy",
    version = "1.0.0",
    description = "Core plugin für Velocity-Proxy mit modularem System und Datenbankunterstützung",
    authors = {"Pexora Development Team"}
)
public class PexoraProxy {

    private static PexoraProxy instance;
    
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    
    private LoggerService loggerService;
    private CoreConfig coreConfig;
    private MessageConfig messageConfig;
    private DatabaseManager databaseManager;
    private ModuleLoader moduleLoader;
    private StatusAPI statusAPI;
    private MessagingManager messagingManager;
    
    @Inject
    public PexoraProxy(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;
        
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }
    
    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Initialisiere Komponenten
        this.loggerService = new LoggerService(this);
        
        // Zeige PX-Logo in der Konsole
        this.loggerService.printLogo();
        
        this.loggerService.info("PexoraProxy wird initialisiert...");
        
        // Konfigurationen laden
        this.coreConfig = new CoreConfig(this);
        this.messageConfig = new MessageConfig(this);
        
        // Datenbankverbindung einrichten
        this.databaseManager = new DatabaseManager(this);
        
        if (this.coreConfig.isDatabaseEnabled()) {
            this.databaseManager.connect();
        }
        
        // Status-API initialisieren
        this.statusAPI = new StatusAPI();
        
        // Messaging-Manager initialisieren
        this.messagingManager = new MessagingManager(this);
        
        // Modul-Loader initialisieren
        this.moduleLoader = new ModuleLoader(this);
        this.loggerService.info("Module wurden geladen: " + this.moduleLoader.getModules().size());
        
        // API initialisieren
        PexoraProxyAPI.initialize(this);
        
        // Befehle registrieren
        registerCommands();
        
        this.loggerService.info("PexoraProxy wurde erfolgreich aktiviert!");
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        this.loggerService.info("PexoraProxy wird deaktiviert...");
        
        // Module entladen
        if (this.moduleLoader != null) {
            this.moduleLoader.disableAllModules();
        }
        
        // Datenbankverbindung schließen
        if (this.databaseManager != null && this.databaseManager.isConnected()) {
            this.databaseManager.disconnect();
        }
        
        // Messaging abmelden
        if (this.messagingManager != null) {
            this.messagingManager.unregister();
        }
        
        this.loggerService.info("PexoraProxy wurde deaktiviert!");
        instance = null;
    }
    
    /**
     * Registriert alle Befehle
     */
    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        
        // Registriere Hauptbefehl
        commandManager.register("pexoraproxy", new PexoraCommand(this));
        
        // Registriere Alias-Befehl
        commandManager.register("pexcore", new PexoraCommand(this));
    }
    
    /**
     * Lädt das Plugin neu
     */
    public void reload() {
        this.loggerService.info("PexoraProxy wird neu geladen...");
        
        // Konfigurationen neu laden
        this.coreConfig.reload();
        this.messageConfig.reload();
        
        // Datenbankverbindung aktualisieren
        if (this.coreConfig.isDatabaseEnabled()) {
            if (!this.databaseManager.isConnected()) {
                this.databaseManager.connect();
            } else if (this.databaseManager.needsReconnect()) {
                this.databaseManager.reconnect();
            }
        } else if (this.databaseManager.isConnected()) {
            this.databaseManager.disconnect();
        }
        
        // Module neu laden, wenn Auto-Reload aktiviert ist
        if (this.coreConfig.isModuleAutoReload()) {
            this.moduleLoader.reloadAllModules();
        }
        
        this.loggerService.info("PexoraProxy wurde neu geladen!");
    }
    
    /**
     * @return die Proxy-Server-Instanz
     */
    public ProxyServer getServer() {
        return server;
    }
    
    /**
     * @return den Logger
     */
    public Logger getSLF4JLogger() {
        return logger;
    }
    
    /**
     * @return das Datenverzeichnis
     */
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    /**
     * @return den Logger-Service
     */
    public LoggerService getLoggerService() {
        return loggerService;
    }
    
    /**
     * @return die Kern-Konfiguration
     */
    public CoreConfig getCoreConfig() {
        return coreConfig;
    }
    
    /**
     * @return die Nachrichten-Konfiguration
     */
    public MessageConfig getMessageConfig() {
        return messageConfig;
    }
    
    /**
     * @return den Datenbank-Manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * @return den Modul-Loader
     */
    public ModuleLoader getModuleLoader() {
        return moduleLoader;
    }
    
    /**
     * @return die Status-API
     */
    public StatusAPI getStatusAPI() {
        return statusAPI;
    }
    
    /**
     * @return den Messaging-Manager
     */
    public MessagingManager getMessagingManager() {
        return messagingManager;
    }
    
    /**
     * @return die Instanz des Plugins
     */
    public static PexoraProxy getInstance() {
        return instance;
    }
}