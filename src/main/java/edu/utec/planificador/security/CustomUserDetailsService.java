package edu.utec.planificador.security;

import edu.utec.planificador.entity.User;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.UserRepository;
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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        User user = userRepository.findByUtecEmailWithPositions(email)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Usuario no encontrado con email: " + email
            ));

        if (!user.isEnabled()) {
            log.warn("Attempt to authenticate disabled user: {}", email);
            throw new UsernameNotFoundException("La cuenta de usuario está deshabilitada");
        }

        log.debug("User loaded successfully: {}", email);
        return user;
    }

    @Transactional(readOnly = true)
    public User loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);
        
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }
}
