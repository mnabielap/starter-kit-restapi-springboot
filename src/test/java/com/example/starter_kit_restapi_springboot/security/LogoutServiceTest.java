package com.example.starter_kit_restapi_springboot.security;

import com.example.starter_kit_restapi_springboot.entity.Token;
import com.example.starter_kit_restapi_springboot.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private LogoutService logoutService;

    @Test
    void logoutShouldReturnWhenHeaderIsMissing() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, null);

        verify(tokenRepository, never()).findByToken(anyString());
    }

    @Test
    void logoutShouldReturnWhenHeaderIsNotBearer() {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        logoutService.logout(request, response, null);

        verify(tokenRepository, never()).findByToken(anyString());
    }

    @Test
    void logoutShouldIgnoreUnknownToken() {
        when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
        when(tokenRepository.findByToken("access-token")).thenReturn(Optional.empty());

        logoutService.logout(request, response, null);
    }

    @Test
    void logoutShouldRevokeStoredToken() {
        Token token = Token.builder().expired(false).revoked(false).build();
        when(request.getHeader("Authorization")).thenReturn("Bearer access-token");
        when(tokenRepository.findByToken("access-token")).thenReturn(Optional.of(token));

        logoutService.logout(request, response, null);

        verify(tokenRepository).save(token);
    }
}
