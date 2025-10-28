package edu.utec.planificador.service.impl;

import edu.utec.planificador.datatype.PersonalData;
import edu.utec.planificador.dto.request.PositionRequest;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.enumeration.Role;
import edu.utec.planificador.exception.InvalidCredentialsException;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.AuthenticationStrategy;
import edu.utec.planificador.service.PositionService;
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

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(name = "security.ldap.enabled", havingValue = "true")
public class LdapAuthenticationStrategy implements AuthenticationStrategy {

    private final UserRepository userRepository;
    private final LdapTemplate ldapTemplate;
    private final MessageSource messageSource;
    private final PositionService positionService;

    @Value("${security.ldap.base:dc=utec,dc=edu,dc=uy}")
    private String ldapBase;

    @Value("${security.ldap.default-role:TEACHER}")
    private String defaultRole;

    @Value("${security.ldap.default-campus-id:1}")
    private Long defaultCampusId;

    @Autowired
    public LdapAuthenticationStrategy(
        UserRepository userRepository,
        MessageSource messageSource,
        PositionService positionService,
        @Autowired(required = false) LdapTemplate ldapTemplate
    ) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.positionService = positionService;
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
                log.warn("LDAP authentication failed for user: {}", email);
                throw new InvalidCredentialsException(
                    messageSource.getMessage(
                        "auth.error.ldap-invalid-credentials",
                        null,
                        LocaleContextHolder.getLocale()
                    )
                );
            }

            User user = findOrCreateLdapUser(email);

            if (!user.isEnabled()) {
                log.warn("Disabled LDAP user attempted to login: {}", email);
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
            log.error("Error during LDAP authentication for user: {}", email, e);
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
            log.error("LDAP authentication error", e);
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

    private User findOrCreateLdapUser(String email) {
        Optional<User> existingUser = userRepository.findByUtecEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            if (user.getAuthProvider() != AuthProvider.LDAP) {
                log.warn("User {} exists but is not configured for LDAP. Provider: {}", email, user.getAuthProvider());
            }
            
            return user;
        }

        log.info("Creating new LDAP user in local database: {}", email);
        
        String username = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
        
        PersonalData personalData = new PersonalData();
        personalData.setName(username);
        personalData.setLastName("LDAP User");
        
        User newUser = new User(email, null, personalData);
        newUser.setAuthProvider(AuthProvider.LDAP);
        newUser.setEnabled(true);
        
        try {
            Role role = Role.valueOf(defaultRole.toUpperCase());
            PositionRequest.PositionType positionType = mapRoleToPositionType(role);
            
            PositionRequest positionRequest = PositionRequest.builder()
                .type(positionType)
                .role(role)
                .campusIds(List.of(defaultCampusId))
                .build();
            
            newUser.addPosition(positionService.createPosition(newUser, positionRequest));
            log.debug("Assigned default position {} with role {} to LDAP user: {}", positionType, role, email);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid default role '{}', falling back to TEACHER", defaultRole);
            
            PositionRequest positionRequest = PositionRequest.builder()
                .type(PositionRequest.PositionType.TEACHER)
                .role(Role.TEACHER)
                .campusIds(List.of(defaultCampusId))
                .build();
            
            newUser.addPosition(positionService.createPosition(newUser, positionRequest));
        }
        
        return userRepository.save(newUser);
    }

    private PositionRequest.PositionType mapRoleToPositionType(Role role) {
        return switch (role) {
            case TEACHER -> PositionRequest.PositionType.TEACHER;
            case COORDINATOR -> PositionRequest.PositionType.COORDINATOR;
            case EDUCATION_MANAGER -> PositionRequest.PositionType.EDUCATION_MANAGER;
            case ANALYST -> PositionRequest.PositionType.ANALYST;
            case ADMIN -> PositionRequest.PositionType.EDUCATION_MANAGER;
        };
    }

    @Override
    public boolean supports(String providerName) {
        return AuthProvider.LDAP.name().equalsIgnoreCase(providerName);
    }
}
