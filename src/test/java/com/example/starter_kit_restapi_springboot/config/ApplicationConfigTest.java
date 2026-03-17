package com.example.starter_kit_restapi_springboot.config;

import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import com.example.starter_kit_restapi_springboot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @Test
    void userDetailsServiceShouldLoadExistingUser() {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        User user = User.builder()
                .id(1L)
                .email("alice@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        var loadedUser = config.userDetailsService().loadUserByUsername("alice@example.com");

        assertThat(loadedUser).isEqualTo(user);
    }

    @Test
    void userDetailsServiceShouldThrowWhenUserIsMissing() {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> config.userDetailsService().loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void authenticationProviderShouldBeConfigured() {
        ApplicationConfig config = new ApplicationConfig(userRepository);

        AuthenticationProvider authenticationProvider = config.authenticationProvider();

        assertThat(authenticationProvider).isNotNull();
        assertThat(config.passwordEncoder().matches("password123", config.passwordEncoder().encode("password123"))).isTrue();
    }

    @Test
    void authenticationManagerShouldDelegateToConfiguration() throws Exception {
        ApplicationConfig config = new ApplicationConfig(userRepository);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        assertThat(config.authenticationManager(authenticationConfiguration)).isEqualTo(authenticationManager);
    }
}
