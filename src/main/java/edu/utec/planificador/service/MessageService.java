package edu.utec.planificador.service;

/**
 * Service for retrieving internationalized messages from properties files.
 * Provides a simplified interface to access messages with parameters.
 */
public interface MessageService {
    
    /**
     * Retrieves a message by its key.
     * 
     * @param key the message key
     * @return the message text
     */
    String getMessage(String key);
    
    /**
     * Retrieves a message by its key with parameters.
     * 
     * @param key the message key
     * @param args the message parameters
     * @return the message text with parameters replaced
     */
    String getMessage(String key, Object... args);
}
