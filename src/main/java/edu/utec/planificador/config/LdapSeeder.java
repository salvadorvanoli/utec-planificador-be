package edu.utec.planificador.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;

import javax.naming.Name;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import java.util.List;

/**
 * Seeder for loading test users into LDAP server.
 * Only runs in 'dev' profile when LDAP is enabled.
 * Executes before DataSeeder to ensure LDAP users exist when database users are created.
 */
@Slf4j
@Component
@Profile({"dev"})
@Order(1)
@ConditionalOnProperty(name = "security.ldap.enabled", havingValue = "true")
public class LdapSeeder implements CommandLineRunner {

    private final LdapTemplate ldapTemplate;

    @Value("${security.ldap.user-base:ou=people}")
    private String userBase;

    @Autowired
    public LdapSeeder(@Autowired(required = false) LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    @Override
    public void run(String... args) {
        if (ldapTemplate == null) {
            log.warn("LDAP template is not configured. Skipping LDAP seeding.");
            return;
        }

        log.info("==================================================");
        log.info("Starting LDAP user seeding...");
        log.info("==================================================");

        try {
            // Verificar si la OU existe, si no, crearla
            ensureOrganizationalUnitExists();

            // Cargar usuarios de prueba
            seedLdapUsers();

            log.info("==================================================");
            log.info("LDAP user seeding completed successfully!");
            log.info("==================================================");

        } catch (Exception e) {
            log.error("Error during LDAP seeding: {}", e.getMessage(), e);
            log.warn("LDAP seeding failed, but application will continue...");
        }
    }

    private void ensureOrganizationalUnitExists() {
        try {
            // Intentar buscar la OU
            ldapTemplate.search(
                userBase,
                "(objectClass=organizationalUnit)",
                new AbstractContextMapper<String>() {
                    @Override
                    protected String doMapFromContext(DirContextOperations ctx) {
                        return ctx.getNameInNamespace();
                    }
                }
            );
            log.info("✓ Organizational unit '{}' already exists", userBase);

        } catch (Exception e) {
            log.info("Creating organizational unit '{}'...", userBase);
            try {
                Name ouName = LdapNameBuilder.newInstance(userBase).build();
                
                Attributes ouAttributes = new BasicAttributes();
                ouAttributes.put(new BasicAttribute("objectClass", "organizationalUnit"));
                ouAttributes.put(new BasicAttribute("ou", "people"));
                
                DirContext context = ldapTemplate.getContextSource().getReadWriteContext();
                context.createSubcontext(ouName, ouAttributes);
                context.close();
                
                log.info("✓ Created organizational unit '{}'", userBase);
            } catch (Exception ex) {
                log.warn("Could not create organizational unit: {}", ex.getMessage());
            }
        }
    }

    private void seedLdapUsers() {
        // Usuario 1: María González (corresponde a user2 en DataSeeder)
        createLdapUserIfNotExists(
            "maria.gonzalez",
            "María González",
            "González",
            "maria.gonzalez@utec.edu.uy",
            "ldap123"
        );

        // Usuario 2: Test LDAP (para testing, no está en BD)
        createLdapUserIfNotExists(
            "test.ldap",
            "Test LDAP",
            "LDAP",
            "test.ldap@utec.edu.uy",
            "testldap123"
        );

        // Usuario 3: Docente LDAP (para testing adicional)
        createLdapUserIfNotExists(
            "docente.ldap",
            "Docente LDAP",
            "Ejemplo",
            "docente.ldap@utec.edu.uy",
            "docente123"
        );
    }

    private void createLdapUserIfNotExists(
        String uid,
        String cn,
        String sn,
        String mail,
        String password
    ) {
        try {
            // Verificar si el usuario ya existe
            EqualsFilter filter = new EqualsFilter("uid", uid);
            List<String> users = ldapTemplate.search(
                userBase,
                filter.encode(),
                new AbstractContextMapper<String>() {
                    @Override
                    protected String doMapFromContext(DirContextOperations ctx) {
                        return ctx.getNameInNamespace();
                    }
                }
            );

            if (!users.isEmpty()) {
                log.info("✓ User '{}' already exists in LDAP", uid);
                return;
            }

            // Crear el usuario
            Name userDn = LdapNameBuilder.newInstance(userBase)
                .add("uid", uid)
                .build();

            Attributes userAttributes = new BasicAttributes();
            
            // objectClass
            BasicAttribute objectClass = new BasicAttribute("objectClass");
            objectClass.add("inetOrgPerson");
            objectClass.add("organizationalPerson");
            objectClass.add("person");
            objectClass.add("top");
            userAttributes.put(objectClass);
            
            // Atributos del usuario
            userAttributes.put(new BasicAttribute("uid", uid));
            userAttributes.put(new BasicAttribute("cn", cn));
            userAttributes.put(new BasicAttribute("sn", sn));
            userAttributes.put(new BasicAttribute("mail", mail));
            userAttributes.put(new BasicAttribute("userPassword", password));

            DirContext context = ldapTemplate.getContextSource().getReadWriteContext();
            context.createSubcontext(userDn, userAttributes);
            context.close();

            log.info("✓ Created LDAP user: {} ({})", cn, mail);

        } catch (Exception e) {
            log.error("Failed to create LDAP user '{}': {}", uid, e.getMessage());
        }
    }
}
