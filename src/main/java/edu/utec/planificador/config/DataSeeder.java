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
        log.info("Creating Programs (Carreras)...");

        // Programas ITR Suroeste
        Program progIngMecatronica = new Program("Ingeniería en Mecatrónica", 8, 240);
        progIngMecatronica = programRepository.save(progIngMecatronica);
        campusFrayBentos.getPrograms().add(progIngMecatronica);

        Program progIngLogistica = new Program("Ingeniería en Logística", 8, 240);
        progIngLogistica = programRepository.save(progIngLogistica);
        campusFrayBentos.getPrograms().add(progIngLogistica);

        Program progTecBiomedica = new Program("Tecnólogo en Ingeniería Biomédica", 6, 180);
        progTecBiomedica = programRepository.save(progTecBiomedica);
        campusFrayBentos.getPrograms().add(progTecBiomedica);

        Program progLicAnalisisAlimentario = new Program("Licenciatura en Análisis Alimentario", 7, 210);
        progLicAnalisisAlimentario = programRepository.save(progLicAnalisisAlimentario);
        campusFrayBentos.getPrograms().add(progLicAnalisisAlimentario);

        Program progLicTI = new Program("Licenciatura en Tecnologías de la Información", 7, 210);
        progLicTI = programRepository.save(progLicTI);
        campusFrayBentos.getPrograms().add(progLicTI);

        Program progLicJazz = new Program("Licenciatura en Jazz y Música Creativa", 7, 210);
        progLicJazz = programRepository.save(progLicJazz);
        campusFrayBentos.getPrograms().add(progLicJazz);

        Program progLicLacteos = new Program("Licenciatura en Ciencia y Tecnología de Lácteos", 7, 210);
        progLicLacteos = programRepository.save(progLicLacteos);
        campusFrayBentos.getPrograms().add(progLicLacteos);

        Program progTecLechera = new Program("Tecnólogo en Manejo de Sistemas de Producción Lechera", 6, 180);
        progTecLechera = programRepository.save(progTecLechera);
        campusFrayBentos.getPrograms().add(progTecLechera);

        campusFrayBentos = campusRepository.save(campusFrayBentos);
        log.info("✓ Created and associated {} programs to {}", 8, campusFrayBentos.getName());

        // Programas ITR Centro-Sur
        Program progTecInformatica = new Program("Tecnólogo en Informática", 6, 180);
        progTecInformatica = programRepository.save(progTecInformatica);
        campusDurazno.getPrograms().add(progTecInformatica);
        campusSanJose.getPrograms().add(progTecInformatica);

        campusDurazno.getPrograms().add(progLicTI); // LTI también en Centro-Sur
        campusSanJose.getPrograms().add(progLicTI);

        Program progIngAgua = new Program("Ingeniería en Agua y Desarrollo Sostenible", 8, 240);
        progIngAgua = programRepository.save(progIngAgua);
        campusDurazno.getPrograms().add(progIngAgua);

        Program progIngAgroambiental = new Program("Ingeniería Agroambiental", 8, 240);
        progIngAgroambiental = programRepository.save(progIngAgroambiental);
        campusDurazno.getPrograms().add(progIngAgroambiental);

        Program progIngEnergias = new Program("Ingeniería en Energías Renovables", 8, 240);
        progIngEnergias = programRepository.save(progIngEnergias);
        campusDurazno.getPrograms().add(progIngEnergias);

        campusDurazno = campusRepository.save(campusDurazno);
        campusSanJose = campusRepository.save(campusSanJose);
        log.info("✓ Created and associated programs to ITR Centro-Sur campuses");

        // Programas ITR Norte
        Program progLicDatosIA = new Program("Licenciatura en Ingeniería de Datos e Inteligencia Artificial", 7, 210);
        progLicDatosIA = programRepository.save(progLicDatosIA);
        campusRivera.getPrograms().add(progLicDatosIA);

        campusRivera.getPrograms().add(progIngLogistica); // Logística también en Norte

        Program progTecMecatronicaInd = new Program("Tecnólogo en Mecatrónica Industrial", 6, 180);
        progTecMecatronicaInd = programRepository.save(progTecMecatronicaInd);
        campusRivera.getPrograms().add(progTecMecatronicaInd);

        Program progTecDatos = new Program("Tecnólogo en Análisis y Gestión de Datos", 6, 180);
        progTecDatos = programRepository.save(progTecDatos);
        campusRivera.getPrograms().add(progTecDatos);

        Program progTecLogistica = new Program("Tecnólogo en Logística", 6, 180);
        progTecLogistica = programRepository.save(progTecLogistica);
        campusRivera.getPrograms().add(progTecLogistica);

        campusRivera = campusRepository.save(campusRivera);
        campusMelo.getPrograms().add(progLicDatosIA);
        campusMelo = campusRepository.save(campusMelo);
        log.info("✓ Created and associated programs to ITR Norte campuses");

        // Programas ITR Este
        campusMinas.getPrograms().add(progIngAgua); // Agua también en Minas
        campusMinas.getPrograms().add(progLicTI); // LTI también en Minas
        campusMinas = campusRepository.save(campusMinas);

        campusMaldonado.getPrograms().add(progLicTI);
        campusMaldonado = campusRepository.save(campusMaldonado);
        log.info("✓ Associated programs to ITR Este campuses");

        // ========================================
        // CREACIÓN DE USUARIOS
        // ========================================
        log.info("Creating users with positions...");

        // USUARIO 1: Docente y roles en ITR Suroeste (Fray Bentos)
        PersonalData personalData1 = new PersonalData();
        personalData1.setName("Juan");
        personalData1.setLastName("Pérez");
        personalData1.setIdentityDocument("12345678");
        personalData1.setPhoneNumber("099123456");
        personalData1.setCountry("Uruguay");
        personalData1.setCity("Fray Bentos");

        User user1 = new User(
            "juan.perez@utec.edu.uy",
            passwordEncoder.encode("password"),
            personalData1
        );
        user1 = userRepository.save(user1);
        log.info("✓ Created user: {} (ID: {})", user1.getUtecEmail(), user1.getId());

        Teacher teacherFrayBentos = new Teacher(user1);
        teacherFrayBentos.addCampus(campusFrayBentos);
        teacherFrayBentos.addCampus(campusMercedes);
        user1.addPosition(teacherFrayBentos);

        Coordinator coordinatorFrayBentos = new Coordinator(user1);
        coordinatorFrayBentos.addCampus(campusFrayBentos);
        user1.addPosition(coordinatorFrayBentos);

        Administrator administratorFrayBentos = new Administrator(user1);
        administratorFrayBentos.addCampus(campusFrayBentos);
        user1.addPosition(administratorFrayBentos);

        EducationManager educationManagerFrayBentos = new EducationManager(user1);
        educationManagerFrayBentos.addCampus(campusFrayBentos);
        user1.addPosition(educationManagerFrayBentos);

        Analyst analystFrayBentos = new Analyst(user1);
        analystFrayBentos.addCampus(campusFrayBentos);
        user1.addPosition(analystFrayBentos);

        user1 = userRepository.save(user1);
        log.info("✓ User 1 has access to ITR Suroeste (Fray Bentos, Mercedes)");

        // ========================================
        // CREACIÓN DE TÉRMINOS Y UNIDADES CURRICULARES
        // ========================================
        log.info("Creating Terms and Curricular Units...");

        // Licenciatura en Tecnologías de la Información - Semestre 1
        Term termLTI1 = new Term(1, progLicTI);
        termLTI1 = termRepository.save(termLTI1);

        CurricularUnit ucIntroTI = new CurricularUnit("Introducción a TI", 6, termLTI1);
        ucIntroTI = curricularUnitRepository.save(ucIntroTI);

        CurricularUnit ucProgramacion1 = new CurricularUnit("Programación I", 8, termLTI1);
        ucProgramacion1.getDomainAreas().add(DomainArea.INSTALLATION_DESIGN);
        ucProgramacion1.getDomainAreas().add(DomainArea.RDI_PROJECTS);
        ucProgramacion1.getProfessionalCompetencies().add(ProfessionalCompetency.TECHNICAL_ASSISTANCE);
        ucProgramacion1.getProfessionalCompetencies().add(ProfessionalCompetency.PROJECT_DESIGN_MANAGEMENT);
        ucProgramacion1 = curricularUnitRepository.save(ucProgramacion1);

        CurricularUnit ucMatDiscreta = new CurricularUnit("Matemática Discreta", 6, termLTI1);
        ucMatDiscreta.getDomainAreas().add(DomainArea.INSTALLATION_DESIGN);
        ucMatDiscreta.getDomainAreas().add(DomainArea.INSTALLATION_MANAGEMENT);
        ucMatDiscreta.getProfessionalCompetencies().add(ProfessionalCompetency.TECHNICAL_ASSISTANCE);
        ucMatDiscreta.getProfessionalCompetencies().add(ProfessionalCompetency.EFFICIENT_MANAGEMENT);
        ucMatDiscreta = curricularUnitRepository.save(ucMatDiscreta);

        CurricularUnit ucFundamentosBD = new CurricularUnit("Fundamentos de Bases de Datos", 6, termLTI1);
        ucFundamentosBD = curricularUnitRepository.save(ucFundamentosBD);

        log.info("✓ Created {} curricular units for {} - Semestre 1", 4, progLicTI.getName());

        // Ingeniería en Logística - Semestre 1
        Term termLog1 = new Term(1, progIngLogistica);
        termLog1 = termRepository.save(termLog1);

        CurricularUnit ucHerramientasOp = new CurricularUnit("Herramientas Operativas I", 6, termLog1);
        ucHerramientasOp = curricularUnitRepository.save(ucHerramientasOp);

        CurricularUnit ucMatematica = new CurricularUnit("Matemática - Álgebra y Cálculo", 8, termLog1);
        ucMatematica = curricularUnitRepository.save(ucMatematica);

        CurricularUnit ucFundEconomia = new CurricularUnit("Fundamentos de Economía", 6, termLog1);
        ucFundEconomia = curricularUnitRepository.save(ucFundEconomia);

        CurricularUnit ucIntroLogistica = new CurricularUnit("Introducción a la Logística", 6, termLog1);
        ucIntroLogistica = curricularUnitRepository.save(ucIntroLogistica);

        log.info("✓ Created {} curricular units for {} - Semestre 1", 4, progIngLogistica.getName());

        // ========================================
        // CREACIÓN DE CURSO PARA USUARIO 1
        // ========================================
        log.info("Creating course...");
        Course course = new Course(
            Shift.MORNING,
            "Curso de Programación I - Grupo 1",
            LocalDate.of(2025, 2, 24),
            LocalDate.of(2025, 6, 27),
            PartialGradingSystem.PGS_1,
            ucProgramacion1
        );
        
        final Campus finalCampusFrayBentos = campusFrayBentos;
        teacherFrayBentos = (Teacher) user1.getPositions().stream()
            .filter(p -> p instanceof Teacher && p.getCampuses().contains(finalCampusFrayBentos))
            .findFirst()
            .orElseThrow();

        course.getTeachers().add(teacherFrayBentos);

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
        log.info("  - Teacher: {}", user1.getUtecEmail());

        createWeeklyPlanningsWithContent(course);
        
        // ========================================
        // SEGUNDO USUARIO Y CURSO PARA TESTING DE CONTROL DE ACCESO
        // ========================================
        log.info("Creating second user for access control testing...");
        PersonalData personalData2 = new PersonalData();
        personalData2.setName("María");
        personalData2.setLastName("González");
        personalData2.setIdentityDocument("87654321");
        personalData2.setPhoneNumber("099654321");
        personalData2.setCountry("Uruguay");
        personalData2.setCity("Rivera");

        User user2 = new User(
            "maria.gonzalez@utec.edu.uy",
            passwordEncoder.encode("password123"),
            personalData2
        );
        user2 = userRepository.save(user2);
        log.info("✓ Created second user: {} (ID: {})", user2.getUtecEmail(), user2.getId());

        // María solo tiene acceso al ITR Norte (Campus Rivera)
        Coordinator coordinatorRivera = new Coordinator(user2);
        coordinatorRivera.addCampus(campusRivera);
        user2.addPosition(coordinatorRivera);

        Teacher teacherRivera = new Teacher(user2);
        teacherRivera.addCampus(campusRivera);
        user2.addPosition(teacherRivera);

        user2 = userRepository.save(user2);
        log.info("✓ User 2 has positions ONLY at ITR Norte (Campus: Rivera)");

        // Crear término y UC para LIDIA en Rivera
        Term termLIDIA1 = new Term(1, progLicDatosIA);
        termLIDIA1 = termRepository.save(termLIDIA1);

        CurricularUnit ucIntroDatosIA = new CurricularUnit("Introducción a la Ingeniería de Datos e IA", 6, termLIDIA1);
        ucIntroDatosIA = curricularUnitRepository.save(ucIntroDatosIA);

        CurricularUnit ucProgramacionLIDIA = new CurricularUnit("Programación I", 8, termLIDIA1);
        ucProgramacionLIDIA = curricularUnitRepository.save(ucProgramacionLIDIA);

        log.info("✓ Created curricular units for LIDIA at ITR Norte");

        // Obtener el teacher guardado desde las posiciones del user2
        final Campus finalCampusRivera = campusRivera;
        Teacher savedTeacherRivera = (Teacher) user2.getPositions().stream()
            .filter(p -> p instanceof Teacher && p.getCampuses().contains(finalCampusRivera))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Teacher position not found for user2"));

        // Crear curso para ITR Norte (solo User 2 tiene acceso)
        Course courseRivera = new Course(
            Shift.EVENING,
            "Curso de Introducción a Datos e IA - Grupo 1",
            LocalDate.of(2025, 3, 1),
            LocalDate.of(2025, 7, 15),
            PartialGradingSystem.PGS_2,
            ucIntroDatosIA
        );
        courseRivera.getTeachers().add(savedTeacherRivera);
        courseRivera.getHoursPerDeliveryFormat().put(DeliveryFormat.IN_PERSON, 50);
        courseRivera.getHoursPerDeliveryFormat().put(DeliveryFormat.VIRTUAL, 15);
        courseRivera.getSustainableDevelopmentGoals().add(SustainableDevelopmentGoal.SDG_4);
        courseRivera.getSustainableDevelopmentGoals().add(SustainableDevelopmentGoal.SDG_9);
        courseRivera.getUniversalDesignLearningPrinciples().add(UniversalDesignLearningPrinciple.MEANS_OF_REPRESENTATION);
        courseRivera.getUniversalDesignLearningPrinciples().add(UniversalDesignLearningPrinciple.MEANS_OF_ENGAGEMENT);
        courseRivera = courseRepository.save(courseRivera);
        log.info("✓ Created course for ITR Norte: {} (ID: {})", courseRivera.getDescription(), courseRivera.getId());

        // ========================================
        // TERCER USUARIO: SOLO ANALYST
        // ========================================
        log.info("Creating third user (Analyst only) for testing...");
        PersonalData personalData3 = new PersonalData();
        personalData3.setName("Carlos");
        personalData3.setLastName("Rodríguez");
        personalData3.setIdentityDocument("11223344");
        personalData3.setPhoneNumber("099112233");
        personalData3.setCountry("Uruguay");
        personalData3.setCity("Durazno");

        User user3 = new User(
            "carlos.rodriguez@utec.edu.uy",
            passwordEncoder.encode("password"),
            personalData3
        );
        user3 = userRepository.save(user3);
        log.info("✓ Created third user: {} (ID: {})", user3.getUtecEmail(), user3.getId());

        // Carlos solo tiene rol de Analyst en ITR Centro-Sur (Campus Durazno)
        Analyst analystDurazno = new Analyst(user3);
        analystDurazno.addCampus(campusDurazno);
        user3.addPosition(analystDurazno);

        user3 = userRepository.save(user3);
        log.info("✓ User 3 has ONLY Analyst position at ITR Centro-Sur (Campus: Durazno)");

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
        log.info("🎓 PROGRAMS CREATED ({}): ", 15);
        log.info("   • Ingeniería en Mecatrónica");
        log.info("   • Ingeniería en Logística");
        log.info("   • Licenciatura en Tecnologías de la Información");
        log.info("   • Licenciatura en Ingeniería de Datos e Inteligencia Artificial");
        log.info("   • Ingeniería en Agua y Desarrollo Sostenible");
        log.info("   • Ingeniería en Energías Renovables");
        log.info("   • Tecnólogo en Informática");
        log.info("   • Tecnólogo en Mecatrónica Industrial");
        log.info("   • Y más...");
        log.info("");
        log.info("👥 USERS FOR ACCESS CONTROL TESTING:");
        log.info("");
        log.info("👤 USER 1: juan.perez@utec.edu.uy");
        log.info("Password: password");
        log.info("Positions:");
        log.info("  - Teacher at ITR Suroeste (Campuses: Fray Bentos, Mercedes)");
        log.info("  - Coordinator at ITR Suroeste (Campus: Fray Bentos)");
        log.info("  - Administrator at ITR Suroeste (Campus: Fray Bentos)");
        log.info("  - Education Manager at ITR Suroeste (Campus: Fray Bentos)");
        log.info("  - Analyst at ITR Suroeste (Campus: Fray Bentos)");
        log.info("✅ HAS ACCESS to: ITR Suroeste ONLY");
        log.info("⛔ NO ACCESS to: ITR Norte, ITR Centro-Sur, ITR Este");
        log.info("✅ Can access Course ID: {} (ITR Suroeste - Fray Bentos)", course.getId());
        log.info("");
        log.info("👤 USER 2: maria.gonzalez@utec.edu.uy");
        log.info("Password: password123");
        log.info("Positions:");
        log.info("  - Coordinator at ITR Norte (Campus: Rivera)");
        log.info("  - Teacher at ITR Norte (Campus: Rivera)");
        log.info("✅ HAS ACCESS to: ITR Norte ONLY");
        log.info("⛔ NO ACCESS to: ITR Suroeste, ITR Centro-Sur, ITR Este");
        log.info("✅ Can access Course ID: {} (ITR Norte - Rivera)", courseRivera.getId());
        log.info("⛔ CANNOT access Course ID: {} (ITR Suroeste)", course.getId());
        log.info("");
        log.info("👤 USER 3: carlos.rodriguez@utec.edu.uy");
        log.info("Password: password");
        log.info("Positions:");
        log.info("  - Analyst at ITR Centro-Sur (Campus: Durazno)");
        log.info("✅ HAS ACCESS to: ITR Centro-Sur ONLY");
        log.info("⛔ NO ACCESS to: ITR Norte, ITR Suroeste, ITR Este");
        log.info("⛔ CANNOT access Course ID: {} (ITR Suroeste)", course.getId());
        log.info("⛔ CANNOT access Course ID: {} (ITR Norte)", courseRivera.getId());
        log.info("");
        log.info("==================================================");
        log.info("🧪 TEST SCENARIOS TO VERIFY ACCESS CONTROL:");
        log.info("==================================================");
        log.info("");
        log.info("1️⃣  Login as User 1 (juan.perez@utec.edu.uy):");
        log.info("   ✅ GET /api/v1/courses/{} → 200 OK", course.getId());
        log.info("   ⛔ GET /api/v1/courses/{} → 403 FORBIDDEN", courseRivera.getId());
        log.info("   ✅ POST /api/v1/agent/chat/message (courseId={}) → 200 OK", course.getId());
        log.info("   ⛔ POST /api/v1/agent/chat/message (courseId={}) → 403 FORBIDDEN", courseRivera.getId());
        log.info("");
        log.info("2️⃣  Login as User 2 (maria.gonzalez@utec.edu.uy):");
        log.info("   ⛔ GET /api/v1/courses/{} → 403 FORBIDDEN", course.getId());
        log.info("   ✅ GET /api/v1/courses/{} → 200 OK", courseRivera.getId());
        log.info("   ⛔ POST /api/v1/agent/chat/message (courseId={}) → 403 FORBIDDEN", course.getId());
        log.info("   ✅ POST /api/v1/agent/chat/message (courseId={}) → 200 OK", courseRivera.getId());
        log.info("");
        log.info("3️⃣  Login as User 3 (carlos.rodriguez@utec.edu.uy):");
        log.info("   ⛔ GET /api/v1/courses/{} → 403 FORBIDDEN", course.getId());
        log.info("   ⛔ GET /api/v1/courses/{} → 403 FORBIDDEN", courseRivera.getId());
        log.info("   (Analyst has no courses in their ITR in this seed)");
        log.info("");
        log.info("==================================================");
        log.info("📍 Course Locations:");
        log.info("   Course {} → ITR Suroeste (Fray Bentos) - LTI", course.getId());
        log.info("   Course {} → ITR Norte (Rivera) - LIDIA", courseRivera.getId());
        log.info("==================================================");
    }

    private void createWeeklyPlanningsWithContent(Course course) {
        log.info("Creating WeeklyPlannings with content for course: {}", course.getDescription());

        // =============== SEMANA 1 ===============
        WeeklyPlanning week1 = new WeeklyPlanning(
            1,
            LocalDate.of(2025, 2, 24),
            LocalDate.of(2025, 3, 2)
        );

        // Contenido 1 - Semana 1
        ProgrammaticContent content1Week1 = new ProgrammaticContent(
            "Introducción a POO",
            "Conceptos fundamentales de Programación Orientada a Objetos: clases, objetos, encapsulación y abstracción",
            week1
        );
        content1Week1.setColor("#F8BBD0"); // Rosa suave

        Activity activity1Content1Week1 = new Activity(
            "Clase magistral sobre los pilares de POO con ejemplos en Java",
            120,
            LearningModality.IN_PERSON,
            content1Week1
        );
        activity1Content1Week1.setTitle("Clase Magistral POO");
        activity1Content1Week1.setColor("#E53935"); // Rojo intenso
        activity1Content1Week1.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.REMEMBER,
            CognitiveProcess.UNDERSTAND
        ));
        activity1Content1Week1.getLearningResources().add(LearningResource.DEMONSTRATION);
        activity1Content1Week1.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);
        activity1Content1Week1.getTeachingStrategies().add(TeachingStrategy.LECTURE);

        Activity activity2Content1Week1 = new Activity(
            "Ejercicios prácticos de creación de clases y objetos simples",
            180,
            LearningModality.IN_PERSON,
            content1Week1
        );
        activity2Content1Week1.setTitle("Práctica de Clases");
        activity2Content1Week1.setColor("#2979FF"); // Azul eléctrico
        activity2Content1Week1.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.APPLY,
            CognitiveProcess.ANALYZE
        ));
        activity2Content1Week1.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity2Content1Week1.getTransversalCompetencies().add(TransversalCompetency.LEARNING_SELF_REGULATION);
        activity2Content1Week1.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);

        content1Week1.getActivities().add(activity1Content1Week1);
        content1Week1.getActivities().add(activity2Content1Week1);

        // Contenido 2 - Semana 1
        ProgrammaticContent content2Week1 = new ProgrammaticContent(
            "Herencia y Polimorfismo",
            "Reutilización de código mediante herencia y comportamiento polimórfico en Java",
            week1
        );
        content2Week1.setColor("#B3E5FC"); // Celeste pastel

        Activity activity1Content2Week1 = new Activity(
            "Laboratorio práctico: crear jerarquías de clases con herencia",
            150,
            LearningModality.IN_PERSON,
            content2Week1
        );
        activity1Content2Week1.setTitle("Lab Herencia");
        activity1Content2Week1.setColor("#76FF03"); // Verde lima
        activity1Content2Week1.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        activity1Content2Week1.getLearningResources().add(LearningResource.DEMONSTRATION);
        activity1Content2Week1.getTransversalCompetencies().add(TransversalCompetency.TEAMWORK);
        activity1Content2Week1.getTeachingStrategies().add(TeachingStrategy.LABORATORY_PRACTICES);

        content2Week1.getActivities().add(activity1Content2Week1);

        week1.getProgrammaticContents().add(content1Week1);
        week1.getProgrammaticContents().add(content2Week1);
        course.getWeeklyPlannings().add(week1);

        // =============== SEMANA 2 ===============
        WeeklyPlanning week2 = new WeeklyPlanning(
            2,
            LocalDate.of(2025, 3, 3),
            LocalDate.of(2025, 3, 9)
        );

        // Contenido 1 - Semana 2
        ProgrammaticContent content1Week2 = new ProgrammaticContent(
            "Interfaces y Clases Abstractas",
            "Contratos y abstracción en Java: diferencias entre interfaces y clases abstractas",
            week2
        );
        content1Week2.setColor("#C8E6C9"); // Verde menta

        Activity activity1Week2 = new Activity(
            "Análisis de casos de uso para identificar cuándo usar interfaces vs clases abstractas",
            120,
            LearningModality.IN_PERSON,
            content1Week2
        );
        activity1Week2.setTitle("Análisis Interfaces");
        activity1Week2.setColor("#D81B60"); // Fucsia
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
            "Implementación práctica de interfaces y clases abstractas en proyectos",
            180,
            LearningModality.IN_PERSON,
            content1Week2
        );
        activity2Week2.setTitle("Implementación Interfaces");
        activity2Week2.setColor("#FF9100"); // Naranja vibrante
        activity2Week2.getCognitiveProcesses().add(CognitiveProcess.CREATE);
        activity2Week2.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity2Week2.getTransversalCompetencies().add(TransversalCompetency.LEARNING_SELF_REGULATION);
        activity2Week2.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);

        content1Week2.getActivities().add(activity1Week2);
        content1Week2.getActivities().add(activity2Week2);

        // Contenido 2 - Semana 2
        ProgrammaticContent content2Week2 = new ProgrammaticContent(
            "Excepciones y Manejo de Errores",
            "Técnicas para manejo robusto de errores y excepciones en aplicaciones Java",
            week2
        );
        content2Week2.setColor("#FFDAB9"); // Durazno claro

        Activity activity3Week2 = new Activity(
            "Práctica de try-catch y manejo de excepciones personalizadas",
            120,
            LearningModality.IN_PERSON,
            content2Week2
        );
        activity3Week2.setTitle("Práctica Excepciones");
        activity3Week2.setColor("#FFEB3B"); // Amarillo brillante
        activity3Week2.getCognitiveProcesses().add(CognitiveProcess.APPLY);
        activity3Week2.getLearningResources().add(LearningResource.BOOK_DOCUMENT);
        activity3Week2.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);
        activity3Week2.getTeachingStrategies().add(TeachingStrategy.PRACTICAL_ACTIVITY);

        content2Week2.getActivities().add(activity3Week2);

        week2.getProgrammaticContents().add(content1Week2);
        week2.getProgrammaticContents().add(content2Week2);
        course.getWeeklyPlannings().add(week2);

        // =============== SEMANA 3 ===============
        WeeklyPlanning week3 = new WeeklyPlanning(
            3,
            LocalDate.of(2025, 3, 10),
            LocalDate.of(2025, 3, 16)
        );

        // Contenido 1 - Semana 3
        ProgrammaticContent content1Week3 = new ProgrammaticContent(
            "Colecciones y Genéricos",
            "API de colecciones en Java: ArrayList, HashMap, Sets y uso de tipos genéricos",
            week3
        );
        content1Week3.setColor("#FFF9C4"); // Amarillo crema

        Activity activity1Week3 = new Activity(
            "Presentación del Collections Framework y sus principales estructuras de datos",
            90,
            LearningModality.IN_PERSON,
            content1Week3
        );
        activity1Week3.setTitle("Intro Collections");
        activity1Week3.setColor("#00B8D4"); // Turquesa fuerte
        activity1Week3.getCognitiveProcesses().addAll(Arrays.asList(
            CognitiveProcess.REMEMBER,
            CognitiveProcess.UNDERSTAND
        ));
        activity1Week3.getLearningResources().add(LearningResource.DEMONSTRATION);
        activity1Week3.getTransversalCompetencies().add(TransversalCompetency.CRITICAL_THINKING);
        activity1Week3.getTeachingStrategies().add(TeachingStrategy.LECTURE);

        Activity activity2Week3 = new Activity(
            "Laboratorio: implementar estructuras de datos usando colecciones de Java",
            210,
            LearningModality.IN_PERSON,
            content1Week3
        );
        activity2Week3.setTitle("Lab Colecciones");
        activity2Week3.setColor("#8E24AA"); // Violeta intenso
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
            "Tarea asincrónica: ejercicios de programación con genéricos para entregar",
            240,
            LearningModality.AUTONOMOUS,
            content1Week3
        );
        activity3Week3.setTitle("Tarea Genéricos");
        activity3Week3.setColor("#E53935"); // Rojo intenso
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

        // =============== SEMANA 4 ===============
        WeeklyPlanning week4 = new WeeklyPlanning(
            4,
            LocalDate.of(2025, 3, 17),
            LocalDate.of(2025, 3, 23)
        );

        // Contenido 1 - Semana 4
        ProgrammaticContent content1Week4 = new ProgrammaticContent(
            "Patrones de Diseño",
            "Patrones de diseño GOF: Singleton, Factory, Observer y otros patrones fundamentales",
            week4
        );
        content1Week4.setColor("#E1BEE7"); // Lavanda suave

        Activity activity1Week4 = new Activity(
            "Estudio y análisis de los principales patrones de diseño Gang of Four",
            120,
            LearningModality.IN_PERSON,
            content1Week4
        );
        activity1Week4.setTitle("Análisis Patrones");
        activity1Week4.setColor("#2979FF"); // Azul eléctrico
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
            "Trabajo en equipo: implementar patrones de diseño en un proyecto grupal",
            180,
            LearningModality.IN_PERSON,
            content1Week4
        );
        activity2Week4.setTitle("Proyecto Patrones");
        activity2Week4.setColor("#76FF03"); // Verde lima
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

        // Contenido 2 - Semana 4
        ProgrammaticContent content2Week4 = new ProgrammaticContent(
            "Testing y JUnit",
            "Pruebas unitarias y desarrollo guiado por tests (TDD) con JUnit 5",
            week4
        );
        content2Week4.setColor("#B2EBF2"); // Turquesa claro

        Activity activity3Week4 = new Activity(
            "Introducción a testing: escribir tests unitarios efectivos con JUnit",
            150,
            LearningModality.IN_PERSON,
            content2Week4
        );
        activity3Week4.setTitle("Intro Testing");
        activity3Week4.setColor("#D81B60"); // Fucsia
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
            "Práctica autónoma de Test-Driven Development aplicado a un proyecto real",
            300,
            LearningModality.AUTONOMOUS,
            content2Week4
        );
        activity4Week4.setTitle("Práctica TDD");
        activity4Week4.setColor("#FF9100"); // Naranja vibrante
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

        // =============== SEMANAS 5-18: VACÍAS ===============
        // Calcular cuántas semanas hay entre el inicio y fin del curso
        LocalDate startDate = course.getStartDate();
        LocalDate endDate = course.getEndDate();
        long totalWeeks = java.time.temporal.ChronoUnit.WEEKS.between(startDate, endDate) + 1;

        for (int weekNum = 5; weekNum <= totalWeeks; weekNum++) {
            LocalDate weekStart = startDate.plusWeeks(weekNum - 1);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            // Asegurarse de que no exceda la fecha de fin
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

        // Guardar el curso con todas las planificaciones
        courseRepository.save(course);
        log.info("Created {} weekly plannings (4 with content, {} empty)", totalWeeks, totalWeeks - 4);
    }
}
