package com.syncup.syncup_api.dto;

import com.syncup.syncup_api.domain.Usuario;
import lombok.Data;

/**
 * DTO (Data Transfer Object) para representar la información
 * pública de un usuario. Se usa para evitar exponer
 * campos sensibles (como la contraseña) en las respuestas de la API.
 */
@Data
public class UserDto {

    private String username;
    private String nombre;

    /**
     * Constructor que facilita la conversión
     * de una entidad Usuario (de la BD) a un UserDto (para la API).
     */
    public UserDto(Usuario usuario) {
        this.username = usuario.getUsername();
        this.nombre = usuario.getNombre();
    }
}