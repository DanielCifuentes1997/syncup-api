package com.syncup.syncup_api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/genres")
public class GenreController {

    @Value("${syncup.genres.master-list}")
    private String genresString;

    @GetMapping("/master")
    public ResponseEntity<List<String>> getMasterGenres() {
        List<String> genreList = Arrays.asList(genresString.split(","));
        return ResponseEntity.ok(genreList);
    }
}