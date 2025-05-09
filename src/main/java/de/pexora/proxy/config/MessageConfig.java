package de.pexora.proxy.config;

import com.moandjiezana.toml.Toml;
import de.pexora.proxy.PexoraProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Verwaltet die Nachrichtenkonfiguration des Plugins
 */
public class MessageConfig {

    private final PexoraProxy plugin;
    private final Path messagesPath;
    private Toml config;
    
    private Component prefixComponent;
    private final Map<String, String> messages = new HashMap<>();

    /**
     * Erstellt eine neue Nachrichtenkonfigurationsinstanz
     *
     * @param plugin Die Plugin-Instanz
     */
    public MessageConfig(PexoraProxy plugin) {
        this.plugin = plugin;
        this.messagesPath = plugin.getDataDirectory().resolve("messages.toml");
        
        createDefault();
        load();
    }

    /**
     * Erstellt die Standardnachrichtenkonfiguration, falls sie nicht existiert
     */
    private void createDefault() {
        if (Files.exists(messagesPath)) {
            return;
        }

        plugin.getLoggerService().info("Erstelle Standard-Nachrichtenkonfiguration...");

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("messages.toml")) {
            Files.createDirectories(messagesPath.getParent());
            Files.copy(is, messagesPath);
        } catch (IOException e) {
            plugin.getLoggerService().severe("Konnte Standard-Nachrichtenkonfiguration nicht erstellen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Nachrichtenkonfiguration
     */
    private void load() {
        plugin.getLoggerService().info("Lade Nachrichtenkonfiguration...");
        
        try {
            this.config = new Toml().read(messagesPath.toFile());
            
            // Präfix laden
            String prefixString = config.getString("prefix");
            this.prefixComponent = MiniMessage.miniMessage().deserialize(prefixString);
            
            // Debug-Ausgabe
            plugin.getLoggerService().debug("Nachrichtenkonfiguration geladen, Präfix: " + prefixString);
            
            // Alle Nachrichten laden
            messages.clear();
            config.entrySet().forEach(entry -> {
                if (!"prefix".equals(entry.getKey())) {
                    messages.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            });
            
            plugin.getLoggerService().info("Nachrichtenkonfiguration erfolgreich geladen!");
        } catch (Exception e) {
            plugin.getLoggerService().severe("Fehler beim Laden der Nachrichtenkonfiguration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lädt die Nachrichtenkonfiguration neu
     */
    public void reload() {
        load();
    }

    /**
     * Holt eine Nachricht aus der Konfiguration
     *
     * @param key Der Schlüssel der Nachricht
     * @return Die Nachricht als Component
     */
    public Component getMessage(String key) {
        if (!messages.containsKey(key)) {
            Component missingComponent = Component.text("Fehlende Nachricht: " + key);
            plugin.getLoggerService().warning("Nachricht '" + key + "' nicht gefunden!");
            return missingComponent;
        }
        
        String message = messages.get(key);
        Component component = MiniMessage.miniMessage().deserialize(message);
        return component;
    }

    /**
     * Holt eine Nachricht mit Präfix aus der Konfiguration
     *
     * @param key Der Schlüssel der Nachricht
     * @return Die Nachricht mit Präfix als Component
     */
    public Component getMessageWithPrefix(String key) {
        if (!messages.containsKey(key)) {
            plugin.getLoggerService().warning("Nachricht '" + key + "' nicht gefunden!");
            return Component.text("Fehlende Nachricht: " + key);
        }
        
        String message = messages.get(key);
        if (prefixComponent != null) {
            return prefixComponent.append(MiniMessage.miniMessage().deserialize(message));
        } else {
            return MiniMessage.miniMessage().deserialize(message);
        }
    }

    /**
     * @return Die Präfix-Komponente
     */
    public Component getPrefix() {
        return prefixComponent;
    }
}