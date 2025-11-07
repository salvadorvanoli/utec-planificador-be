package edu.utec.planificador.config;

import edu.utec.planificador.datatype.PersonalData;
import edu.utec.planificador.entity.Activity;
import edu.utec.planificador.entity.Administrator;
import edu.utec.planificador.entity.Analyst;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Coordinator;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.CurricularUnit;
import edu.utec.planificador.entity.EducationManager;
import edu.utec.planificador.entity.Program;
import edu.utec.planificador.entity.ProgrammaticContent;
import edu.utec.planificador.entity.RegionalTechnologicalInstitute;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.Term;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.entity.WeeklyPlanning;
import edu.utec.planificador.enumeration.CognitiveProcess;
import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.LearningModality;
import edu.utec.planificador.enumeration.LearningResource;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.Shift;
import edu.utec.planificador.enumeration.SustainableDevelopmentGoal;
import edu.utec.planificador.repository.CampusRepository;
import edu.utec.planificador.enumeration.TeachingStrategy;
import edu.utec.planificador.enumeration.TransversalCompetency;
import edu.utec.planificador.enumeration.UniversalDesignLearningPrinciple;
import edu.utec.planificador.repository.RegionalTechnologicalInstituteRepository;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.CurricularUnitRepository;
import edu.utec.planificador.repository.ProgramRepository;
import edu.utec.planificador.repository.TermRepository;
import edu.utec.planificador.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@Component
@Profile({"dev"}) // Solo se ejecuta en el perfil 'dev'
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final TermRepository termRepository;
    private final CurricularUnitRepository curricularUnitRepository;
    private final CourseRepository courseRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegionalTechnologicalInstituteRepository rtiRepository;
    private final CampusRepository campusRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("==================================================");
        log.info("Starting data seeding...");
        log.info("==================================================");

        // Verificar si ya existen datos
        if (courseRepository.count() > 0) {
            log.info("Data already exists. Skipping seeding.");
            log.info("==================================================");
            return;
        }

        seedData();

        log.info("==================================================");
        log.info("Data seeding completed successfully!");
        log.info("==================================================");
    }

    private void seedData() {
        log.info("Creating Regional Technological Institutes (RTI)...");
        RegionalTechnologicalInstitute rtiMontevideo = new RegionalTechnologicalInstitute();
        rtiMontevideo.setName("ITR Montevideo");
        rtiMontevideo = rtiRepository.save(rtiMontevideo);
        log.info("✓ Created RTI: {} (ID: {})", rtiMontevideo.getName(), rtiMontevideo.getId());

        RegionalTechnologicalInstitute rtiNorte = new RegionalTechnologicalInstitute();
        rtiNorte.setName("ITR Norte");
        rtiNorte = rtiRepository.save(rtiNorte);
        log.info("✓ Created RTI: {} (ID: {})", rtiNorte.getName(), rtiNorte.getId());

        RegionalTechnologicalInstitute rtiSur = new RegionalTechnologicalInstitute();
        rtiSur.setName("ITR Sur");
        rtiSur = rtiRepository.save(rtiSur);
        log.info("✓ Created RTI: {} (ID: {})", rtiSur.getName(), rtiSur.getId());

        log.info("Creating Campuses...");
        Campus campusCentro = new Campus();
        campusCentro.setName("Campus Centro");
        campusCentro.setRegionalTechnologicalInstitute(rtiMontevideo);
        campusCentro = campusRepository.save(campusCentro);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusCentro.getName(), rtiMontevideo.getName(), campusCentro.getId());

        Campus campusPocitos = new Campus();
        campusPocitos.setName("Campus Pocitos");
        campusPocitos.setRegionalTechnologicalInstitute(rtiMontevideo);
        campusPocitos = campusRepository.save(campusPocitos);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusPocitos.getName(), rtiMontevideo.getName(), campusPocitos.getId());

        Campus campusRivera = new Campus();
        campusRivera.setName("Campus Rivera");
        campusRivera.setRegionalTechnologicalInstitute(rtiNorte);
        campusRivera = campusRepository.save(campusRivera);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusRivera.getName(), rtiNorte.getName(), campusRivera.getId());

        Campus campusMaldonado = new Campus();
        campusMaldonado.setName("Campus Maldonado");
        campusMaldonado.setRegionalTechnologicalInstitute(rtiSur);
        campusMaldonado = campusRepository.save(campusMaldonado);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusMaldonado.getName(), rtiSur.getName(), campusMaldonado.getId());

        log.info("Creating user...");
        PersonalData personalData = new PersonalData();
        personalData.setName("Juan");
        personalData.setLastName("Pérez");
        personalData.setIdentityDocument("12345678");
        personalData.setPhoneNumber("099123456");
        personalData.setCountry("Uruguay");
        personalData.setCity("Montevideo");

        User user = new User(
            "juan.perez@utec.edu.uy",
            passwordEncoder.encode("password"),
            personalData
        );
        user = userRepository.save(user);
        log.info("✓ Created user: {} (ID: {})", user.getUtecEmail(), user.getId());

        log.info("Creating positions for user...");

        Teacher teacherMontevideo = new Teacher(user);
        teacherMontevideo.addCampus(campusCentro);
        teacherMontevideo.addCampus(campusPocitos);
        user.addPosition(teacherMontevideo);
        log.info("✓ Created Teacher position for {} at ITR Montevideo (Campuses: Centro, Pocitos)", user.getUtecEmail());

        Coordinator coordinatorMontevideo = new Coordinator(user);
        coordinatorMontevideo.addCampus(campusCentro);
        user.addPosition(coordinatorMontevideo);
        log.info("✓ Created Coordinator position for {} at ITR Montevideo (Campus: Centro)", user.getUtecEmail());

        Administrator administratorMontevideo = new Administrator(user);
        administratorMontevideo.addCampus(campusCentro);
        user.addPosition(administratorMontevideo);
        log.info("✓ Created Administrator position for {} at ITR Montevideo (Campus: Centro)", user.getUtecEmail());

        EducationManager educationManagerMontevideo = new EducationManager(user);
        educationManagerMontevideo.addCampus(campusCentro);
        user.addPosition(educationManagerMontevideo);
        log.info("✓ Created Education Manager position for {} at ITR Montevideo (Campus: Centro)", user.getUtecEmail());

        Analyst analystMontevideo = new Analyst(user);
        analystMontevideo.addCampus(campusCentro);
        user.addPosition(analystMontevideo);
        log.info("✓ Created Analyst position for {} at ITR Montevideo (Campus: Centro)", user.getUtecEmail());

        Teacher teacherNorte = new Teacher(user);
        teacherNorte.addCampus(campusRivera);
        user.addPosition(teacherNorte);
        log.info("✓ Created Teacher position for {} at ITR Norte (Campus: Rivera)", user.getUtecEmail());

        Teacher teacherSur = new Teacher(user);
        teacherSur.addCampus(campusMaldonado);
        user.addPosition(teacherSur);
        log.info("✓ Created Teacher position for {} at ITR Sur (Campus: Maldonado)", user.getUtecEmail());

        user = userRepository.save(user);
        log.info("✓ Saved all positions for user: {}", user.getUtecEmail());

        log.info("Creating program...");
        Program program = new Program(
            "Ingeniería en Tecnologías de la Información",
            8,
            240
        );
        program = programRepository.save(program);
        log.info("✓ Created program: {} (ID: {})", program.getName(), program.getId());

        log.info("Creating term...");
        Term term = new Term(1, program);
        term = termRepository.save(term);
        log.info("✓ Created term: Semestre {} (ID: {})", term.getNumber(), term.getId());

        log.info("Creating curricular unit...");
        CurricularUnit curricularUnit = new CurricularUnit(
            "Programación Avanzada",
            8,
            term
        );
        curricularUnit = curricularUnitRepository.save(curricularUnit);
        log.info("✓ Created curricular unit: {} - {} créditos (ID: {})", 
            curricularUnit.getName(), 
            curricularUnit.getCredits(), 
            curricularUnit.getId());

        log.info("Creating course...");
        Course course = new Course(
            Shift.MORNING,
            "Curso de Programación Avanzada - Grupo 1",
            LocalDate.of(2025, 3, 1),
            LocalDate.of(2025, 7, 15),
            PartialGradingSystem.PGS_1,
            curricularUnit
        );
        
        final Campus finalCampusCentro = campusCentro;
        teacherMontevideo = (Teacher) user.getPositions().stream()
            .filter(p -> p instanceof Teacher && p.getCampuses().contains(finalCampusCentro))
            .findFirst()
            .orElseThrow();

        course.getTeachers().add(teacherMontevideo);

        course.getHoursPerDeliveryFormat().put(DeliveryFormat.IN_PERSON, 60);
        course.getHoursPerDeliveryFormat().put(DeliveryFormat.VIRTUAL, 20);
        course.getHoursPerDeliveryFormat().put(DeliveryFormat.HYBRID, 10);

        course.getSustainableDevelopmentGoals().add(SustainableDevelopmentGoal.SDG_4);
        course.getSustainableDevelopmentGoals().add(SustainableDevelopmentGoal.SDG_9);
        course.getSustainableDevelopmentGoals().add(SustainableDevelopmentGoal.SDG_8);

        course.getUniversalDesignLearningPrinciples().add(UniversalDesignLearningPrinciple.MEANS_OF_REPRESENTATION);
        course.getUniversalDesignLearningPrinciples().add(UniversalDesignLearningPrinciple.MEANS_OF_ACTION_EXPRESSION);
        course.getUniversalDesignLearningPrinciples().add(UniversalDesignLearningPrinciple.MEANS_OF_ENGAGEMENT);

        course.setIsRelatedToInvestigation(true);
        course.setInvolvesActivitiesWithProductiveSector(true);

        course = courseRepository.save(course);
        log.info("✓ Created course: {} (ID: {})", course.getDescription(), course.getId());
        log.info("  - Shift: {}", course.getShift());
        log.info("  - Start date: {}", course.getStartDate());
        log.info("  - End date: {}", course.getEndDate());
        log.info("  - Teacher: {}", user.getUtecEmail());

        createWeeklyPlanningsWithContent(course);
        
        log.info("Data seeding completed successfully");
        log.info("");
        log.info("==================================================");
        log.info("Test Data Summary:");
        log.info("==================================================");
        log.info("User: juan.perez@utec.edu.uy");
        log.info("Password: password");
        log.info("");
        log.info("Positions:");
        log.info("  - Teacher at ITR Montevideo (Campuses: Centro, Pocitos)");
        log.info("  - Coordinator at ITR Montevideo (Campus: Centro)");
        log.info("  - Administrator at ITR Montevideo (Campus: Centro)");
        log.info("  - Education Manager at ITR Montevideo (Campus: Centro)");
        log.info("  - Analyst at ITR Montevideo (Campus: Centro)");
        log.info("  - Teacher at ITR Norte (Campus: Rivera)");
        log.info("  - Teacher at ITR Sur (Campus: Maldonado)");
        log.info("");
        log.info("Campus Centro has ALL roles!");
        log.info("");
        log.info("Test endpoint: GET /api/v1/user/positions");
        log.info("==================================================");
    }

    private void createWeeklyPlanningsWithContent(Course course) {
        log.info("Creating WeeklyPlannings with content for course: {}", course.getDescription());
        
        // Semana 1: 2025-03-03 al 2025-03-09
        WeeklyPlanning week1 = new WeeklyPlanning(
            1,
            LocalDate.of(2025, 3, 3),
            LocalDate.of(2025, 3, 9)
        );
        
        // Contenidos para semana 1
        ProgrammaticContent content1Week1 = new ProgrammaticContent(
            "Introducción a POO",
            "Introducción a Programación Orientada a Objetos - Conceptos fundamentales de POO: clases, objetos, encapsulación",
            week1
        );
        content1Week1.setColor("#4A90E2");
        
        Activity activity1Content1 = new Activity(
            "Clase magistral sobre POO - Presentación de conceptos con ejemplos en Java",
            120,
            LearningModality.IN_PERSON,
            content1Week1
        );
        activity1Content1.setTitle("Clase Magistral POO");
        activity1Content1.setColor("#2ECC71");
        activity1Content1.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.REMEMBER,
            CognitiveProcess.UNDERSTAND
        ));
        activity1Content1.getLearningResources().add(LearningResource.DEMONSTRATION);
        activity1Content1.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);
        activity1Content1.getTeachingStrategies().add(TeachingStrategy.LECTURE);
        
        Activity activity2Content1 = new Activity(
            "Ejercicios prácticos - Implementación de clases simples",
            180,
            LearningModality.IN_PERSON,
            content1Week1
        );
        activity2Content1.setTitle("Práctica de Clases");
        activity2Content1.setColor("#E67E22");
        activity2Content1.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.APPLY,
            CognitiveProcess.ANALYZE
        ));
        activity2Content1.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity2Content1.getTransversalCompetencies().add(TransversalCompetency.LEARNING_SELF_REGULATION);
        activity2Content1.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);
        
        content1Week1.getActivities().add(activity1Content1);
        content1Week1.getActivities().add(activity2Content1);
        
        ProgrammaticContent content2Week1 = new ProgrammaticContent(
            "Herencia y Polimorfismo",
            "Herencia y Polimorfismo - Reutilización de código mediante herencia",
            week1
        );
        content2Week1.setColor("#9B59B6");
        
        Activity activity1Content2 = new Activity(
            "Laboratorio de herencia - Crear jerarquías de clases",
            150,
            LearningModality.IN_PERSON,
            content2Week1
        );
        activity1Content2.setTitle("Lab Herencia");
        activity1Content2.setColor("#1ABC9C");
        activity1Content2.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        activity1Content2.getLearningResources().add(LearningResource.DEMONSTRATION);
        activity1Content2.getTransversalCompetencies().add(TransversalCompetency.TEAMWORK);
        activity1Content2.getTeachingStrategies().add(TeachingStrategy.LABORATORY_PRACTICES);
        
        content2Week1.getActivities().add(activity1Content2);
        
        week1.getProgrammaticContents().add(content1Week1);
        week1.getProgrammaticContents().add(content2Week1);
        course.getWeeklyPlannings().add(week1);
        
        // Semana 2: 2025-03-10 al 2025-03-16
        WeeklyPlanning week2 = new WeeklyPlanning(
            2,
            LocalDate.of(2025, 3, 10),
            LocalDate.of(2025, 3, 16)
        );
        
        ProgrammaticContent content1Week2 = new ProgrammaticContent(
            "Interfaces y Clases Abstractas",
            "Interfaces y Clases Abstractas - Contratos y abstracción en Java",
            week2
        );
        content1Week2.setColor("#F39C12");
        
        Activity activity1Week2 = new Activity(
            "Análisis de casos de uso - Identificar cuándo usar interfaces vs clases abstractas",
            120,
            LearningModality.IN_PERSON,
            content1Week2
        );
        activity1Week2.setTitle("Análisis Interfaces");
        activity1Week2.setColor("#3498DB");
        activity1Week2.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.ANALYZE,
            CognitiveProcess.EVALUATE
        ));
        activity1Week2.getLearningResources().addAll(Arrays.asList(
            LearningResource.DEMONSTRATION,
            LearningResource.BOOK_DOCUMENT
        ));
        activity1Week2.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);
        activity1Week2.getTeachingStrategies().add(TeachingStrategy.CASE_STUDY);
        
        Activity activity2Week2 = new Activity(
            "Implementación práctica - Crear interfaces y clases abstractas",
            180,
            LearningModality.IN_PERSON,
            content1Week2
        );
        activity2Week2.setTitle("Implementación Interfaces");
        activity2Week2.setColor("#E74C3C");
        activity2Week2.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        activity2Week2.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity2Week2.getTransversalCompetencies().add(TransversalCompetency.LEARNING_SELF_REGULATION);
        activity2Week2.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);
        
        content1Week2.getActivities().add(activity1Week2);
        content1Week2.getActivities().add(activity2Week2);
        
        ProgrammaticContent content2Week2 = new ProgrammaticContent(
            "Excepciones y Manejo de Errores",
            "Excepciones y Manejo de Errores - Técnicas para manejo robusto de errores",
            week2
        );
        content2Week2.setColor("#16A085");
        
        Activity activity3Week2 = new Activity(
            "Práctica de try-catch - Implementar manejo de excepciones",
            120,
            LearningModality.IN_PERSON,
            content2Week2
        );
        activity3Week2.setTitle("Práctica Excepciones");
        activity3Week2.setColor("#8E44AD");
        activity3Week2.getCognitiveProcesses().add(CognitiveProcess.APPLY);
        activity3Week2.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity3Week2.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);
        activity3Week2.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);
        
        content2Week2.getActivities().add(activity3Week2);
        
        week2.getProgrammaticContents().add(content1Week2);
        week2.getProgrammaticContents().add(content2Week2);
        course.getWeeklyPlannings().add(week2);
        
        // Semana 3: 2025-03-17 al 2025-03-23
        WeeklyPlanning week3 = new WeeklyPlanning(
            3,
            LocalDate.of(2025, 3, 17),
            LocalDate.of(2025, 3, 23)
        );
        
        ProgrammaticContent content1Week3 = new ProgrammaticContent(
            "Colecciones y Genéricos",
            "Colecciones y Genéricos - ArrayList, HashMap, Sets y uso de genéricos",
            week3
        );
        content1Week3.setColor("#D35400");
        
        Activity activity1Week3 = new Activity(
            "Presentación de Collections Framework - API de colecciones en Java",
            90,
            LearningModality.IN_PERSON,
            content1Week3
        );
        activity1Week3.setTitle("Intro Collections");
        activity1Week3.setColor("#27AE60");
        activity1Week3.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.REMEMBER,
            CognitiveProcess.UNDERSTAND
        ));
        activity1Week3.getLearningResources().add(LearningResource.DEMONSTRATION);
        activity1Week3.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);
        activity1Week3.getTeachingStrategies().add(TeachingStrategy.LECTURE);
        
        Activity activity2Week3 = new Activity(
            "Laboratorio de colecciones - Implementar estructuras de datos con colecciones",
            210,
            LearningModality.IN_PERSON,
            content1Week3
        );
        activity2Week3.setTitle("Lab Colecciones");
        activity2Week3.setColor("#C0392B");
        activity2Week3.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.APPLY,
            CognitiveProcess.CREATE
        ));
        activity2Week3.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity2Week3.getTransversalCompetencies().addAll(Arrays.asList(
            TransversalCompetency.LEARNING_SELF_REGULATION,
            TransversalCompetency.TEAMWORK
        ));
        activity2Week3.getTeachingStrategies().add(TeachingStrategy.LABORATORY_PRACTICES);
        
        Activity activity3Week3 = new Activity(
            "Tarea asincrónica - Ejercicios de genéricos para entregar",
            240,
            LearningModality.AUTONOMOUS,
            content1Week3
        );
        activity3Week3.setTitle("Tarea Genéricos");
        activity3Week3.setColor("#2980B9");
        activity3Week3.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        activity3Week3.getLearningResources().add(LearningResource.ONLINE_EVALUATION);
        activity3Week3.getTransversalCompetencies().addAll(Arrays.asList(
            TransversalCompetency.LEARNING_SELF_REGULATION,
            TransversalCompetency.COMMUNICATION
        ));
        activity3Week3.getTeachingStrategies().add(TeachingStrategy.PROJECTS);
        
        content1Week3.getActivities().add(activity1Week3);
        content1Week3.getActivities().add(activity2Week3);
        content1Week3.getActivities().add(activity3Week3);
        
        week3.getProgrammaticContents().add(content1Week3);
        course.getWeeklyPlannings().add(week3);
        
        // Semana 4: 2025-03-24 al 2025-03-30
        WeeklyPlanning week4 = new WeeklyPlanning(
            4,
            LocalDate.of(2025, 3, 24),
            LocalDate.of(2025, 3, 30)
        );
        
        ProgrammaticContent content1Week4 = new ProgrammaticContent(
            "Patrones de Diseño",
            "Patrones de Diseño - Singleton, Factory, Observer y otros patrones",
            week4
        );
        content1Week4.setColor("#8E44AD");
        
        Activity activity1Week4 = new Activity(
            "Estudio de patrones - Análisis de patrones GOF",
            120,
            LearningModality.IN_PERSON,
            content1Week4
        );
        activity1Week4.setTitle("Análisis Patrones");
        activity1Week4.setColor("#F39C12");
        activity1Week4.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.UNDERSTAND,
            CognitiveProcess.ANALYZE
        ));
        activity1Week4.getLearningResources().addAll(Arrays.asList(
            LearningResource.DEMONSTRATION,
            LearningResource.BOOK_DOCUMENT
        ));
        activity1Week4.getTransversalCompetencies().addAll(Arrays.asList(
            TransversalCompetency.CRITICAL_THINKING,
            TransversalCompetency.COMMUNICATION
        ));
        activity1Week4.getTeachingStrategies().add(TeachingStrategy.CASE_STUDY);
        
        Activity activity2Week4 = new Activity(
            "Trabajo en equipo - Implementar patrones en proyecto grupal",
            180,
            LearningModality.IN_PERSON,
            content1Week4
        );
        activity2Week4.setTitle("Proyecto Patrones");
        activity2Week4.setColor("#16A085");
        activity2Week4.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.APPLY,
            CognitiveProcess.CREATE
        ));
        activity2Week4.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity2Week4.getTransversalCompetencies().addAll(Arrays.asList(
            TransversalCompetency.TEAMWORK,
            TransversalCompetency.COMMUNICATION
        ));
        activity2Week4.getTeachingStrategies().add(TeachingStrategy.TEAMWORK);
        
        content1Week4.getActivities().add(activity1Week4);
        content1Week4.getActivities().add(activity2Week4);
        
        ProgrammaticContent content2Week4 = new ProgrammaticContent(
            "Testing y JUnit",
            "Testing y JUnit - Pruebas unitarias y TDD",
            week4
        );
        content2Week4.setColor("#E74C3C");
        
        Activity activity3Week4 = new Activity(
            "Introducción a testing - Escribir tests con JUnit",
            150,
            LearningModality.IN_PERSON,
            content2Week4
        );
        activity3Week4.setTitle("Intro Testing");
        activity3Week4.setColor("#3498DB");
        activity3Week4.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.APPLY,
            CognitiveProcess.EVALUATE
        ));
        activity3Week4.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity3Week4.getTransversalCompetencies().addAll(Arrays.asList(
            TransversalCompetency.CRITICAL_THINKING,
            TransversalCompetency.LEARNING_SELF_REGULATION
        ));
        activity3Week4.getTeachingStrategies().add(TeachingStrategy.TESTS);
        
        Activity activity4Week4 = new Activity(
            "Práctica autónoma de TDD - Desarrollar con Test-Driven Development",
            300,
            LearningModality.AUTONOMOUS,
            content2Week4
        );
        activity4Week4.setTitle("Práctica TDD");
        activity4Week4.setColor("#27AE60");
        activity4Week4.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        activity4Week4.getLearningResources().add(LearningResource.ONLINE_EVALUATION);
        activity4Week4.getTransversalCompetencies().addAll(Arrays.asList(
            TransversalCompetency.LEARNING_SELF_REGULATION,
            TransversalCompetency.CRITICAL_THINKING
        ));
        activity4Week4.getTeachingStrategies().add(TeachingStrategy.PROJECTS);
        
        content2Week4.getActivities().add(activity3Week4);
        content2Week4.getActivities().add(activity4Week4);
        
        week4.getProgrammaticContents().add(content1Week4);
        week4.getProgrammaticContents().add(content2Week4);
        course.getWeeklyPlannings().add(week4);
        
        // Guardar el curso con todas las planificaciones
        courseRepository.save(course);
        log.info("Created 4 weekly plannings with programmatic contents and activities");
    }
}
