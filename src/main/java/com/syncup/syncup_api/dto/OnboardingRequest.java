package com.syncup.syncup_api.dto;

import java.util.List;
import lombok.Data;

@Data
public class OnboardingRequest {
    private List<String> artistas;
    private List<String> generos;
}