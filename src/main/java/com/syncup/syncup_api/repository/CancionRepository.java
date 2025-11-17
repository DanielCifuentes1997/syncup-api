package com.syncup.syncup_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syncup.syncup_api.domain.Cancion;
import java.util.List;

@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {

    List<Cancion> findByArtistaIn(List<String> artistas);
    
    List<Cancion> findByGeneroIn(List<String> generos);
}