package com.syncup.syncup_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "canciones")
@Data 
@NoArgsConstructor 
@EqualsAndHashCode(of = "id") 
public class Cancion {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String artista;
    private String genero;
    private int anio;
    private int duracion; 
    
    private String filename;
}