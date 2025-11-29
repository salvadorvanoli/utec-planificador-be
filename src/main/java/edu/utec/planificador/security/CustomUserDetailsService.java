package edu.utec.planificador.security;

import edu.utec.planificador.entity.User;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.UserRepository;
import edu.utec.planificador.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByUtecEmailWithPositions(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                messageService.getMessage("auth.error.invalid-credentials")
            ));

        if (!user.isEnabled()) {
            log.warn("Attempt to authenticate disabled user: {}", email);
            throw new UsernameNotFoundException(
                messageService.getMessage("auth.error.account-disabled")
            );
        }

        log.debug("User loaded successfully: {}", email);
        return user;
    }

    @Transactional(readOnly = true)
    public User loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);
        
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                messageService.getMessage("error.user.not-found")
            ));
    }
}
