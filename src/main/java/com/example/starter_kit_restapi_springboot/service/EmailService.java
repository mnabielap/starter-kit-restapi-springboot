package com.example.starter_kit_restapi_springboot.service;

/**
 * Interface for the email sending service.
 */
public interface EmailService {

    /**
     * Sends an email for password reset.
     *
     * @param to    The recipient's email address.
     * @param token The password reset token.
     */
    void sendResetPasswordEmail(String to, String token);

    /**
     * Sends an email for account verification.
     *
     * @param to    The recipient's email address.
     * @param token The email verification token.
     */
    void sendVerificationEmail(String to, String token);
}