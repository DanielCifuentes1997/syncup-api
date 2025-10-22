package com.syncup.syncup_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.syncup.syncup_api.domain.Usuario;

import java.util.List;
import java.util.Optional;

// Repositorio para acceder a los datos de la entidad Usuario
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Busca un usuario por su nombre de usuario (username)
    Optional<Usuario> findByUsername(String username);

    // Busca todos los usuarios y carga inmediatamente ('fetch') sus listas de 'seguidos'
    // Esto evita LazyInitializationException al acceder a 'seguidos' fuera de una transacción
    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.seguidos")
    List<Usuario> findAllWithSeguidos();
}