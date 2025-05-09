package de.pexora.proxy.config;

import com.moandjiezana.toml.Toml;
import de.pexora.proxy.PexoraProxy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Verwaltet die Hauptkonfiguration des Plugins
 */
public class CoreConfig {

    private final PexoraProxy plugin;
    private final Path configPath;
    private Toml config;

    private boolean databaseEnabled;
    private String databaseHost;
    private int databasePort;
    private String databaseName;
    private String databaseUser;
    private String databasePassword;
    private int databasePoolSize;
    private int databasePoolMinSize;
    private int databasePoolMaxSize;
    private long databasePoolIdleTimeout;
    private long databasePoolMaxLifetime;
    private boolean databaseAutoCreateTables;
    private boolean debugMode;

    private boolean moduleAutoReload;
    private boolean moduleAutoCopy;
    private boolean deleteOldPlugins;
    private boolean allowNonPexoraModules;
    private String[] additionalModules;

    /**
     * Erstellt eine neue Konfigurationsinstanz
     *
     * @param plugin Die Plugin-Instanz
     */
    public CoreConfig(PexoraProxy plugin) {
        this.plugin = plugin;
        this.configPath = plugin.getDataDirectory().resolve("config.toml");
        
        createDefault();
        load();
    }

    /**
     * Erstellt die Standardkonfiguration, falls sie nicht existiert
     */
    private void createDefault() {
        if (Files.exists(configPath)) {
            return;
        }

        plugin.getLoggerService().info("Erstelle Standardkonfiguration...");

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("config.toml")) {
            Files.createDirectories(configPath.getParent());
            Files.copy(is, configPath);
        } catch (IOException e) {
            plugin.getLoggerService().severe("Konnte Standardkonfiguration nicht erstellen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Konfiguration
     */
    private void load() {
        plugin.getLoggerService().info("Lade Konfiguration...");

        try {
            this.config = new Toml().read(configPath.toFile());
            
            // Datenbankeinstellungen
            Toml dbConfig = config.getTable("database");
            this.databaseEnabled = dbConfig.getBoolean("enabled");
            this.databaseHost = dbConfig.getString("host");
            this.databasePort = dbConfig.getLong("port").intValue();
            this.databaseName = dbConfig.getString("database");
            this.databaseUser = dbConfig.getString("username");
            this.databasePassword = dbConfig.getString("password");
            this.databasePoolSize = dbConfig.getLong("pool-size").intValue();
            
            // Erweiterte Datenbankeinstellungen
            this.databasePoolMinSize = dbConfig.getLong("pool-min-size", 5L).intValue();
            this.databasePoolMaxSize = dbConfig.getLong("pool-max-size", 10L).intValue();
            this.databasePoolIdleTimeout = dbConfig.getLong("pool-idle-timeout", 600000L);
            this.databasePoolMaxLifetime = dbConfig.getLong("pool-max-lifetime", 1800000L);
            this.databaseAutoCreateTables = dbConfig.getBoolean("auto-create-tables", true);
            
            // Debug-Einstellungen
            Toml debugConfig = config.getTable("debug");
            if (debugConfig != null) {
                this.debugMode = debugConfig.getBoolean("enabled", false);
            }
            
            // Moduleinstellungen
            Toml moduleConfig = config.getTable("modules");
            this.moduleAutoReload = moduleConfig.getBoolean("auto-reload");
            this.moduleAutoCopy = moduleConfig.getBoolean("auto-copy-to-modules");
            this.deleteOldPlugins = moduleConfig.getBoolean("delete-old-plugins");
            this.allowNonPexoraModules = moduleConfig.getBoolean("allow-non-pexora-modules");
            
            // Zusätzliche Module
            Object additionalModulesObj = moduleConfig.getList("additional-modules");
            if (additionalModulesObj instanceof java.util.List) {
                java.util.List<?> list = (java.util.List<?>) additionalModulesObj;
                this.additionalModules = list.toArray(new String[0]);
            } else {
                this.additionalModules = new String[0];
            }
            
            plugin.getLoggerService().info("Konfiguration erfolgreich geladen!");
        } catch (Exception e) {
            plugin.getLoggerService().severe("Fehler beim Laden der Konfiguration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Konfiguration neu
     */
    public void reload() {
        load();
    }

    /**
     * @return Ob die Datenbankverbindung aktiviert ist
     */
    public boolean isDatabaseEnabled() {
        return databaseEnabled;
    }

    /**
     * @return Der Datenbank-Host
     */
    public String getDatabaseHost() {
        return databaseHost;
    }

    /**
     * @return Der Datenbank-Port
     */
    public int getDatabasePort() {
        return databasePort;
    }

    /**
     * @return Der Datenbank-Name
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return Der Datenbank-Benutzer
     */
    public String getDatabaseUser() {
        return databaseUser;
    }

    /**
     * @return Das Datenbank-Passwort
     */
    public String getDatabasePassword() {
        return databasePassword;
    }

    /**
     * @return Die Größe des Verbindungspools
     */
    public int getDatabasePoolSize() {
        return databasePoolSize;
    }

    /**
     * @return Ob Module automatisch neu geladen werden sollen
     */
    public boolean isModuleAutoReload() {
        return moduleAutoReload;
    }

    /**
     * @return Ob Plugins automatisch in den Modules-Ordner kopiert werden sollen
     */
    public boolean isModuleAutoCopy() {
        return moduleAutoCopy;
    }

    /**
     * @return Ob alte Plugin-Versionen gelöscht werden sollen
     */
    public boolean isDeleteOldPlugins() {
        return deleteOldPlugins;
    }

    /**
     * @return Ob auch Nicht-Pexora-Module geladen werden sollen
     */
    public boolean isAllowNonPexoraModules() {
        return allowNonPexoraModules;
    }

    /**
     * @return Liste zusätzlicher Module
     */
    public String[] getAdditionalModules() {
        return additionalModules;
    }
    
    /**
     * @return Die minimale Größe des Datenbankpools
     */
    public int getDatabasePoolMinSize() {
        return databasePoolMinSize;
    }
    
    /**
     * @return Die maximale Größe des Datenbankpools
     */
    public int getDatabasePoolMaxSize() {
        return databasePoolMaxSize;
    }
    
    /**
     * @return Die Idle-Timeout-Zeit für Datenbankverbindungen in ms
     */
    public long getDatabasePoolIdleTimeout() {
        return databasePoolIdleTimeout;
    }
    
    /**
     * @return Die maximale Lebensdauer einer Datenbankverbindung in ms
     */
    public long getDatabasePoolMaxLifetime() {
        return databasePoolMaxLifetime;
    }
    
    /**
     * @return Ob Tabellen automatisch erstellt werden sollen
     */
    public boolean isDatabaseAutoCreateTables() {
        return databaseAutoCreateTables;
    }
    
    /**
     * @return Ob der Debug-Modus aktiviert ist
     */
    public boolean isDebugMode() {
        return debugMode;
    }
}