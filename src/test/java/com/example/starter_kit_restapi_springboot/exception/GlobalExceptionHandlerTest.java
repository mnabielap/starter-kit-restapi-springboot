package com.example.starter_kit_restapi_springboot.exception;

import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleResourceNotFoundExceptionShouldReturn404() {
        var response = handler.handleResourceNotFoundException(new ResourceNotFoundException("missing"));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("message", "missing");
    }

    @Test
    void handleDuplicateResourceExceptionShouldReturn400() {
        var response = handler.handleDuplicateResourceException(new DuplicateResourceException("duplicate"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("message", "duplicate");
    }

    @Test
    void handleAuthenticationExceptionShouldReturn401() {
        var response = handler.handleAuthenticationException(new BadCredentialsException("bad credentials"));

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).containsEntry("message", "Invalid email or password");
    }

    @Test
    void handleAccessDeniedExceptionShouldReturn403() {
        var response = handler.handleAccessDeniedException(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).containsEntry("message", "Forbidden");
    }

    @Test
    void handleHttpMessageNotReadableExceptionShouldReturn400() {
        var response = handler.handleHttpMessageNotReadableException(new HttpMessageNotReadableException("invalid json"));

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("message", "Malformed JSON request or invalid field format");
    }

    @Test
    void handleValidationExceptionsShouldJoinAllMessages() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "email", "Email should be valid"));
        bindingResult.addError(new FieldError("request", "password", "Password is required"));
        Method method = SampleController.class.getDeclaredMethod("sample", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(parameter, bindingResult);

        var response = handler.handleValidationExceptions(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().get("message")).isEqualTo("Email should be valid, Password is required");
    }

    @Test
    void handleGeneralExceptionShouldReturn500() {
        var response = handler.handleGeneralException(new IllegalStateException("boom"));

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred");
    }

    @SuppressWarnings("unused")
    private static final class SampleController {
        public void sample(String value) {
        }
    }
}
