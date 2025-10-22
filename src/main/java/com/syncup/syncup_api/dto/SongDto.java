package com.syncup.syncup_api.dto;

import com.opencsv.bean.CsvBindByName;
import com.syncup.syncup_api.domain.Cancion;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) para representar la información
 * pública de una canción. Incluye anotaciones para mapeo CSV.
 */
@Data
@NoArgsConstructor // OpenCSV CsvToBean necesita un constructor sin argumentos
public class SongDto {

    // Mapea la columna 'ID' del CSV a este campo
    @CsvBindByName(column = "ID")
    private Long id;

    // Mapea la columna 'Titulo' del CSV a este campo
    @CsvBindByName(column = "Titulo", required = true) // 'required = true' para importación
    private String titulo;

    // Mapea la columna 'Artista' del CSV
    @CsvBindByName(column = "Artista", required = true)
    private String artista;

    // Mapea la columna 'Genero' del CSV
    @CsvBindByName(column = "Genero", required = true)
    private String genero;

    // Mapea la columna 'Anio' del CSV
    @CsvBindByName(column = "Anio", required = true)
    private int anio;

    // Mapea la columna 'Duracion' del CSV (en segundos)
    @CsvBindByName(column = "Duracion", required = true)
    private int duracion;

    //Constructor para convertir una entidad Cancion a SongDto.
     
    public SongDto(Cancion cancion) {
        this.id = cancion.getId();
        this.titulo = cancion.getTitulo();
        this.artista = cancion.getArtista();
        this.genero = cancion.getGenero();
        this.anio = cancion.getAnio();
        this.duracion = cancion.getDuracion();
    }
}