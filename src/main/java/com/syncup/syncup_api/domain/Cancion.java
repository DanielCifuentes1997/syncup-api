package com.syncup.syncup_api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity // Le dice a Spring que esto es una entidad (tabla) de BD
@Table(name = "canciones")
@Data // Genera getters, setters, toString() (Gracias a Lombok)
@NoArgsConstructor // Genera un constructor vacío (Requerido por JPA)
@EqualsAndHashCode(of = "id") // RF-020: hashCode y equals basados en 'id'
public class Cancion {

    @Id // Marca esto como la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // RF-018: id (único)
    private Long id;

    // RF-018: titulo, artista, genero, año, duracion
    private String titulo;
    private String artista;
    private String genero;
    private int anio;
    private int duracion; // Duración en segundos
}