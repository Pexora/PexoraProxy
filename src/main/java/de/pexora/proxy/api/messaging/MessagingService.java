package de.pexora.proxy.api.messaging;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.pexora.proxy.PexoraProxy;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Service zum Senden von Nachrichten an Spieler.
 * Diese Klasse stellt Methoden bereit, um formatierte Nachrichten an Spieler zu senden.
 */
public class MessagingService {
    
    private final PexoraProxy plugin;
    
    /**
     * Erstellt einen neuen MessagingService
     * 
     * @param plugin Die Plugin-Instanz
     */
    public MessagingService(PexoraProxy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Sendet eine Nachrichtenkomponente an einen Spieler
     * 
     * @param player Der Spieler, der die Nachricht erhalten soll
     * @param component Die zu sendende Nachrichtenkomponente
     */
    public void sendMessage(Player player, Component component) {
        player.sendMessage(component);
    }
    
    /**
     * Sendet eine Textnachricht an einen Spieler
     * 
     * @param player Der Spieler, der die Nachricht erhalten soll
     * @param message Die zu sendende Textnachricht
     */
    public void sendMessage(Player player, String message) {
        player.sendMessage(Component.text(message));
    }
    
    /**
     * Sendet eine Nachricht an einen Spieler mit dem Plugin-Präfix
     * 
     * @param player Der Spieler, der die Nachricht erhalten soll
     * @param component Die zu sendende Nachrichtenkomponente
     * @param usePrefix Ob der Plugin-Präfix verwendet werden soll
     */
    public void sendMessage(Player player, Component component, boolean usePrefix) {
        if (usePrefix) {
            // Implementierung würde den Präfix aus der Konfiguration holen
            // Da die MessageConfig-Klasse keine getPrefixComponent() Methode hat, 
            // senden wir hier nur die normale Komponente
            player.sendMessage(component);
        } else {
            player.sendMessage(component);
        }
    }
    
    /**
     * Sendet eine Nachricht an alle Spieler auf dem Proxy
     * 
     * @param component Die zu sendende Nachrichtenkomponente
     */
    public void broadcastMessage(Component component) {
        plugin.getServer().getAllPlayers().forEach(player -> player.sendMessage(component));
    }
    
    /**
     * Sendet eine Nachricht an alle Spieler mit einer bestimmten Berechtigung
     * 
     * @param component Die zu sendende Nachrichtenkomponente
     * @param permission Die erforderliche Berechtigung
     */
    public void broadcastMessage(Component component, String permission) {
        plugin.getServer().getAllPlayers().stream()
                .filter(player -> player.hasPermission(permission))
                .forEach(player -> player.sendMessage(component));
    }
    
    /**
     * Sendet eine Aktionsleisten-Nachricht an einen Spieler
     * 
     * @param player Der Spieler, der die Nachricht erhalten soll
     * @param component Die zu sendende Nachrichtenkomponente
     */
    public void sendActionBar(Player player, Component component) {
        player.sendActionBar(component);
    }
    
    /**
     * Sendet eine Titel-Nachricht an einen Spieler
     * 
     * @param player Der Spieler, der die Nachricht erhalten soll
     * @param title Der Haupttitel
     * @param subtitle Der Untertitel
     * @param fadeIn Die Einblendezeit in Sekunden
     * @param stay Die Anzeigezeit in Sekunden
     * @param fadeOut Die Ausblendezeit in Sekunden
     */
    public void sendTitle(Player player, Component title, Component subtitle, 
                         int fadeIn, int stay, int fadeOut) {
        Title titleObject = Title.title(
                title,
                subtitle,
                Title.Times.times(
                        Duration.ofSeconds(fadeIn),
                        Duration.ofSeconds(stay),
                        Duration.ofSeconds(fadeOut)
                )
        );
        player.showTitle(titleObject);
    }
    
    /**
     * Findet einen Spieler anhand seines Namens
     * 
     * @param name Der Spielername
     * @return Optional mit dem Spieler, oder empty wenn nicht gefunden
     */
    public Optional<Player> getPlayer(String name) {
        return plugin.getServer().getPlayer(name);
    }
    
    /**
     * Findet einen Spieler anhand seiner UUID
     * 
     * @param uuid Die Spieler-UUID
     * @return Optional mit dem Spieler, oder empty wenn nicht gefunden
     */
    public Optional<Player> getPlayer(UUID uuid) {
        return plugin.getServer().getPlayer(uuid);
    }
    
    /**
     * Gibt die Proxy-Server-Instanz zurück
     * 
     * @return Die Proxy-Server-Instanz
     */
    public ProxyServer getServer() {
        return plugin.getServer();
    }
}