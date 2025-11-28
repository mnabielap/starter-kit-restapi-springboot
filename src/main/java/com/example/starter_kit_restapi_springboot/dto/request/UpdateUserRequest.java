package com.example.starter_kit_restapi_springboot.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email(message = "Email should be valid")
    private String email;

    private String name;
}