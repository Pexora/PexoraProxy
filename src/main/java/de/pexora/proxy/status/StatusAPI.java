package de.pexora.proxy.status;

import de.pexora.proxy.api.status.ModuleStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API für Status-Tracking und Überwachung von Plugin-Komponenten.
 * Implementiert die StatusAPI aus dem API-Paket.
 */
public class StatusAPI implements de.pexora.proxy.api.status.StatusAPI {

    private final Map<String, StatusEntry> statusEntries = new HashMap<>();

    /**
     * Setzt den Status für eine Komponente
     *
     * @param component Die Komponente
     * @param status Der Status
     * @param message Eine optionale Nachricht
     */
    public void setStatus(String component, Status status, String message) {
        statusEntries.put(component, new StatusEntry(status, message));
    }

    /**
     * Setzt den Status für eine Komponente
     *
     * @param component Die Komponente
     * @param status Der Status
     */
    public void setStatus(String component, Status status) {
        setStatus(component, status, null);
    }

    /**
     * Holt den Status einer Komponente
     *
     * @param component Die Komponente
     * @return Der Status oder UNKNOWN, wenn nicht gefunden
     */
    public Status getStatus(String component) {
        StatusEntry entry = statusEntries.get(component);
        return entry != null ? entry.getStatus() : Status.UNKNOWN;
    }

    /**
     * Holt die Statusnachricht einer Komponente
     *
     * @param component Die Komponente
     * @return Die Nachricht oder null, wenn nicht gefunden
     */
    public String getMessage(String component) {
        StatusEntry entry = statusEntries.get(component);
        return entry != null ? entry.getMessage() : null;
    }

    /**
     * Prüft, ob eine Komponente einen bestimmten Status hat
     *
     * @param component Die Komponente
     * @param status Der zu prüfende Status
     * @return Ob die Komponente den Status hat
     */
    public boolean hasStatus(String component, Status status) {
        return getStatus(component) == status;
    }

    /**
     * @return Eine Kopie aller Status-Einträge
     */
    public Map<String, StatusEntry> getAllStatuses() {
        return new HashMap<>(statusEntries);
    }

    /**
     * Setzt den Status einer Komponente auf OK
     *
     * @param component Die Komponente
     */
    public void setOK(String component) {
        setStatus(component, Status.OK);
    }

    /**
     * Setzt den Status einer Komponente auf WARNING
     *
     * @param component Die Komponente
     * @param message Die Warnungsnachricht
     */
    public void setWarning(String component, String message) {
        setStatus(component, Status.WARNING, message);
    }

    /**
     * Setzt den Status einer Komponente auf ERROR
     *
     * @param component Die Komponente
     * @param message Die Fehlernachricht
     */
    public void setError(String component, String message) {
        setStatus(component, Status.ERROR, message);
    }

    /**
     * Setzt den Status einer Komponente auf LOADING
     *
     * @param component Die Komponente
     */
    public void setLoading(String component) {
        setStatus(component, Status.LOADING);
    }

    /**
     * Entfernt den Status einer Komponente
     *
     * @param component Die Komponente
     */
    public void removeStatus(String component) {
        statusEntries.remove(component);
    }

    /**
     * Löscht alle Status-Einträge
     */
    public void clearAllStatuses() {
        statusEntries.clear();
    }
    
    // Methoden der StatusAPI-Schnittstelle
    
    @Override
    public void registerModule(String moduleName, boolean enabled) {
        setStatus(moduleName, enabled ? Status.OK : Status.ERROR);
    }
    
    @Override
    public void unregisterModule(String moduleName) {
        removeStatus(moduleName);
    }
    
    @Override
    public boolean isModuleEnabled(String moduleName) {
        return getStatus(moduleName) == Status.OK;
    }
    
    @Override
    public Map<String, Boolean> getModuleStatus() {
        Map<String, Boolean> result = new HashMap<>();
        for (Map.Entry<String, StatusEntry> entry : statusEntries.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getStatus() == Status.OK);
        }
        return result;
    }
    
    @Override
    public int getEnabledModuleCount() {
        int count = 0;
        for (StatusEntry entry : statusEntries.values()) {
            if (entry.getStatus() == Status.OK) {
                count++;
            }
        }
        return count;
    }
    
    @Override
    public int getTotalModuleCount() {
        return statusEntries.size();
    }
    
    @Override
    public ModuleStatus getModuleStatusForModule(String moduleName) {
        StatusEntry entry = statusEntries.get(moduleName);
        if (entry == null) {
            return null;
        }
        
        return new ModuleStatusImpl(moduleName, entry.getStatus(), entry.getMessage());
    }

    /**
     * Status-Typen für Komponenten
     */
    public enum Status {
        OK,
        WARNING,
        ERROR,
        LOADING,
        UNKNOWN
    }

    /**
     * Repräsentiert einen Status-Eintrag mit Status und Nachricht
     */
    public class StatusEntry {
        private final Status status;
        private final String message;
        
        public StatusEntry(Status status, String message) {
            this.status = status;
            this.message = message;
        }
        
        public Status getStatus() {
            return status;
        }
        
        public String getMessage() {
            return message;
        }
    }
}