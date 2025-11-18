package com.syncup.syncup_api.dto;

import lombok.Data;

@Data
public class UserUpdateDto {
    private String nombre;
    private String currentPassword; // Para seguridad, pedimos la actual
    private String newPassword;
}