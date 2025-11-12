package edu.utec.planificador.service.impl;

import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.exception.InvalidCredentialsException;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AuthenticationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(name = "security.ldap.enabled", havingValue = "true")
public class LdapAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final LdapTemplate ldapTemplate;
    private final MessageSource messageSource;

    @Value("${security.ldap.base:dc=utec,dc=edu,dc=uy}")
    private String ldapBase;

    @Autowired
    public LdapAuthenticationStrategy(
        UserRepository userRepository,
        MessageSource messageSource,
        @Autowired(required = false) LdapTemplate ldapTemplate
    ) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public User authenticate(String email, String password) {
        log.debug("Attempting LDAP authentication for user: {}", email);

        if (ldapTemplate == null) {
            log.error("LDAP template is not configured");
            throw new InvalidCredentialsException(
                messageSource.getMessage(
                    "auth.error.ldap-not-enabled",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );
        }

        try {
            boolean authenticated = authenticateWithLdap(email, password);

            if (!authenticated) {
                log.warn("LDAP authentication failed for {}: invalid LDAP credentials", email);

                throw new InvalidCredentialsException(
                    messageSource.getMessage(
                        "auth.error.invalid-credentials",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                );
            }

            User user = findAndValidateLdapUser(email);

            if (!user.isEnabled()) {
                log.warn("LDAP authentication failed for {}: account disabled", email);
                throw new InvalidCredentialsException(
                    messageSource.getMessage(
                    "auth.error.account-disabled",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                );
            }

            log.info("User authenticated successfully via LDAP: {}", email);
            return user;

        } catch (InvalidCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("LDAP authentication error for user {}: {}", email, e.getMessage());
            throw new InvalidCredentialsException(
                messageSource.getMessage(
                    "auth.error.ldap-error",
                    new Object[]{e.getMessage()},
                    LocaleContextHolder.getLocale()
                ), 
                e
            );
        }
    }

    private boolean authenticateWithLdap(String email, String password) {
        try {
            String sanitizedEmail = sanitizeLdapInput(email);

            String username = sanitizedEmail.contains("@") 
                ? sanitizedEmail.substring(0, sanitizedEmail.indexOf("@")) 
                : sanitizedEmail;

            Filter filter = new EqualsFilter("uid", username);
            return ldapTemplate.authenticate(ldapBase, filter.encode(), password);

        } catch (Exception e) {
            log.error("LDAP authentication error for {}: {}", email, e.getMessage());
            return false;
        }
    }

    private String sanitizeLdapInput(String input) {
        if (input == null) {
            return "";
        }
        
        return input
            .replace("\\", "\\\\")
            .replace("*", "\\*")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("\0", "\\00")
            .replace("/", "\\/");
    }

    private User findAndValidateLdapUser(String email) {
        Optional<User> existingUser = userRepository.findByUtecEmail(email);

        if (existingUser.isEmpty()) {
            log.warn("LDAP authentication failed for {}: user not found in local database", email);

            throw new InvalidCredentialsException(
                messageSource.getMessage(
                    "auth.error.invalid-credentials",
                    null,
                    LocaleContextHolder.getLocale()
                )
            );
        }

        User user = existingUser.get();

        if (user.getAuthProvider() != AuthProvider.LDAP) {
            log.warn(
                "LDAP authentication failed for {}: incorrect provider (expected LDAP, got {})", 
                email, 
                user.getAuthProvider()
            );
            throw new InvalidCredentialsException(
                messageSource.getMessage(
                    "auth.error.incorrect-auth-provider",
                    new Object[]{user.getAuthProvider().getDisplayName()},
                    LocaleContextHolder.getLocale()
                )
            );
        }
        
        return user;
    }

    @Override
    public boolean supports(String providerName) {
        return AuthProvider.LDAP.name().equalsIgnoreCase(providerName);
    }
}
