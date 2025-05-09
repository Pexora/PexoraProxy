package de.pexora.proxy.messaging;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import de.pexora.proxy.PexoraProxy;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Verwaltet das Messaging zwischen Proxy und Server
 */
public class MessagingManager {

    private final PexoraProxy plugin;
    private final ChannelIdentifier channel;
    private final Map<UUID, Long> lastMessageTime = new ConcurrentHashMap<>();
    private final Map<String, Consumer<MessageEvent>> handlers = new HashMap<>();

    /**
     * Erstellt einen neuen Messaging-Manager
     *
     * @param plugin Die Plugin-Instanz
     */
    public MessagingManager(PexoraProxy plugin) {
        this.plugin = plugin;
        this.channel = MinecraftChannelIdentifier.from("pexora:proxy");
        
        registerChannel();
    }

    /**
     * Registriert den Kommunikationskanal
     */
    private void registerChannel() {
        plugin.getLoggerService().info("Registriere Messaging-Kanal: " + channel.getId());
        
        plugin.getServer().getChannelRegistrar().register(channel);
        
        // Implementiere Message-Event-Listener
        plugin.getServer().getEventManager().register(plugin, new PluginMessageListener());
    }
    
    /**
     * Listener für Plugin-Nachrichten
     */
    private class PluginMessageListener {
        // Hier können @Subscribe-annotierte Methoden für PluginMessageEvent hinzugefügt werden
    }

    /**
     * Sendet eine Nachricht an einen Server über einen Spieler
     *
     * @param player Der Spieler
     * @param action Die Aktion
     * @param data Die Daten
     */
    public void sendMessage(Player player, String action, String data) {
        if (player == null || !player.isActive()) {
            return;
        }
        
        // Verhindert Spam durch Nachrichtenbegrenzung
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long lastTime = lastMessageTime.getOrDefault(uuid, 0L);
        
        if (now - lastTime < 100) { // Minimale Zeit zwischen Nachrichten: 100ms
            return;
        }
        
        lastMessageTime.put(uuid, now);
        
        // Hier würde die tatsächliche Implementierung folgen
        // um Nachrichten zu senden (z.B. mit ByteArrayDataOutput)
        plugin.getLoggerService().debug("Nachricht gesendet an " + player.getUsername() + 
                                       ": Action=" + action + ", Data=" + data);
    }

    /**
     * Sendet eine Nachricht an alle verbundenen Server
     *
     * @param action Die Aktion
     * @param data Die Daten
     */
    public void broadcastMessage(String action, String data) {
        for (Player player : plugin.getServer().getAllPlayers()) {
            if (player.getCurrentServer().isPresent()) {
                sendMessage(player, action, data);
            }
        }
    }

    /**
     * Registriert einen Handler für eine bestimmte Aktion
     *
     * @param action Die Aktion
     * @param handler Der Handler
     */
    public void registerHandler(String action, Consumer<MessageEvent> handler) {
        handlers.put(action, handler);
    }

    /**
     * Entfernt einen Handler für eine bestimmte Aktion
     *
     * @param action Die Aktion
     */
    public void unregisterHandler(String action) {
        handlers.remove(action);
    }

    /**
     * Wird beim Herunterfahren aufgerufen, um aufzuräumen
     */
    public void unregister() {
        plugin.getLoggerService().info("Entferne Messaging-Kanal: " + channel.getId());
        plugin.getServer().getChannelRegistrar().unregister(channel);
        handlers.clear();
        lastMessageTime.clear();
    }

    /**
     * Event-Klasse für empfangene Nachrichten
     */
    public class MessageEvent {
        private final Player player;
        private final String action;
        private final String data;
        
        public MessageEvent(Player player, String action, String data) {
            this.player = player;
            this.action = action;
            this.data = data;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public String getAction() {
            return action;
        }
        
        public String getData() {
            return data;
        }
        
        public void reply(String data) {
            sendMessage(player, "reply:" + action, data);
        }
        
        public void replyWithComponent(Component component) {
            // Konvertiere die Komponente in eine JSON-Darstellung
            String json = net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serialize(component);
            reply(json);
        }
    }
}