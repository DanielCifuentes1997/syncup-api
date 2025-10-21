package com.syncup.syncup_api.dto;

import lombok.Data;

/**
 * DTO para manejar la solicitud de creación de una nueva canción.
 * No incluye el 'id', ya que este será generado por la base de datos.
 */
@Data
public class SongCreateDto {

    private String titulo;
    private String artista;
    private String genero;
    private int anio;
    private int duracion; // Duración en segundos

}