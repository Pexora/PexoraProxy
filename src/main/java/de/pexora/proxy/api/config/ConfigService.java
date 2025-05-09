package de.pexora.proxy.api.config;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service zum Verwalten von Konfigurationsdateien für Module.
 * Diese Klasse bietet Methoden zum Laden, Speichern und Bearbeiten von TOML-Konfigurationen.
 */
public class ConfigService {
    
    private final File configFile;
    private Toml config;
    private Map<String, Object> configData;
    
    /**
     * Erstellt einen neuen ConfigService für eine bestimmte Datei
     * 
     * @param configFile Die Konfigurationsdatei
     */
    public ConfigService(File configFile) {
        this.configFile = configFile;
        this.reload();
    }
    
    /**
     * Lädt die Konfiguration neu aus der Datei
     */
    public void reload() {
        if (configFile.exists()) {
            this.config = new Toml().read(configFile);
            this.configData = config.toMap();
        } else {
            this.config = new Toml();
            this.configData = Collections.emptyMap();
        }
    }
    
    /**
     * Speichert die aktuelle Konfiguration in die Datei
     * 
     * @return true bei Erfolg, sonst false
     */
    public boolean save() {
        try {
            TomlWriter writer = new TomlWriter();
            writer.write(configData, configFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Prüft, ob ein Pfad in der Konfiguration existiert
     * 
     * @param path Der Konfigurationspfad
     * @return true, wenn der Pfad existiert, sonst false
     */
    public boolean contains(String path) {
        return config.contains(path);
    }
    
    /**
     * Holt einen String aus der Konfiguration
     * 
     * @param path Der Konfigurationspfad
     * @param defaultValue Der Standardwert, falls nicht gefunden
     * @return Der Wert oder der Standardwert
     */
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    /**
     * Holt einen Long aus der Konfiguration
     * 
     * @param path Der Konfigurationspfad
     * @param defaultValue Der Standardwert, falls nicht gefunden
     * @return Der Wert oder der Standardwert
     */
    public Long getLong(String path, Long defaultValue) {
        return config.getLong(path, defaultValue);
    }
    
    /**
     * Holt einen Integer aus der Konfiguration
     * 
     * @param path Der Konfigurationspfad
     * @param defaultValue Der Standardwert, falls nicht gefunden
     * @return Der Wert oder der Standardwert
     */
    public Long getInteger(String path, Long defaultValue) {
        return config.getLong(path, defaultValue);
    }
    
    /**
     * Holt einen Boolean aus der Konfiguration
     * 
     * @param path Der Konfigurationspfad
     * @param defaultValue Der Standardwert, falls nicht gefunden
     * @return Der Wert oder der Standardwert
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    /**
     * Holt eine Double aus der Konfiguration
     * 
     * @param path Der Konfigurationspfad
     * @param defaultValue Der Standardwert, falls nicht gefunden
     * @return Der Wert oder der Standardwert
     */
    public Double getDouble(String path, Double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * Holt eine Liste aus der Konfiguration
     * 
     * @param path Der Konfigurationspfad
     * @return Die Liste oder eine leere Liste, falls nicht gefunden
     */
    public <T> List<T> getList(String path) {
        return config.getList(path);
    }
    
    /**
     * Setzt einen Wert in der Konfiguration
     * 
     * @param path Der Konfigurationspfad
     * @param value Der zu setzende Wert
     */
    public void set(String path, Object value) {
        // Bei einfachen Pfaden ohne Punkte direkt in die Map einfügen
        if (!path.contains(".")) {
            configData.put(path, value);
            return;
        }
        
        // Bei komplexen Pfaden mit Punkten die Hierarchie durchlaufen
        String[] parts = path.split("\\.");
        Map<String, Object> current = configData;
        
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            
            if (!current.containsKey(part) || !(current.get(part) instanceof Map)) {
                current.put(part, Collections.emptyMap());
            }
            
            current = (Map<String, Object>) current.get(part);
        }
        
        current.put(parts[parts.length - 1], value);
    }
}