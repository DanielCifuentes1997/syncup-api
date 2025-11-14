package com.syncup.syncup_api.domain;

import java.time.LocalDate;
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
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

// Entidad que representa a los usuarios de la plataforma.
// Almacena información de perfil y relaciones con Canciones y otros Usuarios.
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "username")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @jakarta.persistence.Column(unique = true)
    private String username;

    private String password;
    private String nombre;

    // Nuevos campos para el registro estilo Spotify
    private LocalDate fechaNacimiento;
    private String genero;

    // Lista de canciones favoritas del usuario.
    @ManyToMany
    private List<Cancion> listaFavoritos;

    // Conjunto de usuarios a los que este usuario sigue.
    @ManyToMany
    @JoinTable(
        name = "conexiones_sociales",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "seguido_id")
    )
    private Set<Usuario> seguidos = new HashSet<>();
}