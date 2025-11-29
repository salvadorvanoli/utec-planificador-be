package edu.utec.planificador.service.impl;

import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.exception.InvalidCredentialsException;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AuthenticationStrategy;
import edu.utec.planificador.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;

    @Override
    public User authenticate(String email, String password) {
        log.debug("Attempting LOCAL authentication for user: {}", email);

        User user = userRepository.findByUtecEmail(email)
            .orElseThrow(() -> {
                log.warn("LOCAL authentication failed for {}: user not found", email);

                return new InvalidCredentialsException(
                    messageService.getMessage("auth.error.invalid-credentials")
                );
            });

        if (user.getAuthProvider() != AuthProvider.LOCAL) {
            log.warn(
                "LOCAL authentication failed for {}: incorrect provider (expected LOCAL, got {})",
                email,
                user.getAuthProvider()
            );

            throw new InvalidCredentialsException(
                messageService.getMessage(
                    "auth.error.incorrect-auth-provider",
                    user.getAuthProvider().getDisplayName()
                )
            );
        }

        if (!user.isEnabled()) {
            log.warn("LOCAL authentication failed for {}: account disabled", email);

            throw new InvalidCredentialsException(
                messageService.getMessage("auth.error.account-disabled")
            );
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("LOCAL authentication failed for {}: invalid password", email);

            throw new InvalidCredentialsException(
                messageService.getMessage("auth.error.invalid-credentials")
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
