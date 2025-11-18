package com.syncup.syncup_api.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private String nombre;
    private boolean haCompletadoOnboarding;

    public LoginResponse(String token, String nombre, boolean haCompletadoOnboarding) {
        this.token = token;
        this.nombre = nombre;
        this.haCompletadoOnboarding = haCompletadoOnboarding;
    }
}