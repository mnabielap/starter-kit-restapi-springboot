package com.example.starter_kit_restapi_springboot.service;

import com.example.starter_kit_restapi_springboot.dto.request.ForgotPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.request.LoginRequest;
import com.example.starter_kit_restapi_springboot.dto.request.RegisterRequest;
import com.example.starter_kit_restapi_springboot.dto.request.ResetPasswordRequest;
import com.example.starter_kit_restapi_springboot.dto.response.AuthResponse;
import com.example.starter_kit_restapi_springboot.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(String token, ResetPasswordRequest request);
    void sendVerificationEmail(User user);
    void verifyEmail(String token);
}