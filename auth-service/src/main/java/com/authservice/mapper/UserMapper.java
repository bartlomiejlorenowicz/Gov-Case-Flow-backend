package com.authservice.mapper;


import com.authservice.domain.User;
import com.authservice.dto.UserDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRoles(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }
}
