package edu.utec.planificador.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "security.ldap.enabled", havingValue = "true")
public class LdapConfig {

    @Value("${security.ldap.url}")
    private String ldapUrl;

    @Value("${security.ldap.base}")
    private String ldapBase;

    @Value("${security.ldap.manager.dn:}")
    private String managerDn;

    @Value("${security.ldap.manager.password:}")
    private String managerPassword;

    @Bean
    public LdapContextSource ldapContextSource() {
        log.info("Configuring LDAP context source: {}", ldapUrl);
        
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(ldapUrl);
        contextSource.setBase(ldapBase);
        
        if (managerDn != null && !managerDn.isEmpty()) {
            contextSource.setUserDn(managerDn);
            contextSource.setPassword(managerPassword);
        }
        
        contextSource.afterPropertiesSet();
        
        log.info("LDAP context source configured successfully");
        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {
        return new LdapTemplate(ldapContextSource);
    }
}
