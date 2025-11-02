package edu.utec.planificador.service.impl;

import edu.utec.planificador.dto.request.PositionRequest;
import edu.utec.planificador.entity.Administrator;
import edu.utec.planificador.entity.Analyst;
import edu.utec.planificador.entity.Campus;
import edu.utec.planificador.entity.Coordinator;
import edu.utec.planificador.entity.EducationManager;
import edu.utec.planificador.entity.Position;
import edu.utec.planificador.entity.Teacher;
import edu.utec.planificador.entity.User;
import edu.utec.planificador.exception.ResourceNotFoundException;
import edu.utec.planificador.exception.ValidationException;
import edu.utec.planificador.repository.CampusRepository;
import edu.utec.planificador.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl implements PositionService {

    private final CampusRepository campusRepository;

    @Override
    public Position createPosition(User user, PositionRequest request) {
        log.debug("Creating position {} for user {}", request.getType(), user.getUtecEmail());
        
        List<Campus> campuses = validateAndFetchCampuses(request.getCampusIds());
        
        Position position = instantiatePosition(request.getType(), user);
        
        campuses.forEach(position::addCampus);
        
        log.info("Position {} created successfully for user {}", request.getType(), user.getUtecEmail());
        
        return position;
    }

    private Position instantiatePosition(PositionRequest.PositionType type, User user) {
        return switch (type) {
            case TEACHER -> new Teacher(user);
            case COORDINATOR -> new Coordinator(user);
            case EDUCATION_MANAGER -> new EducationManager(user);
            case ANALYST -> new Analyst(user);
            case ADMINISTRATOR -> new Administrator(user);
        };
    }

    private List<Campus> validateAndFetchCampuses(List<Long> campusIds) {
        if (campusIds == null || campusIds.isEmpty()) {
            throw new ValidationException("validation.position.campusIds.required");
        }
        
        List<Campus> campuses = campusRepository.findAllById(campusIds);
        
        if (campuses.size() != campusIds.size()) {
            throw new ResourceNotFoundException("error.campus.notfound");
        }
        
        return campuses;
    }
}
