package edu.utec.planificador.entity;

import edu.utec.planificador.util.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString(exclude = {"activities"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "programmatic_content")
public class ProgrammaticContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    public ProgrammaticContent(String title, String content, WeeklyPlanning weeklyPlanning) {
        this.title = title;
        this.content = content;
        this.weeklyPlanning = weeklyPlanning;
    }

    @Setter
    @Column(nullable = false, length = 200)
    @NotBlank
    @Size(max = 200)
    private String title;

    @Setter
    @Column(nullable = false, length = Constants.MAX_PROGRAMMATIC_CONTENT_LENGTH)
    @NotBlank
    @Size(max = Constants.MAX_PROGRAMMATIC_CONTENT_LENGTH)
    private String content;

    @Setter
    @Column(length = 7)
    @Size(max = 7)
    private String color;

    @OneToMany(mappedBy = "programmaticContent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @Setter
    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "weekly_planning_id", nullable = false)
    private WeeklyPlanning weeklyPlanning;
}
