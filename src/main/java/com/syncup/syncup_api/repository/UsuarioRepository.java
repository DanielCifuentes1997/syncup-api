package com.syncup.syncup_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.syncup.syncup_api.domain.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.seguidos")
    List<Usuario> findAllWithSeguidos();

    long countByRol(String rol);
}