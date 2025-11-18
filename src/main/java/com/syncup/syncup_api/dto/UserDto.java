package com.syncup.syncup_api.dto;

import com.syncup.syncup_api.domain.Usuario;
import lombok.Data;

@Data
public class UserDto {

    private Long id;
    private String username;
    private String nombre;
    private String rol;

    public UserDto(Usuario usuario) {
        this.id = usuario.getId();
        this.username = usuario.getUsername();
        this.nombre = usuario.getNombre();
        this.rol = usuario.getRol();
    }
}