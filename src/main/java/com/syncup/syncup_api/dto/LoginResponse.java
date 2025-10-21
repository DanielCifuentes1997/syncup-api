package com.syncup.syncup_api.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;

    // Constructor para que sea fácil crear la respuesta
    public LoginResponse(String token) {
        this.token = token;
    }
}