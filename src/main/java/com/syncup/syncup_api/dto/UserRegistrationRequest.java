package com.syncup.syncup_api.dto;

import java.time.LocalDate;
import lombok.Data;

// DTO para la solicitud de registro de un nuevo usuario
@Data
public class UserRegistrationRequest {
    private String username;
    private String password;
    private String nombre;
    private LocalDate fechaNacimiento;
    private String genero;
}