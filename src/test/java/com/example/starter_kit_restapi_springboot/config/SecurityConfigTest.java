package com.example.starter_kit_restapi_springboot.config;

import com.example.starter_kit_restapi_springboot.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = SecurityConfigTest.TestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void securityFilterChainShouldBeCreated() {
        assertThat(securityFilterChain).isNotNull();
    }

    @Test
    void constructorShouldCreateConfigurationInstance() {
        SecurityConfig config = new SecurityConfig(
                mock(JwtAuthenticationFilter.class),
                mock(AuthenticationProvider.class),
                mock(LogoutHandler.class)
        );

        assertThat(config).isNotNull();
    }

    @EnableAutoConfiguration
    @Import(SecurityConfig.class)
    static class TestApplication {

        @Bean
        JwtAuthenticationFilter jwtAuthenticationFilter() {
            return mock(JwtAuthenticationFilter.class);
        }

        @Bean
        AuthenticationProvider authenticationProvider() {
            return mock(AuthenticationProvider.class);
        }

        @Bean
        LogoutHandler logoutHandler() {
            return mock(LogoutHandler.class);
        }
    }
}
