package de.pexora.proxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import de.pexora.proxy.PexoraProxy;

/**
 * Haupt-Befehl für das PexoraProxy-Plugin.
 */
public class PexoraCommand implements SimpleCommand {

    private final PexoraProxy plugin;

    /**
     * Erstellt einen neuen PexoraCommand
     *
     * @param plugin Die Plugin-Instanz
     */
    public PexoraCommand(PexoraProxy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length == 0) {
            showHelp(invocation);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (invocation.source().hasPermission("pexora.command.reload")) {
                    plugin.reload();
                    invocation.source().sendMessage(Component.text("PexoraProxy wurde neu geladen!").color(NamedTextColor.GREEN));
                } else {
                    invocation.source().sendMessage(Component.text("Du hast keine Berechtigung für diesen Befehl!").color(NamedTextColor.RED));
                }
                break;
            case "info":
                if (invocation.source().hasPermission("pexora.command.info")) {
                    showInfo(invocation);
                } else {
                    invocation.source().sendMessage(Component.text("Du hast keine Berechtigung für diesen Befehl!").color(NamedTextColor.RED));
                }
                break;
            case "help":
            default:
                showHelp(invocation);
                break;
        }
    }

    /**
     * Zeigt Hilfe-Informationen an
     */
    private void showHelp(Invocation invocation) {
        invocation.source().sendMessage(Component.text("=== PexoraProxy Hilfe ===").color(NamedTextColor.GOLD));
        invocation.source().sendMessage(Component.text("/pexoraproxy reload - Lädt das Plugin neu").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/pexoraproxy info - Zeigt Informationen zum Plugin").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("/pexoraproxy help - Zeigt diese Hilfe an").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("Alias: /pexcore").color(NamedTextColor.GRAY));
    }

    /**
     * Zeigt Informationen zum Plugin an
     */
    private void showInfo(Invocation invocation) {
        invocation.source().sendMessage(Component.text("=== PexoraProxy Info ===").color(NamedTextColor.GOLD));
        invocation.source().sendMessage(Component.text("Version: 1.0.0").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("Entwickler: Pexora Development Team").color(NamedTextColor.YELLOW));
        invocation.source().sendMessage(Component.text("Beschreibung: Core plugin für Velocity-Proxy mit modularem System und Datenbankunterstützung").color(NamedTextColor.YELLOW));
        
        boolean databaseStatus = plugin.getDatabaseManager().isConnected();
        String databaseText = databaseStatus ? "Verbunden" : "Nicht verbunden";
        NamedTextColor databaseColor = databaseStatus ? NamedTextColor.GREEN : NamedTextColor.RED;
        
        invocation.source().sendMessage(Component.text("Datenbank: ").color(NamedTextColor.YELLOW)
                .append(Component.text(databaseText).color(databaseColor)));
        
        int moduleCount = plugin.getModuleLoader().getModules().size();
        invocation.source().sendMessage(Component.text("Geladene Module: " + moduleCount).color(NamedTextColor.YELLOW));
    }
}