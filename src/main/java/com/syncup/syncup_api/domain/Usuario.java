package com.syncup.syncup_api.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.Set; 
import java.util.HashSet; 

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import lombok.Getter; // Reemplazo de @Data
import lombok.Setter; // Reemplazo de @Data
import lombok.NoArgsConstructor; // Reemplazo de @Data

/**
 * Entidad que representa a los usuarios de la plataforma.
 * Almacena información de perfil y relaciones con Canciones y otros Usuarios.
 */
@Entity
@Table(name = "usuarios")
@Getter // Se usa @Getter y @Setter en lugar de @Data
@Setter // para evitar bucles infinitos en relaciones @ManyToMany
@NoArgsConstructor // Requerido por JPA
@EqualsAndHashCode(of = "username") // RF-017: Basado en username
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(unique = true) // RF-015
    private String username;

    private String password; // RF-015
    private String nombre; // RF-015

    /**
     * Lista de canciones favoritas del usuario.
     * Cumple con RF-015.
     */
    @ManyToMany
    private List<Cancion> listaFavoritos;

    /**
     * Conjunto de usuarios a los que este usuario sigue.
     * Modela la relación "seguir" para RF-007.
     * Se usa Set para garantizar que no haya seguimientos duplicados.
     */
    @ManyToMany
    @JoinTable(name = "conexiones_sociales", // Nombre de la tabla intermedia en la BD
            joinColumns = @JoinColumn(name = "usuario_id"), // Columna que referencia a esta entidad
            inverseJoinColumns = @JoinColumn(name = "seguido_id") // Columna que referencia al usuario seguido
    )
    private Set<Usuario> seguidos = new HashSet<>();
}