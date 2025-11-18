package com.syncup.syncup_api.service;

import com.syncup.syncup_api.dto.AdminDashboardMetricsDto;
import com.syncup.syncup_api.dto.ChartDataDto;
import com.syncup.syncup_api.repository.CancionRepository;
import com.syncup.syncup_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MetricsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CancionRepository cancionRepository;

    @Transactional(readOnly = true)
    public AdminDashboardMetricsDto getDashboardMetrics() {
        
        long totalUsuarios = usuarioRepository.countByRol("USER");
        long totalCanciones = cancionRepository.count();
        List<ChartDataDto> generoData = cancionRepository.countByGenero();
        List<ChartDataDto> artistaData = cancionRepository.countTop10ByArtista();

        AdminDashboardMetricsDto metrics = new AdminDashboardMetricsDto();
        metrics.setTotalUsuarios(totalUsuarios);
        metrics.setTotalCanciones(totalCanciones);
        metrics.setGeneroChartData(generoData);
        metrics.setArtistaChartData(artistaData);

        return metrics;
    }
}