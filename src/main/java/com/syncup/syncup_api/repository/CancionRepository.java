package com.syncup.syncup_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syncup.syncup_api.domain.Cancion;

@Repository
public interface CancionRepository extends JpaRepository<Cancion, Long> {

}