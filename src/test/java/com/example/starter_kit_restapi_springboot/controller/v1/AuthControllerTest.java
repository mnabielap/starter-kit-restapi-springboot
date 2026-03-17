package com.example.starter_kit_restapi_springboot.controller.v1;

import com.example.starter_kit_restapi_springboot.dto.request.ForgotPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.request.LoginRequest;
import com.example.starter_kit_restapi_springboot.dto.request.RegisterRequest;
import com.example.starter_kit_restapi_springboot.dto.request.ResetPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.response.AuthResponse;
import com.example.starter_kit_restapi_springboot.dto.response.UserResponse;
import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import com.example.starter_kit_restapi_springboot.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerShouldReturnCreatedResponse() {
        RegisterRequest request = new RegisterRequest();
        AuthResponse response = authResponse();
        when(authService.register(request)).thenReturn(response);

        var entity = authController.register(request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isEqualTo(response);
    }

    @Test
    void loginShouldReturnOkResponse() {
        LoginRequest request = new LoginRequest();
        AuthResponse response = authResponse();
        when(authService.login(request)).thenReturn(response);

        var entity = authController.login(request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody()).isEqualTo(response);
    }

    @Test
    void refreshTokenShouldDelegateToService() throws IOException {
        HttpServletRequest request = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);

        authController.refreshToken(request, response);

        verify(authService).refreshToken(request, response);
    }

    @Test
    void forgotPasswordShouldReturnNoContent() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();

        assertThat(authController.forgotPassword(request).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).forgotPassword(request);
    }

    @Test
    void resetPasswordShouldReturnNoContent() {
        ResetPasswordRequest request = new ResetPasswordRequest();

        assertThat(authController.resetPassword("token", request).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).resetPassword("token", request);
    }

    @Test
    void sendVerificationEmailShouldReturnNoContent() {
        User user = User.builder().id(1L).email("alice@example.com").password("encoded-password").role(Role.USER).build();

        assertThat(authController.sendVerificationEmail(user).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).sendVerificationEmail(user);
    }

    @Test
    void verifyEmailShouldReturnNoContent() {
        assertThat(authController.verifyEmail("token").getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(authService).verifyEmail("token");
    }

    private AuthResponse authResponse() {
        return AuthResponse.builder()
                .user(UserResponse.builder()
                        .id(1L)
                        .name("Alice")
                        .email("alice@example.com")
                        .role(Role.USER)
                        .build())
                .tokens(AuthResponse.TokensWrapper.builder()
                        .access(AuthResponse.TokenDetail.builder().token("access-token").build())
                        .refresh(AuthResponse.TokenDetail.builder().token("refresh-token").build())
                        .build())
                .build();
    }
}
