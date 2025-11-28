package com.example.starter_kit_restapi_springboot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}