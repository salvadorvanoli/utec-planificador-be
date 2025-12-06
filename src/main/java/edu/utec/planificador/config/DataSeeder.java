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
import edu.utec.planificador.enumeration.AuthProvider;
import edu.utec.planificador.enumeration.CognitiveProcess;
import edu.utec.planificador.enumeration.DeliveryFormat;
import edu.utec.planificador.enumeration.DomainArea;
import edu.utec.planificador.enumeration.LearningModality;
import edu.utec.planificador.enumeration.LearningResource;
import edu.utec.planificador.enumeration.PartialGradingSystem;
import edu.utec.planificador.enumeration.ProfessionalCompetency;
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
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;

@Slf4j
@Component
@Profile({"dev"}) // Solo se ejecuta en el perfil 'dev'
@Order(2)
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
        // ========================================
        // CREACIÓN DE ITRs Y SEDES (CAMPUS)
        // ========================================
        log.info("Creating Regional Technological Institutes (RTI)...");

        // ITR SUROESTE
        RegionalTechnologicalInstitute rtiSuroeste = new RegionalTechnologicalInstitute();
        rtiSuroeste.setName("ITR Suroeste");
        rtiSuroeste = rtiRepository.save(rtiSuroeste);
        log.info("✓ Created RTI: {} (ID: {})", rtiSuroeste.getName(), rtiSuroeste.getId());

        // ITR CENTRO-SUR
        RegionalTechnologicalInstitute rtiCentroSur = new RegionalTechnologicalInstitute();
        rtiCentroSur.setName("ITR Centro-Sur");
        rtiCentroSur = rtiRepository.save(rtiCentroSur);
        log.info("✓ Created RTI: {} (ID: {})", rtiCentroSur.getName(), rtiCentroSur.getId());

        // ITR NORTE
        RegionalTechnologicalInstitute rtiNorte = new RegionalTechnologicalInstitute();
        rtiNorte.setName("ITR Norte");
        rtiNorte = rtiRepository.save(rtiNorte);
        log.info("✓ Created RTI: {} (ID: {})", rtiNorte.getName(), rtiNorte.getId());

        // ITR ESTE
        RegionalTechnologicalInstitute rtiEste = new RegionalTechnologicalInstitute();
        rtiEste.setName("ITR Este");
        rtiEste = rtiRepository.save(rtiEste);
        log.info("✓ Created RTI: {} (ID: {})", rtiEste.getName(), rtiEste.getId());

        log.info("Creating Campuses for each ITR...");

        // ========== CAMPUS ITR SUROESTE ==========
        Campus campusFrayBentos = new Campus();
        campusFrayBentos.setName("Fray Bentos");
        campusFrayBentos.setRegionalTechnologicalInstitute(rtiSuroeste);
        campusFrayBentos = campusRepository.save(campusFrayBentos);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusFrayBentos.getName(), rtiSuroeste.getName(), campusFrayBentos.getId());

        Campus campusMercedes = new Campus();
        campusMercedes.setName("Mercedes");
        campusMercedes.setRegionalTechnologicalInstitute(rtiSuroeste);
        campusMercedes = campusRepository.save(campusMercedes);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusMercedes.getName(), rtiSuroeste.getName(), campusMercedes.getId());

        Campus campusPaysandu = new Campus();
        campusPaysandu.setName("Paysandú");
        campusPaysandu.setRegionalTechnologicalInstitute(rtiSuroeste);
        campusPaysandu = campusRepository.save(campusPaysandu);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusPaysandu.getName(), rtiSuroeste.getName(), campusPaysandu.getId());

        Campus campusColoniaPaz = new Campus();
        campusColoniaPaz.setName("Colonia La Paz");
        campusColoniaPaz.setRegionalTechnologicalInstitute(rtiSuroeste);
        campusColoniaPaz = campusRepository.save(campusColoniaPaz);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusColoniaPaz.getName(), rtiSuroeste.getName(), campusColoniaPaz.getId());

        Campus campusNuevaHelvecia = new Campus();
        campusNuevaHelvecia.setName("Nueva Helvecia");
        campusNuevaHelvecia.setRegionalTechnologicalInstitute(rtiSuroeste);
        campusNuevaHelvecia = campusRepository.save(campusNuevaHelvecia);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusNuevaHelvecia.getName(), rtiSuroeste.getName(), campusNuevaHelvecia.getId());

        // ========== CAMPUS ITR CENTRO-SUR ==========
        Campus campusDurazno = new Campus();
        campusDurazno.setName("Durazno");
        campusDurazno.setRegionalTechnologicalInstitute(rtiCentroSur);
        campusDurazno = campusRepository.save(campusDurazno);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusDurazno.getName(), rtiCentroSur.getName(), campusDurazno.getId());

        Campus campusSanJose = new Campus();
        campusSanJose.setName("San José");
        campusSanJose.setRegionalTechnologicalInstitute(rtiCentroSur);
        campusSanJose = campusRepository.save(campusSanJose);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusSanJose.getName(), rtiCentroSur.getName(), campusSanJose.getId());

        // ========== CAMPUS ITR NORTE ==========
        Campus campusRivera = new Campus();
        campusRivera.setName("Rivera");
        campusRivera.setRegionalTechnologicalInstitute(rtiNorte);
        campusRivera = campusRepository.save(campusRivera);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusRivera.getName(), rtiNorte.getName(), campusRivera.getId());

        Campus campusMelo = new Campus();
        campusMelo.setName("Melo");
        campusMelo.setRegionalTechnologicalInstitute(rtiNorte);
        campusMelo = campusRepository.save(campusMelo);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusMelo.getName(), rtiNorte.getName(), campusMelo.getId());

        // ========== CAMPUS ITR ESTE ==========
        Campus campusMinas = new Campus();
        campusMinas.setName("Minas");
        campusMinas.setRegionalTechnologicalInstitute(rtiEste);
        campusMinas = campusRepository.save(campusMinas);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusMinas.getName(), rtiEste.getName(), campusMinas.getId());

        Campus campusMaldonado = new Campus();
        campusMaldonado.setName("Maldonado");
        campusMaldonado.setRegionalTechnologicalInstitute(rtiEste);
        campusMaldonado = campusRepository.save(campusMaldonado);
        log.info("✓ Created Campus: {} - {} (ID: {})", campusMaldonado.getName(), rtiEste.getName(), campusMaldonado.getId());

        // ========================================
        // CREACIÓN DE PROGRAMAS (CARRERAS)
        // ========================================
        log.info("Creating Programs...");

        // Tecnólogo en Informática - 252 créditos
        Program progTecInformatica = new Program("Tecnólogo en Informática", 6, 252);
        progTecInformatica = programRepository.save(progTecInformatica);
        log.info("✓ Created Program: {} ({} créditos)", progTecInformatica.getName(), progTecInformatica.getTotalCredits());

        // Asociar programa al campus San José
        campusSanJose.getPrograms().add(progTecInformatica);
        campusSanJose = campusRepository.save(campusSanJose);
        log.info("✓ Associated {} to Campus {}", progTecInformatica.getName(), campusSanJose.getName());

        // Asociar programa también al campus Maldonado
        campusMaldonado.getPrograms().add(progTecInformatica);
        campusMaldonado = campusRepository.save(campusMaldonado);
        log.info("✓ Associated {} to Campus {}", progTecInformatica.getName(), campusMaldonado.getName());

        // ========================================
        // CREACIÓN DE TÉRMINOS Y UNIDADES CURRICULARES
        // ========================================
        log.info("Creating Terms and Curricular Units for {}...", progTecInformatica.getName());

        // Término 1
        Term term1 = new Term(1, progTecInformatica);
        term1 = termRepository.save(term1);

        // Unidades Curriculares - Término 1
        CurricularUnit ucPrincipiosProg = new CurricularUnit("Principios de Programación", 16, term1);
        ucPrincipiosProg = curricularUnitRepository.save(ucPrincipiosProg);
        log.info("  ✓ Created UC: {} ({} créditos)", ucPrincipiosProg.getName(), ucPrincipiosProg.getCredits());

        CurricularUnit ucArqComputadoras = new CurricularUnit("Arquitectura de Computadoras", 12, term1);
        ucArqComputadoras = curricularUnitRepository.save(ucArqComputadoras);
        log.info("  ✓ Created UC: {} ({} créditos)", ucArqComputadoras.getName(), ucArqComputadoras.getCredits());

        CurricularUnit ucInglesTec1 = new CurricularUnit("Inglés Técnico 1", 8, term1);
        ucInglesTec1 = curricularUnitRepository.save(ucInglesTec1);
        log.info("  ✓ Created UC: {} ({} créditos)", ucInglesTec1.getName(), ucInglesTec1.getCredits());

        CurricularUnit ucMatDiscreta1 = new CurricularUnit("Matemática Discreta y Lógica 1", 12, term1);
        ucMatDiscreta1 = curricularUnitRepository.save(ucMatDiscreta1);
        log.info("  ✓ Created UC: {} ({} créditos)", ucMatDiscreta1.getName(), ucMatDiscreta1.getCredits());

        // Término 2
        Term term2 = new Term(2, progTecInformatica);
        term2 = termRepository.save(term2);

        CurricularUnit ucMatDiscreta2 = new CurricularUnit("Matemática Discreta y Lógica 2", 6, term2);
        ucMatDiscreta2 = curricularUnitRepository.save(ucMatDiscreta2);
        log.info("  ✓ Created UC: {} ({} créditos)", ucMatDiscreta2.getName(), ucMatDiscreta2.getCredits());

        CurricularUnit ucBD1 = new CurricularUnit("Bases de Datos 1", 12, term2);
        ucBD1 = curricularUnitRepository.save(ucBD1);
        log.info("  ✓ Created UC: {} ({} créditos)", ucBD1.getName(), ucBD1.getCredits());

        CurricularUnit ucEstructuraAlg = new CurricularUnit("Estructura y Diseño de Algoritmos", 12, term2);
        ucEstructuraAlg = curricularUnitRepository.save(ucEstructuraAlg);
        log.info("  ✓ Created UC: {} ({} créditos)", ucEstructuraAlg.getName(), ucEstructuraAlg.getCredits());

        CurricularUnit ucInglesTec2 = new CurricularUnit("Inglés Técnico 2", 4, term2);
        ucInglesTec2 = curricularUnitRepository.save(ucInglesTec2);
        log.info("  ✓ Created UC: {} ({} créditos)", ucInglesTec2.getName(), ucInglesTec2.getCredits());

        CurricularUnit ucSistemasOp = new CurricularUnit("Sistemas Operativos", 12, term2);
        ucSistemasOp = curricularUnitRepository.save(ucSistemasOp);
        log.info("  ✓ Created UC: {} ({} créditos)", ucSistemasOp.getName(), ucSistemasOp.getCredits());

        log.info("✓ Created {} curricular units for {}", 9, progTecInformatica.getName());

        // ========================================
        // CREACIÓN DE USUARIOS
        // ========================================
        log.info("Creating users...");

        PersonalData personalDataJuan = new PersonalData();
        personalDataJuan.setName("Juan");
        personalDataJuan.setLastName("Pérez");
        personalDataJuan.setIdentityDocument("12345678");
        personalDataJuan.setPhoneNumber("099123456");
        personalDataJuan.setCountry("Uruguay");
        personalDataJuan.setCity("San José");

        User userJuan = new User(
            "juan.perez@utec.edu.uy",
            passwordEncoder.encode("password123"),
            personalDataJuan
        );
        userJuan = userRepository.save(userJuan);
        log.info("✓ Created user: {} (ID: {})", userJuan.getUtecEmail(), userJuan.getId());

        // Crear posición de docente en San José
        Teacher teacherJuan = new Teacher(userJuan);
        teacherJuan.addCampus(campusSanJose);
        teacherJuan.addCampus(campusMaldonado);
        userJuan.addPosition(teacherJuan);

        userJuan = userRepository.save(userJuan);
        log.info("✓ User Juan Pérez assigned as Teacher at Campuses: {}, {}", campusSanJose.getName(), campusMaldonado.getName());

        // Usuario con múltiples roles
        PersonalData personalDataMaria = new PersonalData();
        personalDataMaria.setName("María");
        personalDataMaria.setLastName("González");
        personalDataMaria.setIdentityDocument("87654321");
        personalDataMaria.setPhoneNumber("099654321");
        personalDataMaria.setCountry("Uruguay");
        personalDataMaria.setCity("San José");

        User userMaria = new User(
            "maria.gonzalez@utec.edu.uy",
            passwordEncoder.encode("password123"),
            personalDataMaria
        );
        userMaria = userRepository.save(userMaria);
        log.info("✓ Created user: {} (ID: {})", userMaria.getUtecEmail(), userMaria.getId());

        // Crear posición de Analista
        Analyst analystMaria = new Analyst(userMaria);
        analystMaria.addCampus(campusSanJose);
        userMaria.addPosition(analystMaria);

        // Crear posición de Referente de Educación
        EducationManager educationManagerMaria = new EducationManager(userMaria);
        educationManagerMaria.addCampus(campusSanJose);
        userMaria.addPosition(educationManagerMaria);

        // Crear posición de Coordinador
        Coordinator coordinatorMaria = new Coordinator(userMaria);
        coordinatorMaria.addCampus(campusSanJose);
        userMaria.addPosition(coordinatorMaria);

        userMaria = userRepository.save(userMaria);
        log.info("✓ User María González assigned as Analyst, Education Manager and Coordinator at Campus: {}", campusSanJose.getName());

        // ========================================
        // CREACIÓN DE CURSO
        // ========================================
        log.info("Creating course...");

        Course coursePrincipiosProg = new Course(
            Shift.MORNING,
            "Principios de Programación - Grupo 1",
            LocalDate.of(2024, 3, 1),
            LocalDate.of(2024, 6, 20),
            PartialGradingSystem.PGS_1,
            ucPrincipiosProg
        );

        // Obtener el teacher guardado desde las posiciones del userJuan
        final Campus finalCampusSanJose = campusSanJose;
        Teacher savedTeacherJuan = (Teacher) userJuan.getPositions().stream()
            .filter(p -> p instanceof Teacher && p.getCampuses().contains(finalCampusSanJose))
            .findFirst()
            .orElseThrow();

        coursePrincipiosProg.getTeachers().add(savedTeacherJuan);
        coursePrincipiosProg.getHoursPerDeliveryFormat().put(DeliveryFormat.IN_PERSON, 80);
        coursePrincipiosProg.getHoursPerDeliveryFormat().put(DeliveryFormat.VIRTUAL, 20);
        coursePrincipiosProg.getSustainableDevelopmentGoals().add(SustainableDevelopmentGoal.SDG_4);
        coursePrincipiosProg.getSustainableDevelopmentGoals().add(SustainableDevelopmentGoal.SDG_9);
        coursePrincipiosProg.getUniversalDesignLearningPrinciples().add(UniversalDesignLearningPrinciple.MEANS_OF_REPRESENTATION);
        coursePrincipiosProg.getUniversalDesignLearningPrinciples().add(UniversalDesignLearningPrinciple.MEANS_OF_ACTION_EXPRESSION);
        coursePrincipiosProg.setIsRelatedToInvestigation(false);
        coursePrincipiosProg.setInvolvesActivitiesWithProductiveSector(true);

        coursePrincipiosProg = courseRepository.save(coursePrincipiosProg);
        log.info("✓ Created course: {} (ID: {})", coursePrincipiosProg.getDescription(), coursePrincipiosProg.getId());
        log.info("  - Teacher: {}", userJuan.getUtecEmail());
        log.info("  - Period: {} to {}", coursePrincipiosProg.getStartDate(), coursePrincipiosProg.getEndDate());

        createWeeklyPlanningsForPrincipiosProgramacion(coursePrincipiosProg);

        // ========================================
        // RESUMEN FINAL
        // ========================================
        log.info("");
        log.info("==================================================");
        log.info("📊 DATA SEEDING COMPLETED - SUMMARY");
        log.info("==================================================");
        log.info("");
        log.info("🏛️  REGIONAL TECHNOLOGICAL INSTITUTES (4):");
        log.info("   • ITR Suroeste - Campuses: Fray Bentos, Mercedes, Paysandú, Colonia La Paz, Nueva Helvecia");
        log.info("   • ITR Centro-Sur - Campuses: Durazno, San José");
        log.info("   • ITR Norte - Campuses: Rivera, Melo");
        log.info("   • ITR Este - Campuses: Minas, Maldonado");
        log.info("");
        log.info("🎓 PROGRAMS:");
        log.info("   • Tecnólogo en Informática (252 créditos)");
        log.info("     - Campuses: San José, Maldonado");
        log.info("     - 9 Unidades Curriculares distribuidas en 2 términos");
        log.info("");
        log.info("👥 USERS:");
        log.info("   • Juan Pérez (juan.perez@utec.edu.uy)");
        log.info("     - Password: password123");
        log.info("     - Role: Teacher at Campuses San José and Maldonado");
        log.info("   • María González (maria.gonzalez@utec.edu.uy)");
        log.info("     - Password: password123");
        log.info("     - Roles: Analyst, Education Manager, Coordinator at Campus San José");
        log.info("");
        log.info("==================================================");

    }

    private void createWeeklyPlanningsForPrincipiosProgramacion(Course course) {
        log.info("Creating Weekly Plannings for course: {}", course.getDescription());

        // =============== SEMANA 1: 4-10 Marzo ===============
        WeeklyPlanning week1 = new WeeklyPlanning(
            1,
            LocalDate.of(2024, 3, 4),
            LocalDate.of(2024, 3, 10)
        );

        // Contenido 1 - Semana 1
        ProgrammaticContent content1Week1 = new ProgrammaticContent(
            "Introducción a Estructuras de Datos",
            "Conceptos fundamentales de organización de datos, tipos de datos primitivos y complejos",
            week1
        );
        content1Week1.setColor("#F8BBD0");

        Activity act1C1W1 = new Activity(
            "Clase magistral: Introducción al curso y conceptos básicos de estructuras de datos",
            120,
            LearningModality.IN_PERSON,
            content1Week1
        );
        act1C1W1.setTitle("Clase Introductoria");
        act1C1W1.setColor("#E53935");
        act1C1W1.getCognitiveProcesses().addAll(Arrays.asList(CognitiveProcess.REMEMBER, CognitiveProcess.UNDERSTAND));
        act1C1W1.getTeachingStrategies().add(TeachingStrategy.LECTURE);
        act1C1W1.getLearningResources().add(LearningResource.DEMONSTRATION);

        Activity act2C1W1 = new Activity(
            "Discusión: Análisis de problemas cotidianos que requieren organización de datos",
            90,
            LearningModality.IN_PERSON,
            content1Week1
        );
        act2C1W1.setTitle("Discusión Casos Reales");
        act2C1W1.setColor("#2979FF");
        act2C1W1.getCognitiveProcesses().add(CognitiveProcess.ANALYZE);
        act2C1W1.getTeachingStrategies().add(TeachingStrategy.CASE_STUDY);
        act2C1W1.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);

        Activity act3C1W1 = new Activity(
            "Ejercicios prácticos: Identificación de tipos de datos en diferentes contextos",
            90,
            LearningModality.IN_PERSON,
            content1Week1
        );
        act3C1W1.setTitle("Práctica Tipos de Datos");
        act3C1W1.setColor("#76FF03");
        act3C1W1.getCognitiveProcesses().add(CognitiveProcess.APPLY);
        act3C1W1.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);

        content1Week1.getActivities().add(act1C1W1);
        content1Week1.getActivities().add(act2C1W1);
        content1Week1.getActivities().add(act3C1W1);

        // Contenido 2 - Semana 1
        ProgrammaticContent content2Week1 = new ProgrammaticContent(
            "Arrays y Memoria",
            "Estructuras de datos básicas: arrays, acceso a elementos y gestión de memoria",
            week1
        );
        content2Week1.setColor("#FFF9C4");

        Activity act1C2W1 = new Activity(
            "Presentación: Conceptos de arrays y acceso directo a memoria",
            90,
            LearningModality.IN_PERSON,
            content2Week1
        );
        act1C2W1.setTitle("Arrays y Acceso");
        act1C2W1.setColor("#D81B60");
        act1C2W1.getCognitiveProcesses().add(CognitiveProcess.UNDERSTAND);
        act1C2W1.getTeachingStrategies().add(TeachingStrategy.LECTURE);

        Activity act2C2W1 = new Activity(
            "Laboratorio: Implementación de arrays y operaciones básicas en C/Java",
            120,
            LearningModality.IN_PERSON,
            content2Week1
        );
        act2C2W1.setTitle("Lab Arrays");
        act2C2W1.setColor("#FF9100");
        act2C2W1.getCognitiveProcesses().add(CognitiveProcess.APPLY);
        act2C2W1.getTeachingStrategies().add(TeachingStrategy.LABORATORY_PRACTICES);
        act2C2W1.getTransversalCompetencies().add(TransversalCompetency.LEARNING_SELF_REGULATION);

        Activity act3C2W1 = new Activity(
            "Ejercicios autónomos: Problemas de manipulación de arrays",
            180,
            LearningModality.AUTONOMOUS,
            content2Week1
        );
        act3C2W1.setTitle("Ejercicios Arrays");
        act3C2W1.setColor("#FFEB3B");
        act3C2W1.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        act3C2W1.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);

        content2Week1.getActivities().add(act1C2W1);
        content2Week1.getActivities().add(act2C2W1);
        content2Week1.getActivities().add(act3C2W1);

        week1.getProgrammaticContents().add(content1Week1);
        week1.getProgrammaticContents().add(content2Week1);
        week1.getBibliographicReferences().add("Cormen, T. et al. (2009). Introduction to Algorithms. MIT Press. Capítulos 1-2.");
        week1.getBibliographicReferences().add("Knuth, D. (1997). The Art of Computer Programming, Vol. 1. Addison-Wesley.");
        course.getWeeklyPlannings().add(week1);

        // =============== SEMANA 2: 11-17 Marzo ===============
        WeeklyPlanning week2 = new WeeklyPlanning(
            2,
            LocalDate.of(2024, 3, 11),
            LocalDate.of(2024, 3, 17)
        );

        // Contenido 1 - Semana 2
        ProgrammaticContent content1Week2 = new ProgrammaticContent(
            "Punteros y Referencias",
            "Manejo de punteros, referencias y gestión dinámica de memoria",
            week2
        );
        content1Week2.setColor("#C8E6C9");

        Activity act1C1W2 = new Activity(
            "Clase teórica: Conceptos de punteros y direcciones de memoria",
            120,
            LearningModality.IN_PERSON,
            content1Week2
        );
        act1C1W2.setTitle("Teoría Punteros");
        act1C1W2.setColor("#00B8D4");
        act1C1W2.getCognitiveProcesses().addAll(Arrays.asList(CognitiveProcess.REMEMBER, CognitiveProcess.UNDERSTAND));
        act1C1W2.getTeachingStrategies().add(TeachingStrategy.LECTURE);
        act1C1W2.getLearningResources().add(LearningResource.DEMONSTRATION);

        Activity act2C1W2 = new Activity(
            "Demostración práctica: Uso de punteros y operador de dirección",
            90,
            LearningModality.IN_PERSON,
            content1Week2
        );
        act2C1W2.setTitle("Demo Punteros");
        act2C1W2.setColor("#8E24AA");
        act2C1W2.getCognitiveProcesses().add(CognitiveProcess.APPLY);

        Activity act3C1W2 = new Activity(
            "Ejercicios guiados: Manipulación de datos mediante punteros",
            120,
            LearningModality.IN_PERSON,
            content1Week2
        );
        act3C1W2.setTitle("Ejercicios Punteros");
        act3C1W2.setColor("#E53935");
        act3C1W2.getCognitiveProcesses().add(CognitiveProcess.APPLY);
        act3C1W2.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);
        act3C1W2.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);

        content1Week2.getActivities().add(act1C1W2);
        content1Week2.getActivities().add(act2C1W2);
        content1Week2.getActivities().add(act3C1W2);

        // Contenido 2 - Semana 2
        ProgrammaticContent content2Week2 = new ProgrammaticContent(
            "Listas Enlazadas Simples",
            "Implementación y operaciones básicas en listas enlazadas",
            week2
        );
        content2Week2.setColor("#B3E5FC");

        Activity act1C2W2 = new Activity(
            "Clase magistral: Introducción a listas enlazadas y nodos",
            90,
            LearningModality.IN_PERSON,
            content2Week2
        );
        act1C2W2.setTitle("Intro Listas Enlazadas");
        act1C2W2.setColor("#2979FF");
        act1C2W2.getCognitiveProcesses().add(CognitiveProcess.UNDERSTAND);
        act1C2W2.getTeachingStrategies().add(TeachingStrategy.LECTURE);

        Activity act2C2W2 = new Activity(
            "Laboratorio: Implementación de lista enlazada simple",
            150,
            LearningModality.IN_PERSON,
            content2Week2
        );
        act2C2W2.setTitle("Lab Listas");
        act2C2W2.setColor("#76FF03");
        act2C2W2.getCognitiveProcesses().addAll(Arrays.asList(CognitiveProcess.APPLY, CognitiveProcess.CREATE));
        act2C2W2.getTeachingStrategies().add(TeachingStrategy.LABORATORY_PRACTICES);
        act2C2W2.getTransversalCompetencies().add(TransversalCompetency.LEARNING_SELF_REGULATION);

        Activity act3C2W2 = new Activity(
            "Tarea: Operaciones de inserción, eliminación y búsqueda en listas",
            200,
            LearningModality.AUTONOMOUS,
            content2Week2
        );
        act3C2W2.setTitle("Tarea Listas");
        act3C2W2.setColor("#D81B60");
        act3C2W2.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        act3C2W2.getTeachingStrategies().add(TeachingStrategy.PROJECTS);
        act3C2W2.getLearningResources().add(LearningResource.ONLINE_EVALUATION);

        content2Week2.getActivities().add(act1C2W2);
        content2Week2.getActivities().add(act2C2W2);
        content2Week2.getActivities().add(act3C2W2);

        // Contenido 3 - Semana 2
        ProgrammaticContent content3Week2 = new ProgrammaticContent(
            "Complejidad Algorítmica Básica",
            "Introducción al análisis de complejidad temporal y espacial",
            week2
        );
        content3Week2.setColor("#E1BEE7");

        Activity act1C3W2 = new Activity(
            "Presentación: Notación Big-O y análisis de algoritmos",
            90,
            LearningModality.IN_PERSON,
            content3Week2
        );
        act1C3W2.setTitle("Big-O Notation");
        act1C3W2.setColor("#FF9100");
        act1C3W2.getCognitiveProcesses().add(CognitiveProcess.UNDERSTAND);
        act1C3W2.getTeachingStrategies().add(TeachingStrategy.LECTURE);

        Activity act2C3W2 = new Activity(
            "Análisis: Comparación de complejidad de diferentes operaciones",
            60,
            LearningModality.IN_PERSON,
            content3Week2
        );
        act2C3W2.setTitle("Análisis Complejidad");
        act2C3W2.setColor("#FFEB3B");
        act2C3W2.getCognitiveProcesses().add(CognitiveProcess.ANALYZE);
        act2C3W2.getTeachingStrategies().add(TeachingStrategy.CASE_STUDY);
        act2C3W2.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);

        Activity act3C3W2 = new Activity(
            "Ejercicios: Cálculo de complejidad de algoritmos simples",
            90,
            LearningModality.AUTONOMOUS,
            content3Week2
        );
        act3C3W2.setTitle("Ejercicios Complejidad");
        act3C3W2.setColor("#00B8D4");
        act3C3W2.getCognitiveProcesses().add(CognitiveProcess.APPLY);
        act3C3W2.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);

        content3Week2.getActivities().add(act1C3W2);
        content3Week2.getActivities().add(act2C3W2);
        content3Week2.getActivities().add(act3C3W2);

        week2.getProgrammaticContents().add(content1Week2);
        week2.getProgrammaticContents().add(content2Week2);
        week2.getProgrammaticContents().add(content3Week2);
        week2.getBibliographicReferences().add("Kernighan, B. & Ritchie, D. (1988). The C Programming Language. Prentice Hall. Capítulo 5.");
        week2.getBibliographicReferences().add("Skiena, S. (2008). The Algorithm Design Manual. Springer. Capítulo 3.");
        course.getWeeklyPlannings().add(week2);

        // =============== SEMANA 3: 18-24 Marzo ===============
        WeeklyPlanning week3 = new WeeklyPlanning(
            3,
            LocalDate.of(2024, 3, 18),
            LocalDate.of(2024, 3, 24)
        );

        // Contenido 1 - Semana 3
        ProgrammaticContent content1Week3 = new ProgrammaticContent(
            "Pilas (Stacks)",
            "Implementación y aplicaciones de la estructura de datos tipo pila (LIFO)",
            week3
        );
        content1Week3.setColor("#FFDAB9");

        Activity act1C1W3 = new Activity(
            "Clase teórica: Concepto de pila y operaciones básicas (push, pop, peek)",
            90,
            LearningModality.IN_PERSON,
            content1Week3
        );
        act1C1W3.setTitle("Teoría Pilas");
        act1C1W3.setColor("#8E24AA");
        act1C1W3.getCognitiveProcesses().add(CognitiveProcess.UNDERSTAND);
        act1C1W3.getTeachingStrategies().add(TeachingStrategy.LECTURE);
        act1C1W3.getLearningResources().add(LearningResource.DEMONSTRATION);

        Activity act2C1W3 = new Activity(
            "Laboratorio: Implementación de pila usando arrays y listas enlazadas",
            120,
            LearningModality.IN_PERSON,
            content1Week3
        );
        act2C1W3.setTitle("Lab Pilas");
        act2C1W3.setColor("#E53935");
        act2C1W3.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        act2C1W3.getTeachingStrategies().add(TeachingStrategy.LABORATORY_PRACTICES);

        Activity act3C1W3 = new Activity(
            "Aplicación práctica: Evaluación de expresiones con pilas",
            150,
            LearningModality.IN_PERSON,
            content1Week3
        );
        act3C1W3.setTitle("Aplicación Pilas");
        act3C1W3.setColor("#2979FF");
        act3C1W3.getCognitiveProcesses().addAll(Arrays.asList(CognitiveProcess.APPLY, CognitiveProcess.ANALYZE));
        act3C1W3.getTeachingStrategies().add(TeachingStrategy.CASE_STUDY);
        act3C1W3.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);

        content1Week3.getActivities().add(act1C1W3);
        content1Week3.getActivities().add(act2C1W3);
        content1Week3.getActivities().add(act3C1W3);

        // Contenido 2 - Semana 3
        ProgrammaticContent content2Week3 = new ProgrammaticContent(
            "Colas (Queues)",
            "Implementación y aplicaciones de la estructura de datos tipo cola (FIFO)",
            week3
        );
        content2Week3.setColor("#B2EBF2");

        Activity act1C2W3 = new Activity(
            "Clase magistral: Concepto de cola y operaciones (enqueue, dequeue)",
            90,
            LearningModality.IN_PERSON,
            content2Week3
        );
        act1C2W3.setTitle("Teoría Colas");
        act1C2W3.setColor("#76FF03");
        act1C2W3.getCognitiveProcesses().add(CognitiveProcess.UNDERSTAND);
        act1C2W3.getTeachingStrategies().add(TeachingStrategy.LECTURE);

        Activity act2C2W3 = new Activity(
            "Implementación: Colas circulares y colas con prioridad",
            120,
            LearningModality.IN_PERSON,
            content2Week3
        );
        act2C2W3.setTitle("Lab Colas");
        act2C2W3.setColor("#D81B60");
        act2C2W3.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        act2C2W3.getTeachingStrategies().add(TeachingStrategy.LABORATORY_PRACTICES);
        act2C2W3.getTransversalCompetencies().add(TransversalCompetency.TEAMWORK);

        Activity act3C2W3 = new Activity(
            "Proyecto: Simulación de sistemas de atención usando colas",
            180,
            LearningModality.AUTONOMOUS,
            content2Week3
        );
        act3C2W3.setTitle("Proyecto Colas");
        act3C2W3.setColor("#FF9100");
        act3C2W3.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        act3C2W3.getTeachingStrategies().add(TeachingStrategy.PROJECTS);
        act3C2W3.getLearningResources().add(LearningResource.ONLINE_EVALUATION);

        content2Week3.getActivities().add(act1C2W3);
        content2Week3.getActivities().add(act2C2W3);
        content2Week3.getActivities().add(act3C2W3);

        week3.getProgrammaticContents().add(content1Week3);
        week3.getProgrammaticContents().add(content2Week3);
        week3.getBibliographicReferences().add("Weiss, M. A. (2013). Data Structures and Algorithm Analysis in Java. Pearson. Capítulo 3.");
        course.getWeeklyPlannings().add(week3);

        // =============== SEMANA 4: 25-31 Marzo ===============
        WeeklyPlanning week4 = new WeeklyPlanning(
            4,
            LocalDate.of(2024, 3, 25),
            LocalDate.of(2024, 3, 31)
        );

        // Contenido 1 - Semana 4
        ProgrammaticContent content1Week4 = new ProgrammaticContent(
            "Recursión",
            "Técnicas de programación recursiva y análisis de casos base",
            week4
        );
        content1Week4.setColor("#D1C4E9");

        Activity act1C1W4 = new Activity(
            "Clase teórica: Fundamentos de recursión y casos base",
            90,
            LearningModality.IN_PERSON,
            content1Week4
        );
        act1C1W4.setTitle("Teoría Recursión");
        act1C1W4.setColor("#FFEB3B");
        act1C1W4.getCognitiveProcesses().add(CognitiveProcess.UNDERSTAND);
        act1C1W4.getTeachingStrategies().add(TeachingStrategy.LECTURE);
        act1C1W4.getLearningResources().add(LearningResource.DEMONSTRATION);

        Activity act2C1W4 = new Activity(
            "Ejercicios prácticos: Problemas clásicos de recursión (factorial, fibonacci)",
            120,
            LearningModality.IN_PERSON,
            content1Week4
        );
        act2C1W4.setTitle("Práctica Recursión");
        act2C1W4.setColor("#00B8D4");
        act2C1W4.getCognitiveProcesses().add(CognitiveProcess.APPLY);
        act2C1W4.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);

        Activity act3C1W4 = new Activity(
            "Análisis: Recursión vs iteración, ventajas y desventajas",
            90,
            LearningModality.IN_PERSON,
            content1Week4
        );
        act3C1W4.setTitle("Análisis Recursión");
        act3C1W4.setColor("#8E24AA");
        act3C1W4.getCognitiveProcesses().add(CognitiveProcess.EVALUATE);
        act3C1W4.getTeachingStrategies().add(TeachingStrategy.CASE_STUDY);
        act3C1W4.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);

        content1Week4.getActivities().add(act1C1W4);
        content1Week4.getActivities().add(act2C1W4);
        content1Week4.getActivities().add(act3C1W4);

        // Contenido 2 - Semana 4
        ProgrammaticContent content2Week4 = new ProgrammaticContent(
            "Tipos Abstractos de Datos (TAD)",
            "Concepto de abstracción, encapsulamiento y definición de interfaces",
            week4
        );
        content2Week4.setColor("#F8BBD0");

        Activity act1C2W4 = new Activity(
            "Presentación: Concepto de TAD y separación interfaz/implementación",
            90,
            LearningModality.IN_PERSON,
            content2Week4
        );
        act1C2W4.setTitle("Teoría TAD");
        act1C2W4.setColor("#E53935");
        act1C2W4.getCognitiveProcesses().add(CognitiveProcess.UNDERSTAND);
        act1C2W4.getTeachingStrategies().add(TeachingStrategy.LECTURE);

        Activity act2C2W4 = new Activity(
            "Diseño: Creación de TADs para pilas, colas y listas",
            120,
            LearningModality.IN_PERSON,
            content2Week4
        );
        act2C2W4.setTitle("Diseño TAD");
        act2C2W4.setColor("#2979FF");
        act2C2W4.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        act2C2W4.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);
        act2C2W4.getTransversalCompetencies().add(TransversalCompetency.TEAMWORK);

        Activity act3C2W4 = new Activity(
            "Proyecto integrador: Implementación completa de un TAD personalizado",
            240,
            LearningModality.AUTONOMOUS,
            content2Week4
        );
        act3C2W4.setTitle("Proyecto TAD");
        act3C2W4.setColor("#76FF03");
        act3C2W4.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        act3C2W4.getTeachingStrategies().add(TeachingStrategy.PROJECTS);
        act3C2W4.getLearningResources().add(LearningResource.ONLINE_EVALUATION);

        content2Week4.getActivities().add(act1C2W4);
        content2Week4.getActivities().add(act2C2W4);
        content2Week4.getActivities().add(act3C2W4);

        week4.getProgrammaticContents().add(content1Week4);
        week4.getProgrammaticContents().add(content2Week4);
        week4.getBibliographicReferences().add("Sedgewick, R. & Wayne, K. (2011). Algorithms. Addison-Wesley. Capítulos 1.1-1.2.");
        week4.getBibliographicReferences().add("Aho, A. et al. (2006). Compilers: Principles, Techniques, and Tools. Pearson. Capítulo 2.");
        course.getWeeklyPlannings().add(week4);

        // =============== SEMANAS RESTANTES VACÍAS ===============
        LocalDate startDate = course.getStartDate();
        LocalDate endDate = course.getEndDate();
        long totalWeeks = java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate) + 1;

        for (int weekNum = 5; weekNum <= totalWeeks; weekNum++) {
            LocalDate weekStart = startDate.plusWeeks(weekNum - 1);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }
            
            WeeklyPlanning emptyWeek = new WeeklyPlanning(
                weekNum,
                weekStart,
                weekEnd
            );
            course.getWeeklyPlannings().add(emptyWeek);
        }

        courseRepository.save(course);
        log.info("✓ Created {} weekly plannings (4 with content for March, {} empty)", totalWeeks, totalWeeks - 4);
    }
}
