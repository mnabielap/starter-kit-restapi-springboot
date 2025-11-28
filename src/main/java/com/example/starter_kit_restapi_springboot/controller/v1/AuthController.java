package com.example.starter_kit_restapi_springboot.controller.v1;

import com.example.starter_kit_restapi_springboot.dto.request.ForgotPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.request.LoginRequest;
import com.example.starter_kit_restapi_springboot.dto.request.RegisterRequest;
import com.example.starter_kit_restapi_springboot.dto.request.ResetPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.response.AuthResponse;
import com.example.starter_kit_restapi_springboot.entity.User;
import com.example.starter_kit_restapi_springboot.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        authService.refreshToken(request, response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String token, @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(token, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-verification-email")
    public ResponseEntity<Void> sendVerificationEmail(@AuthenticationPrincipal User user) {
        authService.sendVerificationEmail(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }
}