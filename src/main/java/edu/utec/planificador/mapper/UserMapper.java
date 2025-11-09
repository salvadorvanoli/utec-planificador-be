package edu.utec.planificador.mapper;

import edu.utec.planificador.dto.response.UserBasicResponse;
import edu.utec.planificador.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserBasicResponse toBasicResponse(User user) {
        if (user == null) {
            return null;
        }

        String fullName = null;
        if (user.getPersonalData() != null) {
            fullName = user.getPersonalData().getName() + " " + user.getPersonalData().getLastName();
        }

        return UserBasicResponse.builder()
            .id(user.getId())
            .email(user.getUtecEmail())
            .fullName(fullName)
            .build();
    }
}
