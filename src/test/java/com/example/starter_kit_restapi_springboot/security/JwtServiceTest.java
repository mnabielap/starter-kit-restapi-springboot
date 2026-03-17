package com.example.starter_kit_restapi_springboot.security;

import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService();

    private User user;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 10L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 5L);

        user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }

    @Test
    void extractUsernameShouldReturnSubjectFromToken() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@example.com");
    }

    @Test
    void extractClaimShouldReadCustomClaim() {
        String token = jwtService.generateToken(Map.of("team", "qa"), user);

        String team = jwtService.extractClaim(token, claims -> claims.get("team", String.class));

        assertThat(team).isEqualTo("qa");
    }

    @Test
    void generateRefreshTokenShouldProduceValidTokenForUser() {
        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValidShouldReturnFalseWhenSubjectDoesNotMatch() {
        String token = jwtService.generateToken(user);
        User anotherUser = User.builder()
                .email("other@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();

        assertThat(jwtService.isTokenValid(token, anotherUser)).isFalse();
    }

    @Test
    void isTokenValidShouldThrowForExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1L);
        String token = jwtService.generateToken(user);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, user))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractClaimShouldExposeExpirationClaim() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractClaim(token, Claims::getExpiration)).isNotNull();
    }
}
