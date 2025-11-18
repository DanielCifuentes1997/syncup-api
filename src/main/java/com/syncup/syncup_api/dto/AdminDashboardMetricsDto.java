package com.syncup.syncup_api.dto;

import lombok.Data;
import java.util.List;

@Data
public class AdminDashboardMetricsDto {
    private long totalUsuarios;
    private long totalCanciones;
    private List<ChartDataDto> generoChartData;
    private List<ChartDataDto> artistaChartData;
}