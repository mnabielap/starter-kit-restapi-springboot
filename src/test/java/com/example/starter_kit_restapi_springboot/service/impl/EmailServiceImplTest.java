package com.example.starter_kit_restapi_springboot.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void sendResetPasswordEmailShouldSendExpectedMessage() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "support@example.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://frontend.example.com");

        emailService.sendResetPasswordEmail("alice@example.com", "reset-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getFrom()).isEqualTo("support@example.com");
        assertThat(captor.getValue().getTo()).containsExactly("alice@example.com");
        assertThat(captor.getValue().getSubject()).isEqualTo("Reset Your Password");
        assertThat(captor.getValue().getText()).contains("https://frontend.example.com/reset-password?token=reset-token");
    }

    @Test
    void sendVerificationEmailShouldSendExpectedMessage() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "support@example.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://frontend.example.com");

        emailService.sendVerificationEmail("alice@example.com", "verify-token");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getSubject()).isEqualTo("Verify Your Email Address");
        assertThat(captor.getValue().getText()).contains("https://frontend.example.com/verify-email?token=verify-token");
    }

    @Test
    void sendSimpleMailMessageShouldSwallowMailerFailures() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "support@example.com");
        ReflectionTestUtils.setField(emailService, "frontendUrl", "https://frontend.example.com");
        doThrow(new RuntimeException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendVerificationEmail("alice@example.com", "verify-token");
    }
}
