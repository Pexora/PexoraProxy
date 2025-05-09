package de.pexora.proxy.logger;

import de.pexora.proxy.PexoraProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * Service für formatierte Logging-Funktionen
 */
public class LoggerService {

    private final PexoraProxy plugin;
    private final String prefix = "[PEX] ";
    private boolean debugMode = false;

    /**
     * Erstellt einen neuen Logger-Service
     *
     * @param plugin Die Plugin-Instanz
     */
    public LoggerService(PexoraProxy plugin) {
        this.plugin = plugin;
    }

    /**
     * Sendet eine Info-Nachricht an die Konsole
     *
     * @param message Die Nachricht
     */
    public void info(String message) {
        plugin.getSLF4JLogger().info(prefix + message);
    }

    /**
     * Sendet eine Warnung an die Konsole
     *
     * @param message Die Nachricht
     */
    public void warning(String message) {
        plugin.getSLF4JLogger().warn(prefix + message);
    }
    
    /**
     * Sendet eine Warnung an die Konsole (Alias für warning)
     *
     * @param message Die Nachricht
     */
    public void warn(String message) {
        warning(message);
    }

    /**
     * Sendet einen schwerwiegenden Fehler an die Konsole
     *
     * @param message Die Nachricht
     */
    public void severe(String message) {
        plugin.getSLF4JLogger().error(prefix + message);
    }
    
    /**
     * Sendet einen Fehler an die Konsole (Alias für severe)
     *
     * @param message Die Nachricht
     */
    public void error(String message) {
        severe(message);
    }

    /**
     * Sendet eine Debug-Nachricht an die Konsole, wenn der Debug-Modus aktiviert ist
     *
     * @param message Die Nachricht
     */
    public void debug(String message) {
        if (debugMode) {
            plugin.getSLF4JLogger().info(prefix + "[DEBUG] " + message);
        }
    }

    /**
     * Sendet eine Komponente als Nachricht an die Konsole
     *
     * @param component Die Komponente
     */
    public void info(Component component) {
        String plainText = PlainTextComponentSerializer.plainText().serialize(component);
        plugin.getSLF4JLogger().info(prefix + plainText);
    }

    /**
     * Aktiviert oder deaktiviert den Debug-Modus
     *
     * @param enabled Ob der Debug-Modus aktiviert sein soll
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        info("Debug-Modus " + (enabled ? "aktiviert" : "deaktiviert"));
    }

    /**
     * Zeigt das PEX-Logo in der Konsole an
     */
    public void printLogo() {
        info("");
        info("  _____   ________   __");
        info(" |  __ \\ |  ____\\ \\ / /");
        info(" | |__) || |__   \\ V / ");
        info(" |  ___/ |  __|   > <  ");
        info(" | |     | |____ / . \\ ");
        info(" |_|     |______/_/ \\_\\");
        info("");
        info(" PexoraProxy v1.0.0");
        info(" Entwickelt vom Pexora Development Team");
        info(" --------------------------------------");
    }
}