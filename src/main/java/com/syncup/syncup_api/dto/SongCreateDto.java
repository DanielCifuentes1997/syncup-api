package com.syncup.syncup_api.dto;

import lombok.Data;

@Data
public class SongCreateDto {

    private String titulo;
    private String artista;
    private String genero;
    private int anio;
    private int duracion; 
    private String filename;

}