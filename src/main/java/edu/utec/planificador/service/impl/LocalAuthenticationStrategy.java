package edu.utec.planificador.service.impl;

import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.exception.InvalidCredentialsException;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AuthenticationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageSource messageSource;

    @Override
    public User authenticate(String email, String password) {
        log.debug("Attempting local authentication for user: {}", email);

        User user = userRepository.findByUtecEmail(email)
            .orElseThrow(() -> {
                log.warn("Authentication failed for {}: user not found", email);
                return new InvalidCredentialsException(
                    messageSource.getMessage("auth.error.invalid-credentials", 
                        null, 
                        LocaleContextHolder.getLocale())
                );
            });

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            log.warn(
                "Authentication failed for {}: incorrect provider configured (expected LOCAL, got {})",
                email,
                user.getAuthProvider()
            );
            throw new InvalidCredentialsException(
                "Este usuario no está configurado para autenticación local. " +
                "Por favor, use " + user.getAuthProvider().getDisplayName()
            );
        }

        if (!user.isEnabled()) {
            log.warn("Authentication failed for {}: account disabled", email);
            throw new InvalidCredentialsException(
                messageSource.getMessage("auth.error.account-disabled", 
                    null, 
                    LocaleContextHolder.getLocale())
            );
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Authentication failed for {}: invalid credentials", email);
            throw new InvalidCredentialsException(
                messageSource.getMessage("auth.error.invalid-credentials", 
                    null, 
                    LocaleContextHolder.getLocale())
            );
        }

        log.info("User authenticated successfully via LOCAL: {}", email);
        return user;
    }

    @Override
    public boolean supports(String providerName) {
        return AuthProvider.LOCAL.name().equalsIgnoreCase(providerName);
    }
}
