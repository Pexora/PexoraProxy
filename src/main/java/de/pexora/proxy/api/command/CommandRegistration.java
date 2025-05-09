package de.pexora.proxy.api.command;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import de.pexora.proxy.PexoraProxy;

/**
 * Hilfsklasse zur Registrierung von Befehlen im PexoraProxy-System.
 * Diese Klasse erleichtert das Registrieren und Verwalten von Befehlen für Module.
 */
public class CommandRegistration {
    
    private final PexoraProxy plugin;
    private final String commandName;
    private final SimpleCommand command;
    private String[] aliases;
    private String permission;
    private String description;
    
    /**
     * Erstellt eine neue CommandRegistration-Instanz
     * 
     * @param plugin Die Plugin-Instanz
     * @param commandName Der Name des Befehls
     * @param command Der Befehlsausführer
     */
    public CommandRegistration(PexoraProxy plugin, String commandName, SimpleCommand command) {
        this.plugin = plugin;
        this.commandName = commandName;
        this.command = command;
    }
    
    /**
     * Setzt die Aliase für den Befehl
     * 
     * @param aliases Die Befehlsaliase
     * @return Diese CommandRegistration-Instanz (für Method-Chaining)
     */
    public CommandRegistration withAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }
    
    /**
     * Setzt die erforderliche Berechtigung für den Befehl
     * 
     * @param permission Die Befehlsberechtigung
     * @return Diese CommandRegistration-Instanz (für Method-Chaining)
     */
    public CommandRegistration withPermission(String permission) {
        this.permission = permission;
        return this;
    }
    
    /**
     * Setzt die Beschreibung des Befehls
     * 
     * @param description Die Befehlsbeschreibung
     * @return Diese CommandRegistration-Instanz (für Method-Chaining)
     */
    public CommandRegistration withDescription(String description) {
        this.description = description;
        return this;
    }
    
    /**
     * Registriert den Befehl beim Velocity-Server
     */
    public void register() {
        CommandManager commandManager = plugin.getServer().getCommandManager();
        
        // Registriere Hauptbefehl
        commandManager.register(commandName, command);
        
        // Registriere Alias-Befehle, wenn vorhanden
        if (aliases != null && aliases.length > 0) {
            for (String alias : aliases) {
                commandManager.register(alias, command);
            }
        }
    }
    
    /**
     * Hebt die Registrierung des Befehls auf
     */
    public void unregister() {
        CommandManager commandManager = plugin.getServer().getCommandManager();
        
        // Entferne Hauptbefehl
        commandManager.unregister(commandName);
        
        // Entferne Alias-Befehle, wenn vorhanden
        if (aliases != null && aliases.length > 0) {
            for (String alias : aliases) {
                commandManager.unregister(alias);
            }
        }
    }
    
    /**
     * Gibt den Befehlsnamen zurück
     * 
     * @return Der Befehlsname
     */
    public String getCommandName() {
        return commandName;
    }
    
    /**
     * Gibt den Befehlsausführer zurück
     * 
     * @return Der Befehlsausführer
     */
    public SimpleCommand getCommand() {
        return command;
    }
    
    /**
     * Gibt die Befehlsaliase zurück
     * 
     * @return Die Befehlsaliase oder null
     */
    public String[] getAliases() {
        return aliases;
    }
    
    /**
     * Gibt die Befehlsberechtigung zurück
     * 
     * @return Die Befehlsberechtigung oder null
     */
    public String getPermission() {
        return permission;
    }
    
    /**
     * Gibt die Befehlsbeschreibung zurück
     * 
     * @return Die Befehlsbeschreibung oder null
     */
    public String getDescription() {
        return description;
    }
}