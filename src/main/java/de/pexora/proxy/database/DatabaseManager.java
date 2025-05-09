package de.pexora.proxy.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.pexora.proxy.PexoraProxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Verwaltet die Datenbankverbindung und -operationen für PexoraProxy.
 */
public class DatabaseManager {

    private final PexoraProxy proxy;
    private HikariDataSource dataSource;
    private String lastHost;
    private String lastDatabase;
    private String lastUser;
    private String lastPassword;
    private int lastPort;
    
    public DatabaseManager(PexoraProxy proxy) {
        this.proxy = proxy;
    }
    
    /**
     * Stellt eine Verbindung zur Datenbank her
     * 
     * @return true, wenn die Verbindung erfolgreich hergestellt wurde
     */
    public boolean connect() {
        if (!proxy.getCoreConfig().isDatabaseEnabled()) {
            proxy.getLoggerService().warn("Datenbankverbindung ist in der Konfiguration deaktiviert.");
            return false;
        }
        
        try {
            // Konfigurationsdaten abrufen
            String host = proxy.getCoreConfig().getDatabaseHost();
            int port = proxy.getCoreConfig().getDatabasePort();
            String database = proxy.getCoreConfig().getDatabaseName();
            String user = proxy.getCoreConfig().getDatabaseUser();
            String password = proxy.getCoreConfig().getDatabasePassword();
            
            // Konfigurationswerte speichern für Reconnect-Überprüfung
            lastHost = host;
            lastPort = port;
            lastDatabase = database;
            lastUser = user;
            lastPassword = password;
            
            // HikariCP konfigurieren
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(user);
            config.setPassword(password);
            config.setPoolName("PexoraPool");
            
            // Verbindungspool-Einstellungen
            config.setMinimumIdle(proxy.getCoreConfig().getDatabasePoolMinSize());
            config.setMaximumPoolSize(proxy.getCoreConfig().getDatabasePoolMaxSize());
            config.setIdleTimeout(proxy.getCoreConfig().getDatabasePoolIdleTimeout());
            config.setMaxLifetime(proxy.getCoreConfig().getDatabasePoolMaxLifetime());
            
            // Weitere Einstellungen
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // DataSource erstellen
            dataSource = new HikariDataSource(config);
            
            // Testverbindung herstellen
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(5)) {
                    proxy.getLoggerService().info("Datenbankverbindung hergestellt zu: " + host + ":" + port + "/" + database);
                    
                    // Wenn automatische Tabellenerstellung aktiviert ist
                    if (proxy.getCoreConfig().isDatabaseAutoCreateTables()) {
                        createTables();
                    }
                    
                    return true;
                } else {
                    proxy.getLoggerService().error("Datenbankverbindung ungültig!");
                    return false;
                }
            } catch (SQLException e) {
                proxy.getLoggerService().error("Fehler beim Testen der Datenbankverbindung: " + e.getMessage());
                if (proxy.getCoreConfig().isDebugMode()) {
                    e.printStackTrace();
                }
                return false;
            }
        } catch (Exception e) {
            proxy.getLoggerService().error("Fehler beim Herstellen der Datenbankverbindung: " + e.getMessage());
            if (proxy.getCoreConfig().isDebugMode()) {
                e.printStackTrace();
            }
            return false;
        }
    }
    
    /**
     * Trennt die Verbindung zur Datenbank
     */
    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            proxy.getLoggerService().info("Datenbankverbindung getrennt.");
        }
    }
    
    /**
     * Stellt die Verbindung zur Datenbank neu her
     * 
     * @return true, wenn die Verbindung erfolgreich neu hergestellt wurde
     */
    public boolean reconnect() {
        disconnect();
        return connect();
    }
    
    /**
     * Prüft, ob eine Neuverbindung erforderlich ist (weil sich Konfigurationswerte geändert haben)
     * 
     * @return true, wenn eine Neuverbindung erforderlich ist
     */
    public boolean needsReconnect() {
        return !proxy.getCoreConfig().getDatabaseHost().equals(lastHost) ||
               proxy.getCoreConfig().getDatabasePort() != lastPort ||
               !proxy.getCoreConfig().getDatabaseName().equals(lastDatabase) ||
               !proxy.getCoreConfig().getDatabaseUser().equals(lastUser) ||
               !proxy.getCoreConfig().getDatabasePassword().equals(lastPassword);
    }
    
    /**
     * @return eine Verbindung aus dem Pool
     * @throws SQLException wenn ein Fehler auftritt
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            connect();
        }
        
        if (dataSource == null) {
            throw new SQLException("Keine Datenbankverbindung verfügbar!");
        }
        
        return dataSource.getConnection();
    }
    
    /**
     * @return ob eine Verbindung zur Datenbank besteht
     */
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
    
    /**
     * Erstellt die erforderlichen Tabellen in der Datenbank
     */
    private void createTables() {
        proxy.getLoggerService().info("Erstelle erforderliche Datenbanktabellen...");
        
        try (Connection conn = getConnection()) {
            // Tabelle für Module
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS pexora_modules (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(64) NOT NULL, " +
                    "version VARCHAR(32) NOT NULL, " +
                    "enabled BOOLEAN NOT NULL DEFAULT TRUE, " +
                    "load_time BIGINT NOT NULL, " +
                    "UNIQUE KEY unique_name (name)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            )) {
                stmt.executeUpdate();
            }
            
            // Tabelle für Spielerdaten
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS pexora_players (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "name VARCHAR(16) NOT NULL, " +
                    "first_join BIGINT NOT NULL, " +
                    "last_join BIGINT NOT NULL, " +
                    "last_server VARCHAR(64), " +
                    "UNIQUE KEY unique_uuid (uuid)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            )) {
                stmt.executeUpdate();
            }
            
            // Tabelle für Server
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS pexora_servers (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(64) NOT NULL, " +
                    "address VARCHAR(128) NOT NULL, " +
                    "status VARCHAR(16) NOT NULL, " +
                    "last_ping BIGINT, " +
                    "UNIQUE KEY unique_name (name)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;"
            )) {
                stmt.executeUpdate();
            }
            
            proxy.getLoggerService().info("Datenbanktabellen erfolgreich erstellt oder aktualisiert.");
        } catch (SQLException e) {
            proxy.getLoggerService().error("Fehler beim Erstellen der Datenbanktabellen: " + e.getMessage());
            if (proxy.getCoreConfig().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Aktualisiert den Status eines Moduls in der Datenbank
     * 
     * @param moduleName der Name des Moduls
     * @param version die Version des Moduls
     * @param enabled ob das Modul aktiviert ist
     */
    public void updateModuleStatus(String moduleName, String version, boolean enabled) {
        if (!isConnected()) {
            return;
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO pexora_modules (name, version, enabled, load_time) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE version = ?, enabled = ?, load_time = ?;"
             )) {
            
            long currentTime = System.currentTimeMillis();
            
            stmt.setString(1, moduleName);
            stmt.setString(2, version);
            stmt.setBoolean(3, enabled);
            stmt.setLong(4, currentTime);
            
            stmt.setString(5, version);
            stmt.setBoolean(6, enabled);
            stmt.setLong(7, currentTime);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            proxy.getLoggerService().error("Fehler beim Aktualisieren des Modulstatus: " + e.getMessage());
            if (proxy.getCoreConfig().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Aktualisiert den Status eines Servers in der Datenbank
     * 
     * @param serverName der Name des Servers
     * @param address die Adresse des Servers
     * @param status der Status des Servers (online, offline, etc.)
     */
    public void updateServerStatus(String serverName, String address, String status) {
        if (!isConnected()) {
            return;
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO pexora_servers (name, address, status, last_ping) " +
                     "VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE address = ?, status = ?, last_ping = ?;"
             )) {
            
            long currentTime = System.currentTimeMillis();
            
            stmt.setString(1, serverName);
            stmt.setString(2, address);
            stmt.setString(3, status);
            stmt.setLong(4, currentTime);
            
            stmt.setString(5, address);
            stmt.setString(6, status);
            stmt.setLong(7, currentTime);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            proxy.getLoggerService().error("Fehler beim Aktualisieren des Serverstatus: " + e.getMessage());
            if (proxy.getCoreConfig().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Aktualisiert die Spielerdaten in der Datenbank
     * 
     * @param uuid die UUID des Spielers
     * @param name der Name des Spielers
     * @param server der aktuelle Server des Spielers (kann null sein)
     * @param isJoining ob der Spieler gerade joined (true) oder disconnected (false)
     */
    public void updatePlayerData(String uuid, String name, String server, boolean isJoining) {
        if (!isConnected()) {
            return;
        }
        
        try (Connection conn = getConnection()) {
            if (isJoining) {
                // Spieler betritt den Proxy
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO pexora_players (uuid, name, first_join, last_join, last_server) " +
                        "VALUES (?, ?, ?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = ?, last_join = ?, last_server = ?;"
                )) {
                    
                    long currentTime = System.currentTimeMillis();
                    
                    stmt.setString(1, uuid);
                    stmt.setString(2, name);
                    stmt.setLong(3, currentTime); // first_join (für neue Spieler)
                    stmt.setLong(4, currentTime); // last_join
                    stmt.setString(5, server);
                    
                    stmt.setString(6, name);
                    stmt.setLong(7, currentTime);
                    stmt.setString(8, server);
                    
                    stmt.executeUpdate();
                }
            } else {
                // Spieler verlässt den Proxy, nur den letzten Server aktualisieren
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE pexora_players SET last_server = ? WHERE uuid = ?;"
                )) {
                    stmt.setString(1, server);
                    stmt.setString(2, uuid);
                    
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            proxy.getLoggerService().error("Fehler beim Aktualisieren der Spielerdaten: " + e.getMessage());
            if (proxy.getCoreConfig().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }
}