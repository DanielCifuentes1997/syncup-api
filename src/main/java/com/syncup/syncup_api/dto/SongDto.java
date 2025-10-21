package com.syncup.syncup_api.dto;

import com.syncup.syncup_api.domain.Cancion;
import lombok.Data;

/**
 * DTO (Data Transfer Object) para representar la información
 * pública de una canción.
 */
@Data
public class SongDto {

    private Long id;
    private String titulo;
    private String artista;
    private String genero;
    private int anio;
    private int duracion;

    /**
     * Constructor que facilita la conversión
     * de una entidad Cancion (de la BD) a un SongDto (para la API).
     */
    public SongDto(Cancion cancion) {
        this.id = cancion.getId();
        this.titulo = cancion.getTitulo();
        this.artista = cancion.getArtista();
        this.genero = cancion.getGenero();
        this.anio = cancion.getAnio();
        this.duracion = cancion.getDuracion();
    }
}