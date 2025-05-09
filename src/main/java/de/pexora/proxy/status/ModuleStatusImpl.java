package de.pexora.proxy.status;

import de.pexora.proxy.api.status.ModuleStatus;

/**
 * Implementierung des ModuleStatus für das Proxy-System.
 */
public class ModuleStatusImpl extends ModuleStatus {
    
    private final String component;
    private StatusAPI.Status status;
    private String message;
    
    /**
     * Erstellt eine neue ModuleStatusImpl-Instanz
     * 
     * @param component Der Name der Komponente
     * @param status Der initiale Status
     * @param message Die Statusnachricht
     */
    public ModuleStatusImpl(String component, StatusAPI.Status status, String message) {
        super(component, status == StatusAPI.Status.OK);
        this.component = component;
        this.status = status;
        this.message = message;
    }
    
    /**
     * Gibt den Status zurück
     * 
     * @return Der Status
     */
    public StatusAPI.Status getProxyStatus() {
        return status;
    }
    
    /**
     * Setzt den Status
     * 
     * @param status Der neue Status
     */
    public void setProxyStatus(StatusAPI.Status status) {
        this.status = status;
        setEnabled(status == StatusAPI.Status.OK);
    }
    
    /**
     * Gibt die Statusnachricht zurück
     * 
     * @return Die Statusnachricht
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * Setzt die Statusnachricht
     * 
     * @param message Die neue Statusnachricht
     */
    public void setMessage(String message) {
        this.message = message;
    }
}