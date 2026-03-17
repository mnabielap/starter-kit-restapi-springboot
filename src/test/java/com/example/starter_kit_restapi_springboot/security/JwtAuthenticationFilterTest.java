package com.example.starter_kit_restapi_springboot.security;

import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.Token;
import com.example.starter_kit_restapi_springboot.entity.User;
import com.example.starter_kit_restapi_springboot.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternalShouldSkipWhenAuthorizationHeaderIsMissing() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);

        filter.doFilterInternal(new MockHttpServletRequest(), new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternalShouldSkipWhenAuthorizationHeaderIsNotBearer() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(any(), any());
        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void doFilterInternalShouldSkipWhenUsernameIsNull() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");

        when(jwtService.extractUsername("access-token")).thenReturn(null);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void doFilterInternalShouldSkipWhenAuthenticationAlreadyExists() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null)
        );

        when(jwtService.extractUsername("access-token")).thenReturn("alice@example.com");

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        verify(userDetailsService, never()).loadUserByUsername("alice@example.com");
    }

    @Test
    void doFilterInternalShouldNotAuthenticateWhenStoredTokenIsInvalid() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        User user = user();

        when(jwtService.extractUsername("access-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(user);
        when(tokenRepository.findByToken("access-token")).thenReturn(Optional.of(Token.builder().expired(true).revoked(false).build()));
        when(jwtService.isTokenValid("access-token", user)).thenReturn(true);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternalShouldNotAuthenticateWhenStoredTokenIsMissing() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        User user = user();

        when(jwtService.extractUsername("access-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(user);
        when(tokenRepository.findByToken("access-token")).thenReturn(Optional.empty());
        when(jwtService.isTokenValid("access-token", user)).thenReturn(true);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternalShouldNotAuthenticateWhenJwtValidationFails() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        User user = user();

        when(jwtService.extractUsername("access-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(user);
        when(tokenRepository.findByToken("access-token")).thenReturn(Optional.of(Token.builder().expired(false).revoked(false).build()));
        when(jwtService.isTokenValid("access-token", user)).thenReturn(false);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternalShouldAuthenticateWhenJwtAndStoredTokenAreValid() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService, tokenRepository);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer access-token");
        User user = user();

        when(jwtService.extractUsername("access-token")).thenReturn("alice@example.com");
        when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(user);
        when(tokenRepository.findByToken("access-token")).thenReturn(Optional.of(Token.builder().expired(false).revoked(false).build()));
        when(jwtService.isTokenValid("access-token", user)).thenReturn(true);

        filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(user);
    }

    private User user() {
        return User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }
}
