package edu.utec.planificador.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {

    LOCAL("Base de Datos Local"),

    LDAP("LDAP/Active Directory");

    private final String displayName;

    public boolean requiresPasswordStorage() {
        return this == LOCAL;
    }
}
