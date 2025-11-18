package com.syncup.syncup_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.ChartDataDto;

import java.util.List;

@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {

    List<Cancion> findByArtistaIn(List<String> artistas);
    
    List<Cancion> findByGeneroIn(List<String> generos);

    @Query("SELECT new com.syncup.syncup_api.dto.ChartDataDto(c.genero, COUNT(c)) FROM Cancion c WHERE c.genero IS NOT NULL GROUP BY c.genero ORDER BY COUNT(c) DESC")
    List<ChartDataDto> countByGenero();

    @Query("SELECT new com.syncup.syncup_api.dto.ChartDataDto(c.artista, COUNT(c)) FROM Cancion c WHERE c.artista IS NOT NULL GROUP BY c.artista ORDER BY COUNT(c) DESC LIMIT 10")
    List<ChartDataDto> countTop10ByArtista();
}