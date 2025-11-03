package edu.utec.planificador.exception;

public class AIAgentException extends RuntimeException {

    public AIAgentException(String message) {
        super(message);
    }

    public AIAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}

