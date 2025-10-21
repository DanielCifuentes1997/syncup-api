package com.syncup.syncup_api.domain;

import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity // Le dice a Spring que esto es una entidad (tabla) de BD
@Table(name = "usuarios") // Nombra la tabla en plural
@Data // Genera getters, setters, toString() (Gracias a Lombok)
@NoArgsConstructor // Genera un constructor vacío (Requerido por JPA)
@EqualsAndHashCode(of = "username") // RF-017: hashCode y equals basados en 'username'
public class Usuario {

    @Id // Marca esto como la llave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental
    private Long id;

    // RF-015: Almacenar username (único), password, nombre
    @jakarta.persistence.Column(unique = true) // RF-015: username (único)
    private String username;

    private String password;
    private String nombre;

    // RF-015: listaFavoritos (LinkedList<Cancion>)
    @ManyToMany
    private List<Cancion> listaFavoritos = new LinkedList<>();
}