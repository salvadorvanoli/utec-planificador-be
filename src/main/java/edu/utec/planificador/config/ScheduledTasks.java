package edu.utec.planificador.config;

import edu.utec.planificador.security.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for maintenance and cleanup operations.
 * All tasks run in background and don't block application execution.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final LoginAttemptService loginAttemptService;

    /**
     * Cleans up expired login attempt entries from memory cache.
     * Runs every hour to prevent memory leaks and maintain optimal performance.
     * 
     * This task removes entries where:
     * - The lockout period (15 minutes) has expired
     * - No subsequent login attempts have been made
     * 
     * Without this cleanup, entries would accumulate indefinitely, causing:
     * - Gradual memory growth
     * - Slower lookup performance
     * - Potential OutOfMemoryError in high-traffic scenarios
     */
    @Scheduled(fixedRate = 3600000) // Every 1 hour (3,600,000 milliseconds)
    public void cleanupExpiredLoginAttempts() {
        log.debug("Starting cleanup of expired login attempt entries");
        
        try {
            loginAttemptService.clearExpiredEntries();
            log.debug("Successfully cleaned expired login attempt entries");
        } catch (Exception e) {
            log.error("Error during login attempts cleanup", e);
        }
    }
}
