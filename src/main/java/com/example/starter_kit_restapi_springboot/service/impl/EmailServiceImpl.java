package com.example.starter_kit_restapi_springboot.service.impl;

import com.example.starter_kit_restapi_springboot.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    @Async
    public void sendResetPasswordEmail(String to, String token) {
        String subject = "Reset Your Password";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String text = "Dear user,\n\nTo reset your password, click on this link: " + resetUrl +
                "\n\nIf you did not request any password resets, then ignore this email.";
        sendSimpleMailMessage(to, subject, text);
    }

    @Override
    @Async
    public void sendVerificationEmail(String to, String token) {
        String subject = "Verify Your Email Address";
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;
        String text = "Dear user,\n\nTo verify your email, click on this link: " + verificationUrl +
                "\n\nIf you did not create an account, then ignore this email.";
        sendSimpleMailMessage(to, subject, text);
    }
    
    private void sendSimpleMailMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}