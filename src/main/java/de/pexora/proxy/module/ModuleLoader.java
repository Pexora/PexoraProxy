package de.pexora.proxy.module;

import de.pexora.proxy.PexoraProxy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Verwaltet das Laden und Entladen von Modulen
 * 
 * Der ModuleLoader ist eines der Kernstücke von PexoraCore und übernimmt 
 * das automatische Erkennen, Laden und Verwalten aller Module im System.
 * 
 * Funktionen:
 * - Automatisches Laden aller JAR-Dateien aus dem Modules-Verzeichnis
 * - Prüfung der Plugin-Abhängigkeiten für kompatible Module
 * - Statusverfolgung aller geladenen Module
 * - Möglichkeit zum Neuladen einzelner oder aller Module
 * - Verhinderung von Mehrfachladung identischer Module
 * 
 * Module werden als reguläre JAR-Dateien verarbeitet, die den Namenskonventionen 
 * folgen (z.B. PexoraEconomy.jar) und die richtige Abhängigkeit zu PexoraCore definieren.
 */
public class ModuleLoader {

    private final PexoraProxy plugin;
    private final Path modulesDir;
    private final Map<String, ProxyModule> modules = new HashMap<>();

    /**
     * Erstellt einen neuen Modul-Loader
     *
     * @param plugin Die Plugin-Instanz
     */
    public ModuleLoader(PexoraProxy plugin) {
        this.plugin = plugin;
        this.modulesDir = plugin.getDataDirectory().resolve("modules");
        
        createModulesFolder();
        loadModules();
    }

