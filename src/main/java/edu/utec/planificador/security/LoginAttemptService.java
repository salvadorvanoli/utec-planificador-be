package edu.utec.planificador.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptInfo> emailAttemptsCache = new ConcurrentHashMap<>();

    public void loginFailed(String key, boolean isEmail) {
        ConcurrentHashMap<String, AttemptInfo> cache = isEmail ? emailAttemptsCache : attemptsCache;
        
        AttemptInfo attempts = cache.computeIfAbsent(key, k -> new AttemptInfo());
        attempts.incrementAttempts();
        
        log.warn(
            "Login failed for {}: {} (attempts: {})", 
            isEmail ? "user" : "IP", 
            key, 
            attempts.getAttempts()
        );
        
        if (attempts.getAttempts() >= MAX_ATTEMPTS) {
            log.error(
                "SECURITY ALERT: {} {} has been blocked due to {} failed login attempts", 
                isEmail ? "User" : "IP",
                key, 
                MAX_ATTEMPTS
            );
        }
    }

    public void loginSucceeded(String key, boolean isEmail) {
        ConcurrentHashMap<String, AttemptInfo> cache = isEmail ? emailAttemptsCache : attemptsCache;
        cache.remove(key);
        log.debug(
            "Login succeeded for {}: {}, cleared failed attempts", 
            isEmail ? "user" : "IP", key
        );
    }

    public boolean isBlocked(String key, boolean isEmail) {
        ConcurrentHashMap<String, AttemptInfo> cache = isEmail ? emailAttemptsCache : attemptsCache;
        
        AttemptInfo attempts = cache.get(key);
        if (attempts == null) {
            return false;
        }

        if (attempts.isLockoutExpired(LOCKOUT_DURATION_MINUTES)) {
            cache.remove(key);
            log.info("Lockout expired for {}: {}", isEmail ? "user" : "IP", key);
            return false;
        }

        return attempts.getAttempts() >= MAX_ATTEMPTS;
    }

    public long getRemainingLockoutTime(String key, boolean isEmail) {
        ConcurrentHashMap<String, AttemptInfo> cache = isEmail ? emailAttemptsCache : attemptsCache;
        
        AttemptInfo attempts = cache.get(key);
        if (attempts == null) {
            return 0;
        }

        return attempts.getRemainingLockoutMinutes(LOCKOUT_DURATION_MINUTES);
    }

    public void clearExpiredEntries() {
        clearExpiredFromCache(attemptsCache);
        clearExpiredFromCache(emailAttemptsCache);
    }

    private void clearExpiredFromCache(ConcurrentHashMap<String, AttemptInfo> cache) {
        cache.entrySet().removeIf(entry -> 
            entry.getValue().isLockoutExpired(LOCKOUT_DURATION_MINUTES)
        );
    }

    private static class AttemptInfo {
        private int attempts;
        private LocalDateTime lastAttempt;

        public AttemptInfo() {
            this.attempts = 0;
            this.lastAttempt = LocalDateTime.now();
        }

        public synchronized void incrementAttempts() {
            this.attempts++;
            this.lastAttempt = LocalDateTime.now();
        }

        public int getAttempts() {
            return attempts;
        }

        public boolean isLockoutExpired(int lockoutMinutes) {
            return LocalDateTime.now().isAfter(lastAttempt.plusMinutes(lockoutMinutes));
        }

        public long getRemainingLockoutMinutes(int lockoutMinutes) {
            LocalDateTime unlockTime = lastAttempt.plusMinutes(lockoutMinutes);
            if (LocalDateTime.now().isAfter(unlockTime)) {
                return 0;
            }
            return java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
        }
    }
}
