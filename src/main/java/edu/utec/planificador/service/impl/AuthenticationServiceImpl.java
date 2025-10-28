package edu.utec.planificador.service.impl;

import edu.utec.planificador.datatype.PersonalData;
import edu.utec.planificador.dto.request.LoginRequest;
import edu.utec.planificador.dto.request.RegisterRequest;
import edu.utec.planificador.dto.response.AuthResponse;
import edu.utec.planificador.dto.response.UserResponse;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.exception.DuplicateResourceException;
import edu.utec.planificador.exception.InvalidCredentialsException;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.security.JwtTokenProvider;
import edu.utec.planificador.security.LoginAttemptService;
import edu.utec.planificador.service.AuthenticationService;
import edu.utec.planificador.service.AuthenticationStrategy;
import edu.utec.planificador.service.PositionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final List<AuthenticationStrategy> authenticationStrategies;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;
    private final MessageSource messageSource;
    private final PositionService positionService;

    @Value("${security.auth.default-provider:LOCAL}")
    private String defaultAuthProvider;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();
        String clientIP = getClientIP();
        
        log.info("Login attempt for user: {} from IP: {}", email, clientIP);

        if (loginAttemptService.isBlocked(clientIP, false)) {
            long remainingTime = loginAttemptService.getRemainingLockoutTime(clientIP, false);
            log.warn("Blocked login attempt from IP: {} ({} minutes remaining)", clientIP, remainingTime);
            throw new InvalidCredentialsException(
                messageSource.getMessage("auth.error.too-many-attempts", 
                    new Object[]{remainingTime}, 
                    LocaleContextHolder.getLocale())
            );
        }

        if (loginAttemptService.isBlocked(email, true)) {
            long remainingTime = loginAttemptService.getRemainingLockoutTime(email, true);
            log.warn("Blocked login attempt for user: {} ({} minutes remaining)", email, remainingTime);
            throw new InvalidCredentialsException(
                messageSource.getMessage("auth.error.account-locked", 
                    new Object[]{remainingTime}, 
                    LocaleContextHolder.getLocale())
            );
        }

        try {
            String providerName = determineAuthProvider(email);

            AuthenticationStrategy strategy = authenticationStrategies.stream()
                .filter(s -> s.supports(providerName))
                .findFirst()
                .orElseThrow(() -> new InvalidCredentialsException(
                    messageSource.getMessage("auth.error.no-strategy", 
                        new Object[]{providerName}, 
                        LocaleContextHolder.getLocale())
                ));

            User user = strategy.authenticate(email, loginRequest.getPassword());

            loginAttemptService.loginSucceeded(clientIP, false);
            loginAttemptService.loginSucceeded(email, true);

            updateLastLogin(user);

            String token = tokenProvider.generateTokenFromUser(user);

            log.info("User logged in successfully: {} from IP: {}", email, clientIP);

            return buildAuthResponse(token, user);
            
        } catch (InvalidCredentialsException e) {
            loginAttemptService.loginFailed(clientIP, false);
            loginAttemptService.loginFailed(email, true);
            throw e;
        }
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Registration attempt for user: {}", registerRequest.getEmail());

        if (userRepository.existsByUtecEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Usuario", "email", registerRequest.getEmail());
        }

        PersonalData personalData = new PersonalData();
        String[] nameParts = registerRequest.getFullName().split(" ", 2);
        personalData.setName(nameParts.length > 0 ? nameParts[0] : registerRequest.getFullName());
        personalData.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        personalData.setIdentityDocument(registerRequest.getCi());
        personalData.setPhoneNumber(registerRequest.getPhone());

        final User user = new User(
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                personalData
        );

        user.setAuthProvider(AuthProvider.LOCAL);
        user.setEnabled(true);

        registerRequest.getPositions().forEach(positionRequest -> {
            Position position = positionService.createPosition(user, positionRequest);
            user.addPosition(position);
        });

        User savedUser = userRepository.save(user);

        String token = tokenProvider.generateTokenFromUser(savedUser);

        log.info("User registered successfully: {}", registerRequest.getEmail());

        return buildAuthResponse(token, savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidCredentialsException(
                messageSource.getMessage("auth.error.user-not-authenticated", 
                    null, 
                    LocaleContextHolder.getLocale())
            );
        }

        User user = (User) authentication.getPrincipal();

        return buildUserResponse(user);
    }

    @Override
    @Transactional
    public void updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private String determineAuthProvider(String email) {
        return userRepository.findByUtecEmail(email)
                .map(user -> user.getAuthProvider().name())
                .orElse(defaultAuthProvider);
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        String[] roleNames = user.getPositions().stream()
            .filter(position -> position.getIsActive())
            .map(position -> position.getRole().name())
            .distinct()
            .toArray(String[]::new);

        String fullName = null;
        if (user.getPersonalData() != null) {
            String name = user.getPersonalData().getName() != null ? user.getPersonalData().getName() : "";
            String lastName = user.getPersonalData().getLastName() != null ? user.getPersonalData().getLastName() : "";
            fullName = (name + " " + lastName).trim();
        }

        return AuthResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(tokenProvider.getExpirationMs() / 1000)
            .email(user.getUtecEmail())
            .fullName(fullName)
            .roles(roleNames)
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }

    private UserResponse buildUserResponse(User user) {
        String fullName = null;
        String ci = null;
        String phone = null;

        if (user.getPersonalData() != null) {
            String name = user.getPersonalData().getName() != null ? user.getPersonalData().getName() : "";
            String lastName = user.getPersonalData().getLastName() != null ? user.getPersonalData().getLastName() : "";
            fullName = (name + " " + lastName).trim();
            ci = user.getPersonalData().getIdentityDocument();
            phone = user.getPersonalData().getPhoneNumber();
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getUtecEmail())
                .fullName(fullName)
                .ci(ci)
                .phone(phone)
                .authProvider(user.getAuthProvider())
                .roles(
                    user.getPositions().stream()
                    .filter(position -> position.getIsActive())
                    .map(position -> position.getRole())
                    .distinct()
                    .collect(java.util.stream.Collectors.toSet())
                )
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
