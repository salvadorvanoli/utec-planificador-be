package edu.utec.planificador.service;

import edu.utec.planificador.dto.request.LoginRequest;
import edu.utec.planificador.dto.request.RegisterRequest;
import edu.utec.planificador.dto.response.AuthResponse;
import edu.utec.planificador.dto.response.UserResponse;
import edu.utec.planificador.entity.User;

public interface AuthenticationService {

    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    UserResponse getCurrentUser();
    void updateLastLogin(User user);
}
