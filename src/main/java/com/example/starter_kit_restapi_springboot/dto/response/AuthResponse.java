package com.example.starter_kit_restapi_springboot.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private UserResponse user;
    private TokensWrapper tokens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokensWrapper {
        private TokenDetail access;
        private TokenDetail refresh;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenDetail {
        private String token;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "UTC")
        private Date expires;
    }
}