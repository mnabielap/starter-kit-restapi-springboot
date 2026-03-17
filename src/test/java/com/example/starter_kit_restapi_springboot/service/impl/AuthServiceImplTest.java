package com.example.starter_kit_restapi_springboot.service.impl;

import com.example.starter_kit_restapi_springboot.dto.request.ForgotPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.request.LoginRequest;
import com.example.starter_kit_restapi_springboot.dto.request.RegisterRequest;
import com.example.starter_kit_restapi_springboot.dto.request.ResetPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.response.AuthResponse;
import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.Token;
import com.example.starter_kit_restapi_springboot.entity.TokenType;
import com.example.starter_kit_restapi_springboot.entity.User;
import com.example.starter_kit_restapi_springboot.exception.DuplicateResourceException;
import com.example.starter_kit_restapi_springboot.exception.ResourceNotFoundException;
import com.example.starter_kit_restapi_springboot.repository.TokenRepository;
import com.example.starter_kit_restapi_springboot.repository.UserRepository;
import com.example.starter_kit_restapi_springboot.security.JwtService;
import com.example.starter_kit_restapi_springboot.service.EmailService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldCreateUserAndReturnAuthResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        User savedUser = user(1L, "Alice", "alice@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        mockTokens(savedUser);

        AuthResponse response = authService.register(request);

        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getTokens().getAccess().getToken()).isEqualTo("access-token");
        assertThat(response.getTokens().getRefresh().getToken()).isEqualTo("refresh-token");
        verify(tokenRepository).save(any(Token.class));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already taken");
    }

    @Test
    void loginShouldAuthenticateDeleteBearerTokensAndReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("password123");

        User user = user(11L, "Alice", "alice@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        mockTokens(user);

        AuthResponse response = authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo("alice@example.com");
        assertThat(captor.getValue().getCredentials()).isEqualTo("password123");
        verify(tokenRepository).deleteAllBearerTokensByUserId(11L);
        assertThat(response.getUser().getId()).isEqualTo(11L);
    }

    @Test
    void loginShouldFailWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest();
        request.setEmail("missing@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void refreshTokenShouldReturnImmediatelyWhenAuthorizationHeaderIsMissing() throws IOException {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        authService.refreshToken(request, response);

        verify(jwtService, never()).extractUsername(anyString());
    }

    @Test
    void refreshTokenShouldReturnImmediatelyWhenUsernameCannotBeExtracted() throws IOException {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer refresh-token");
        when(jwtService.extractUsername("refresh-token")).thenReturn(null);

        authService.refreshToken(request, response);

        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void refreshTokenShouldDoNothingWhenTokenIsInvalid() throws IOException {
        User user = user(12L, "Alice", "alice@example.com");
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer refresh-token");
        when(jwtService.extractUsername("refresh-token")).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refresh-token", user)).thenReturn(false);

        authService.refreshToken(request, response);

        verify(tokenRepository, never()).deleteAllBearerTokensByUserId(anyLong());
    }

    @Test
    void refreshTokenShouldFailWhenUserCannotBeFound() {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer refresh-token");
        when(jwtService.extractUsername("refresh-token")).thenReturn("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshToken(request, response))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void refreshTokenShouldWriteNewTokensToResponseWhenTokenIsValid() throws IOException {
        User user = user(13L, "Alice", "alice@example.com");
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer refresh-token");
        when(jwtService.extractUsername("refresh-token")).thenReturn("alice@example.com");
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("refresh-token", user)).thenReturn(true);
        when(response.getOutputStream()).thenReturn(new TestServletOutputStream(outputStream));
        mockTokens(user);

        authService.refreshToken(request, response);

        verify(tokenRepository).deleteAllBearerTokensByUserId(13L);
        assertThat(outputStream.toString(StandardCharsets.UTF_8))
                .contains("access-token")
                .contains("refresh-token")
                .contains("alice@example.com");
    }

    @Test
    void forgotPasswordShouldCreateResetTokenAndSendEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("alice@example.com");
        User user = user(14L, "Alice", "alice@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        authService.forgotPassword(request);

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenType()).isEqualTo(TokenType.RESET_PASSWORD);
        verify(emailService).sendResetPasswordEmail("alice@example.com", captor.getValue().getToken());
    }

    @Test
    void forgotPasswordShouldFailWhenUserDoesNotExist() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@example.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.forgotPassword(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No user found with this email");
    }

    @Test
    void resetPasswordShouldUpdatePasswordAndInvalidateToken() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("new-password");
        User user = user(15L, "Alice", "alice@example.com");
        Token resetToken = Token.builder()
                .token("reset-token")
                .tokenType(TokenType.RESET_PASSWORD)
                .user(user)
                .build();

        when(tokenRepository.findByTokenAndTokenType("reset-token", TokenType.RESET_PASSWORD))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-password");

        authService.resetPassword("reset-token", request);

        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(resetToken.isExpired()).isTrue();
        assertThat(resetToken.isRevoked()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(resetToken);
    }

    @Test
    void resetPasswordShouldFailWhenTokenIsUnknown() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setPassword("new-password");

        when(tokenRepository.findByTokenAndTokenType("missing-token", TokenType.RESET_PASSWORD))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("missing-token", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invalid or expired token");
    }

    @Test
    void sendVerificationEmailShouldCreateTokenAndSendEmail() {
        User user = user(16L, "Alice", "alice@example.com");

        authService.sendVerificationEmail(user);

        ArgumentCaptor<Token> captor = ArgumentCaptor.forClass(Token.class);
        verify(tokenRepository).save(captor.capture());
        assertThat(captor.getValue().getTokenType()).isEqualTo(TokenType.VERIFY_EMAIL);
        verify(emailService).sendVerificationEmail("alice@example.com", captor.getValue().getToken());
    }

    @Test
    void verifyEmailShouldMarkUserAsVerifiedAndInvalidateToken() {
        User user = user(17L, "Alice", "alice@example.com");
        Token token = Token.builder()
                .token("verify-token")
                .tokenType(TokenType.VERIFY_EMAIL)
                .user(user)
                .build();

        when(tokenRepository.findByTokenAndTokenType("verify-token", TokenType.VERIFY_EMAIL))
                .thenReturn(Optional.of(token));

        authService.verifyEmail("verify-token");

        assertThat(user.isEmailVerified()).isTrue();
        assertThat(token.isExpired()).isTrue();
        assertThat(token.isRevoked()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void verifyEmailShouldFailWhenTokenIsUnknown() {
        when(tokenRepository.findByTokenAndTokenType("verify-token", TokenType.VERIFY_EMAIL))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verifyEmail("verify-token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Invalid or expired verification token");
    }

    @Test
    void revokeAllUserTokensShouldReturnWhenNoValidTokensExist() {
        User user = user(18L, "Alice", "alice@example.com");
        when(tokenRepository.findAllValidTokenByUser(18L)).thenReturn(List.of());

        ReflectionTestUtils.invokeMethod(authService, "revokeAllUserTokens", user);

        verify(tokenRepository, never()).saveAll(any());
    }

    @Test
    void revokeAllUserTokensShouldMarkEveryTokenAsRevoked() {
        User user = user(19L, "Alice", "alice@example.com");
        Token firstToken = Token.builder().token("first").build();
        Token secondToken = Token.builder().token("second").build();
        when(tokenRepository.findAllValidTokenByUser(19L)).thenReturn(List.of(firstToken, secondToken));

        ReflectionTestUtils.invokeMethod(authService, "revokeAllUserTokens", user);

        assertThat(firstToken.isExpired()).isTrue();
        assertThat(firstToken.isRevoked()).isTrue();
        assertThat(secondToken.isExpired()).isTrue();
        assertThat(secondToken.isRevoked()).isTrue();
        verify(tokenRepository).saveAll(List.of(firstToken, secondToken));
    }

    private void mockTokens(User user) {
        when(jwtService.generateToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getAccessTokenExpiration()).thenReturn(30L);
        when(jwtService.getRefreshTokenExpiration()).thenReturn(7L);
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private User user(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .password("encoded-password")
                .role(Role.USER)
                .build();
    }

    private static final class TestServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream outputStream;

        private TestServletOutputStream(ByteArrayOutputStream outputStream) {
            this.outputStream = outputStream;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public void write(int b) {
            outputStream.write(b);
        }
    }
}
