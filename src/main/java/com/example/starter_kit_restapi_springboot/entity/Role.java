package com.example.starter_kit_restapi_springboot.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    USER,
    ADMIN;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        return Role.valueOf(value.toUpperCase());
    }
}