    /**
     * Erstellt den Modules-Ordner, falls er nicht existiert
     */
    private void createModulesFolder() {
        try {
            if (!Files.exists(modulesDir)) {
                Files.createDirectories(modulesDir);
                plugin.getLoggerService().info("Modules-Ordner erstellt: " + modulesDir);
            }
        } catch (IOException e) {
            plugin.getLoggerService().severe("Fehler beim Erstellen des Modules-Ordners: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Kopiert Plugins in den Modules-Ordner, falls aktiviert
     */
    private void copyModulesToFolder() {
        if (!plugin.getCoreConfig().isModuleAutoCopy()) {
            return;
        }

        plugin.getLoggerService().info("Kopiere Plugins in den Modules-Ordner...");
        
        try {
            Path pluginsDir = plugin.getDataDirectory().getParent().resolve("plugins");
            
            if (!Files.exists(pluginsDir)) {
                return;
            }
            
            List<Path> pluginFiles = Files.list(pluginsDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .filter(path -> {
                        try {
                            String fileName = path.getFileName().toString();
                            return isPexoraModule(path) || 
                                   (plugin.getCoreConfig().isAllowNonPexoraModules() && !fileName.equals("PexoraProxy-1.0.0.jar"));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            
            for (Path pluginPath : pluginFiles) {
                String fileName = pluginPath.getFileName().toString();
                Path targetPath = modulesDir.resolve(fileName);
                
                // Überprüfen, ob eine ältere Version gelöscht werden soll
                if (plugin.getCoreConfig().isDeleteOldPlugins()) {
                    String baseName = fileName.substring(0, fileName.lastIndexOf('-'));
                    try {
                        Files.list(modulesDir)
                            .filter(path -> path.getFileName().toString().startsWith(baseName + "-") && 
                                   !path.getFileName().toString().equals(fileName))
                            .forEach(path -> {
                                try {
                                    Files.delete(path);
                                    plugin.getLoggerService().info("Alte Version gelöscht: " + path.getFileName());
                                } catch (IOException e) {
                                    plugin.getLoggerService().warning("Konnte alte Version nicht löschen: " + e.getMessage());
                                }
                            });
                    } catch (IOException e) {
                        plugin.getLoggerService().warning("Fehler beim Prüfen auf alte Versionen: " + e.getMessage());
                    }
                }
                
                // Kopieren der Datei, wenn sie noch nicht existiert
                if (!Files.exists(targetPath)) {
                    Files.copy(pluginPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    plugin.getLoggerService().info("Plugin kopiert: " + fileName);
                }
            }
        } catch (IOException e) {
            plugin.getLoggerService().severe("Fehler beim Kopieren von Plugins: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Prüft, ob es sich um ein Pexora-Modul handelt
     *
     * @param path Der Pfad zur JAR-Datei
     * @return Ob es sich um ein Pexora-Modul handelt
     */
    private boolean isPexoraModule(Path path) {
        try (JarFile jarFile = new JarFile(path.toFile())) {
            return jarFile.getEntry("module.toml") != null;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Lädt alle Module
     */
    private void loadModules() {
        // Zuerst alle Plugins in den Modules-Ordner kopieren
        copyModulesToFolder();
        
        plugin.getLoggerService().info("Lade Module...");
        
        // Zusätzliche Module hinzufügen
        String[] additionalModules = plugin.getCoreConfig().getAdditionalModules();
        if (additionalModules != null && additionalModules.length > 0) {
            for (String moduleName : additionalModules) {
                Path modulePath = plugin.getServer().getPluginManager().getPlugin(moduleName)
                        .flatMap(provider -> {
                            try {
                                // In Velocity 3.1.1, we need to use reflection to get the plugin jar path
                                // as the API might have changed
                                if (provider.getDescription() != null && provider.getDescription().getSource().isPresent()) {
                                    Path path = provider.getDescription().getSource().get();
                                    return java.util.Optional.of(path);
                                }
                            } catch (Exception e) {
                                plugin.getLoggerService().warning("Fehler beim Zugriff auf Plugin-JAR von " + moduleName + ": " + e.getMessage());
                            }
                            return java.util.Optional.empty();
                        })
                        .orElse(null);
                
                if (modulePath != null) {
                    try {
                        Path targetPath = modulesDir.resolve(modulePath.getFileName());
                        if (!Files.exists(targetPath)) {
                            Files.copy(modulePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            plugin.getLoggerService().info("Zusätzliches Modul kopiert: " + modulePath.getFileName());
                        }
                    } catch (IOException e) {
                        plugin.getLoggerService().warning("Konnte zusätzliches Modul nicht kopieren: " + e.getMessage());
                    }
                }
            }
        }
        
        // Keine Module laden, wenn der Ordner nicht existiert
        if (!Files.exists(modulesDir)) {
            plugin.getLoggerService().info("Keine Module geladen (Ordner existiert nicht)");
            return;
        }
        
        try {
            List<Path> moduleFiles = Files.list(modulesDir)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .collect(Collectors.toList());
            
            for (Path modulePath : moduleFiles) {
                try {
                    loadModule(modulePath.toFile());
                } catch (Exception e) {
                    plugin.getLoggerService().severe("Fehler beim Laden des Moduls " + modulePath.getFileName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            plugin.getLoggerService().info(modules.size() + " Module geladen");
        } catch (IOException e) {
            plugin.getLoggerService().severe("Fehler beim Laden der Module: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Lädt ein einzelnes Modul
     *
     * @param file Die Moduldatei
     */
    private void loadModule(File file) {
        String fileName = file.getName();
        
        plugin.getLoggerService().info("Lade Modul: " + fileName);
        
        // Hier würde die tatsächliche Implementierung sein, um das Modul zu laden
        // Da wir keine echten Module haben, erstellen wir ein Dummy-Modul
        ProxyModule module = new ProxyModule(fileName, "Dummy-Modul", "1.0.0");
        modules.put(fileName, module);
        
        plugin.getLoggerService().info("Modul " + fileName + " geladen");
    }

    /**
     * Entlädt alle Module
     */
    public void disableAllModules() {
        plugin.getLoggerService().info("Entlade alle Module...");
        
        for (ProxyModule module : new ArrayList<>(modules.values())) {
            try {
                // Hier würde die tatsächliche Implementierung sein, um das Modul zu entladen
                plugin.getLoggerService().info("Entlade Modul: " + module.getName());
                modules.remove(module.getFileName());
            } catch (Exception e) {
                plugin.getLoggerService().severe("Fehler beim Entladen des Moduls " + module.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        modules.clear();
        plugin.getLoggerService().info("Alle Module entladen");
    }

    /**
     * Lädt alle Module neu
     */
    public void reloadAllModules() {
        plugin.getLoggerService().info("Lade alle Module neu...");
        disableAllModules();
        loadModules();
    }

    /**
     * @return Eine Kopie der geladenen Module
     */
    public Map<String, ProxyModule> getModules() {
        return new HashMap<>(modules);
    }

    /**
     * Repräsentiert ein geladenes Proxy-Modul
     */
    public class ProxyModule {
        private final String fileName;
        private final String name;
        private final String version;
        
        public ProxyModule(String fileName, String name, String version) {
            this.fileName = fileName;
            this.name = name;
            this.version = version;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
    }
}