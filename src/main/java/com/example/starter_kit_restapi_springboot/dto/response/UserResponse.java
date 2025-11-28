package com.example.starter_kit_restapi_springboot.dto.response;

import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;

    public static UserResponse fromUser(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}