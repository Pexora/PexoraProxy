package de.pexora.proxy.api.events;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import de.pexora.proxy.PexoraProxy;

import java.lang.reflect.Method;

/**
 * Hilfsklasse zur Registrierung von Event-Listenern im PexoraProxy-System.
 * Diese Klasse vereinfacht die Registrierung von Event-Handlern.
 */
public class EventRegistration {
    
    private final PexoraProxy plugin;
    
    /**
     * Erstellt eine neue EventRegistration-Instanz
     * 
     * @param plugin Das Plugin, für das Events registriert werden sollen
     */
    public EventRegistration(PexoraProxy plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Registriert einen Listener für alle seine Event-Handler
     * 
     * @param listener Der zu registrierende Listener
     */
    public void registerListener(Object listener) {
        plugin.getServer().getEventManager().register(plugin, listener);
    }
    
    /**
     * Registriert mehrere Listener auf einmal
     * 
     * @param listeners Die zu registrierenden Listener
     */
    public void registerListeners(Object... listeners) {
        for (Object listener : listeners) {
            registerListener(listener);
        }
    }
    
    /**
     * Hebt die Registrierung aller Listener eines Plugins auf
     */
    public void unregisterAllListeners() {
        plugin.getServer().getEventManager().unregisterListeners(plugin);
    }
    
    /**
     * Hebt die Registrierung eines bestimmten Listeners auf
     * 
     * @param listener Der zu deregistrierende Listener
     */
    public void unregisterListener(Object listener) {
        // In der Velocity-API muss man jeden EventHandler einzeln deregistrieren
        // oder alle Events eines Plugins auf einmal
        plugin.getServer().getEventManager().unregisterListeners(listener);
    }
    
    /**
     * Registriert einen spezifischen Event-Handler für einen bestimmten Event-Typ mit einer gewünschten Reihenfolge
     * 
     * @param <E> Der Event-Typ
     * @param eventClass Die Event-Klasse
     * @param handler Der Event-Handler
     * @param postOrder Die Reihenfolge der Event-Verarbeitung
     */
    public <E> void registerEvent(Class<E> eventClass, ProxyEventHandler<E> handler, PostOrder postOrder) {
        EventManager eventManager = plugin.getServer().getEventManager();
        eventManager.register(plugin, eventClass, postOrder, (e) -> handler.handle(e));
    }
    
    /**
     * Registriert einen spezifischen Event-Handler mit normaler Priorität
     * 
     * @param <E> Der Event-Typ
     * @param eventClass Die Event-Klasse
     * @param handler Der Event-Handler
     */
    public <E> void registerEvent(Class<E> eventClass, ProxyEventHandler<E> handler) {
        registerEvent(eventClass, handler, PostOrder.NORMAL);
    }
    
    /**
     * Funktionales Interface für Event-Handler
     * 
     * @param <E> Der Event-Typ
     */
    @FunctionalInterface
    public interface ProxyEventHandler<E> {
        void handle(E event);
    }
}