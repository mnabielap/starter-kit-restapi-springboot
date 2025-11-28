package com.example.starter_kit_restapi_springboot.security;

import com.example.starter_kit_restapi_springboot.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
    public boolean hasUserId(Authentication authentication, Long userId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }
        User user = (User) authentication.getPrincipal();
        return user.getId().equals(userId);
    }
}