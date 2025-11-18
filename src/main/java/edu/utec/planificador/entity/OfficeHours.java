package edu.utec.planificador.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString(exclude = {"course"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "office_hours")
public class OfficeHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    public OfficeHours(LocalDate date, Integer startHour, Integer endHour, Course course) {
        this.date = date;
        this.startHour = startHour;
        this.endHour = endHour;
        this.course = course;
    }

    @Setter
    @Column(nullable = false)
    @NotNull
    private LocalDate date;

    @Setter
    @Column(nullable = false)
    @NotNull
    @Min(0)
    @Max(23)
    private Integer startHour;

    @Setter
    @Column(nullable = false)
    @NotNull
    @Min(0)
    @Max(23)
    private Integer endHour;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    @NotNull
    private Course course;
}
