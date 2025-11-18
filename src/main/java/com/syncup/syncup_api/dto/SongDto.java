package com.syncup.syncup_api.dto;

import com.opencsv.bean.CsvBindByName;
import com.syncup.syncup_api.domain.Cancion;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
public class SongDto {

    @CsvBindByName(column = "ID")
    private Long id;

    @CsvBindByName(column = "Titulo", required = true) 
    private String titulo;

    @CsvBindByName(column = "Artista", required = true)
    private String artista;

    @CsvBindByName(column = "Genero", required = true)
    private String genero;

    @CsvBindByName(column = "Anio", required = true)
    private int anio;

    @CsvBindByName(column = "Duracion", required = true)
    private int duracion;
    
    @CsvBindByName(column = "Filename", required = true)
    private String filename;

    public SongDto(Cancion cancion) {
        this.id = cancion.getId();
        this.titulo = cancion.getTitulo();
        this.artista = cancion.getArtista();
        this.genero = cancion.getGenero();
        this.anio = cancion.getAnio();
        this.duracion = cancion.getDuracion();
        this.filename = cancion.getFilename();
    }
}