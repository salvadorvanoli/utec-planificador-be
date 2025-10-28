package edu.utec.planificador.entity;

import edu.utec.planificador.datatype.PersonalData;
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.util.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString(exclude = {"positions", "password"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "utec_email"),
    @Index(name = "idx_user_enabled", columnList = "enabled")
})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Setter
    @Column(unique = true, nullable = false, length = Constants.MAX_EMAIL_LENGTH)
    @Size(max = Constants.MAX_EMAIL_LENGTH)
    @Pattern(regexp = Constants.EMAIL_REGEX, message = "{validation.email.utec.format}")
    private String utecEmail;

    @Setter
    @Column(nullable = true, length = Constants.MAX_PASSWORD_LENGTH)
    @Size(min = Constants.MIN_PASSWORD_LENGTH, max = Constants.MAX_PASSWORD_LENGTH)
    private String password;

    @Setter
    @Embedded
    @Valid
    private PersonalData personalData;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Setter
    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column
    private LocalDateTime updatedAt;

    @Setter
    @Column
    private LocalDateTime lastLoginAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Position> positions = new ArrayList<>();

    public User(String utecEmail, String password, PersonalData personalData) {
        this.utecEmail = utecEmail;
        this.password = password;
        this.personalData = personalData;
        this.authProvider = AuthProvider.LOCAL;
        this.enabled = true;
    }

    public void addPosition(Position position) {
        this.positions.add(position);
        position.setUser(this);
    }

    public void removePosition(Position position) {
        this.positions.remove(position);
        position.setUser(null);
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return positions.stream()
            .filter(Position::getIsActive)
            .flatMap(position -> position.getRole().getAuthorities().stream())
            .distinct()
            .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return utecEmail;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
