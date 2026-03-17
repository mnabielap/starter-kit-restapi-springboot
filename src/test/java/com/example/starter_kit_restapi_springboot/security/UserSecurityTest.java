package com.example.starter_kit_restapi_springboot.security;

import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

class UserSecurityTest {

    private final UserSecurity userSecurity = new UserSecurity();

    @Test
    void hasUserIdShouldReturnFalseWhenAuthenticationIsNull() {
        assertThat(userSecurity.hasUserId(null, 1L)).isFalse();
    }

    @Test
    void hasUserIdShouldReturnFalseWhenPrincipalIsNotUser() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("alice", null);

        assertThat(userSecurity.hasUserId(authentication, 1L)).isFalse();
    }

    @Test
    void hasUserIdShouldReturnFalseWhenIdsDoNotMatch() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user(1L), null);

        assertThat(userSecurity.hasUserId(authentication, 2L)).isFalse();
    }

    @Test
    void hasUserIdShouldReturnTrueWhenIdsMatch() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(user(1L), null);

        assertThat(userSecurity.hasUserId(authentication, 1L)).isTrue();
    }

    private User user(Long id) {
        return User.builder()
                .id(id)
                .email("alice@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }
}
