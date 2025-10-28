package edu.utec.planificador.entity;

import edu.utec.planificador.enumeration.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString(exclude = {"user", "campuses"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "position")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @NotNull
    private Role role;

    @Setter
    @Column(nullable = false)
    private Boolean isActive = true;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private User user;

    @ManyToMany
    @JoinTable(
        name = "position_campus",
        joinColumns = @JoinColumn(name = "position_id"),
        inverseJoinColumns = @JoinColumn(name = "campus_id")
    )
    @OrderBy("name ASC")
    private List<Campus> campuses = new ArrayList<>();

    protected Position(Role role) {
        this.role = role;
    }

    public void addCampus(Campus campus) {
        if (campus != null && !this.campuses.contains(campus)) {
            this.campuses.add(campus);
        }
    }

    public void removeCampus(Campus campus) {
        this.campuses.remove(campus);
    }
}
