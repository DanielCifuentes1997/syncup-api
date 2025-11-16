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

   
    private LocalDate fechaNacimiento;
    private String genero;


    private boolean haCompletadoOnboarding = false;

    
    @ManyToMany
    private List<Cancion> listaFavoritos;

  
    @ManyToMany
    @JoinTable(
        name = "conexiones_sociales",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "seguido_id")
    )
    private Set<Usuario> seguidos = new HashSet<>();
}