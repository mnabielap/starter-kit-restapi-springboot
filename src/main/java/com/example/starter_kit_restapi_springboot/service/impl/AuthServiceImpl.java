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
import com.example.starter_kit_restapi_springboot.service.AuthService;
import com.example.starter_kit_restapi_springboot.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already taken");
        }

        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isEmailVerified(false)
                .build();
        var savedUser = userRepository.save(user);

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(savedUser, accessToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        var accessToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        tokenRepository.deleteAllBearerTokensByUserId(user.getId());
        saveUserToken(user, accessToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            var user = this.userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user);
                
                tokenRepository.deleteAllBearerTokensByUserId(user.getId());
                saveUserToken(user, accessToken);
                
                var authResponse = AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .build();
                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No user found with this email"));

        String resetToken = UUID.randomUUID().toString();

        Token token = Token.builder()
                .user(user)
                .token(resetToken)
                .tokenType(TokenType.RESET_PASSWORD)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);

        emailService.sendResetPasswordEmail(user.getEmail(), resetToken);
    }

    @Override
    @Transactional
    public void resetPassword(String token, ResetPasswordRequest request) {
        Token resetToken = tokenRepository.findByTokenAndTokenType(token, TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired token"));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Invalidate the token after use
        resetToken.setRevoked(true);
        resetToken.setExpired(true);
        tokenRepository.save(resetToken);
    }

    @Override
    @Transactional
    public void sendVerificationEmail(User user) {
        String verificationToken = UUID.randomUUID().toString();

        Token token = Token.builder()
                .user(user)
                .token(verificationToken)
                .tokenType(TokenType.VERIFY_EMAIL)
                .build();
        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        Token verificationToken = tokenRepository.findByTokenAndTokenType(token, TokenType.VERIFY_EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token"));

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        // Invalidate the token after use
        verificationToken.setRevoked(true);
        verificationToken.setExpired(true);
        tokenRepository.save(verificationToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    @SuppressWarnings("unused")
    private void revokeAllUserTokens(User user) {
        var validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }
}