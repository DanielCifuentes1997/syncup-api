package com.syncup.syncup_api.dto;

import lombok.Data;

// RF-001: Esto es lo que el usuario envía para registrarse
@Data
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String nombre;
}