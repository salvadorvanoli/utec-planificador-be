package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.request.OfficeHoursRequest;
import edu.utec.planificador.dto.response.OfficeHoursResponse;
import edu.utec.planificador.entity.Course;
import edu.utec.planificador.entity.OfficeHours;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.repository.CourseRepository;
import edu.utec.planificador.repository.OfficeHoursRepository;
import edu.utec.planificador.service.AccessControlService;
import edu.utec.planificador.service.OfficeHoursService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfficeHoursServiceImpl implements OfficeHoursService {

    private final OfficeHoursRepository officeHoursRepository;
    private final CourseRepository courseRepository;
    private final AccessControlService accessControlService;

    @Override
    @Transactional
    public OfficeHoursResponse createOfficeHours(OfficeHoursRequest request) {
        log.debug("Creating office hours for course {}", request.getCourseId());

        // Validate planning management access to the course (ensures teachers can only manage planning for their own courses)
        accessControlService.validateCoursePlanningManagement(request.getCourseId());

        // Validate that endHour is after startHour
        if (request.getEndHour() <= request.getStartHour()) {
            throw new IllegalArgumentException("End hour must be after start hour");
        }

        Course course = courseRepository.findById(request.getCourseId())
            .orElseThrow(() -> new ResourceNotFoundException("Course not found with id: " + request.getCourseId()));

        // Validate that office hours date is within course period
        if (request.getDate().isBefore(course.getStartDate())) {
            throw new IllegalArgumentException("Office hours date must be on or after course start date (" + course.getStartDate() + ")");
        }
        if (request.getDate().isAfter(course.getEndDate())) {
            throw new IllegalArgumentException("Office hours date must be on or before course end date (" + course.getEndDate() + ")");
        }

        OfficeHours officeHours = new OfficeHours(
            request.getDate(),
            request.getStartHour(),
            request.getEndHour(),
            course
        );

        OfficeHours savedOfficeHours = officeHoursRepository.save(officeHours);

        log.info("Office hours created successfully with id: {}", savedOfficeHours.getId());

        return mapToResponse(savedOfficeHours);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeHoursResponse> getOfficeHoursByCourseId(Long courseId) {
        log.debug("Getting office hours for course {}", courseId);

        // Validate access to course
        accessControlService.validateCourseAccess(courseId);

        // Validate that course exists
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found with id: " + courseId);
        }

        List<OfficeHours> officeHoursList = officeHoursRepository.findByCourseId(courseId);

        log.info("Found {} office hours for course {}", officeHoursList.size(), courseId);

        return officeHoursList.stream()
            .map(this::mapToResponse)
            .toList();
    }

    @Override
    @Transactional
    public void deleteOfficeHours(Long id) {
        log.debug("Deleting office hours with id: {}", id);

        OfficeHours officeHours = officeHoursRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Office hours not found with id: " + id));

        // Validate planning management access to the course (ensures teachers can only manage planning for their own courses)
        accessControlService.validateCoursePlanningManagement(officeHours.getCourse().getId());

        officeHoursRepository.delete(officeHours);

        log.info("Office hours deleted successfully with id: {}", id);
    }

    private OfficeHoursResponse mapToResponse(OfficeHours officeHours) {
        return OfficeHoursResponse.builder()
            .id(officeHours.getId())
            .date(officeHours.getDate())
            .startHour(officeHours.getStartHour())
            .endHour(officeHours.getEndHour())
            .courseId(officeHours.getCourse().getId())
            .build();
    }
}
