package de.pexora.proxy.api;

import de.pexora.proxy.PexoraProxy;
import de.pexora.proxy.database.DatabaseManager;
import de.pexora.proxy.logger.LoggerService;
import de.pexora.proxy.status.StatusAPI;

/**
 * Die API-Schnittstelle für das PexoraProxy-Plugin.
 * Ermöglicht externen Zugriff auf zentrale Funktionen.
 */
public class PexoraProxyAPI {

    private static PexoraProxy plugin;

    /**
     * Initialisiert die API mit der Plugin-Instanz
     *
     * @param instance Die PexoraProxy-Instanz
     */
    public static void initialize(PexoraProxy instance) {
        if (plugin == null) {
            plugin = instance;
        }
    }

    /**
     * @return Die PexoraProxy-Instanz
     * @throws IllegalStateException wenn die API nicht initialisiert wurde
     */
    public static PexoraProxy getPlugin() {
        if (plugin == null) {
            throw new IllegalStateException("PexoraProxyAPI wurde nicht initialisiert!");
        }
        return plugin;
    }

    /**
     * @return Der Logger-Service für formatierte Konsolenausgaben
     * @throws IllegalStateException wenn die API nicht initialisiert wurde
     */
    public static LoggerService getLoggerService() {
        if (plugin == null) {
            throw new IllegalStateException("PexoraProxyAPI wurde nicht initialisiert!");
        }
        return plugin.getLoggerService();
    }

    /**
     * @return Die Status-API für Plugin-Status-Management
     * @throws IllegalStateException wenn die API nicht initialisiert wurde
     */
    public static StatusAPI getStatusAPI() {
        if (plugin == null) {
            throw new IllegalStateException("PexoraProxyAPI wurde nicht initialisiert!");
        }
        return plugin.getStatusAPI();
    }

    /**
     * @return Der Datenbank-Manager für Datenbankoperationen
     * @throws IllegalStateException wenn die API nicht initialisiert wurde
     */
    public static DatabaseManager getDatabaseManager() {
        if (plugin == null) {
            throw new IllegalStateException("PexoraProxyAPI wurde nicht initialisiert!");
        }
        return plugin.getDatabaseManager();
    }
}