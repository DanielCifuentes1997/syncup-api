package com.syncup.syncup_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syncup.syncup_api.domain.Usuario;

import java.util.Optional;

@Repository // Le dice a Spring que esto es un Repositorio
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Spring creará automáticamente una consulta que busca un usuario por su 'username'
    Optional<Usuario> findByUsername(String username);
}