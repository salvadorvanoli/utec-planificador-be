package edu.utec.planificador.service;

import edu.utec.planificador.entity.User;

public interface AuthenticationStrategy {

    User authenticate(String email, String password);
    
    boolean supports(String providerName);
}
