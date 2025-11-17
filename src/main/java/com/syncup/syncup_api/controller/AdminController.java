package com.syncup.syncup_api.controller;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.syncup.syncup_api.domain.Cancion;
import com.syncup.syncup_api.dto.SongCreateDto;
import com.syncup.syncup_api.dto.SongDto;
import com.syncup.syncup_api.service.CancionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; 
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader; 
import java.io.InputStreamReader;
import java.io.Reader; 
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin") 
public class AdminController {

    @Autowired
    private CancionService cancionService;

    @PostMapping("/songs")
    public ResponseEntity<SongDto> addSong(@RequestBody SongCreateDto songDto) {
        Cancion cancionGuardada = cancionService.crearCancion(songDto);
        SongDto respuestaDto = new SongDto(cancionGuardada);
        return new ResponseEntity<>(respuestaDto, HttpStatus.CREATED);
    }

    @PostMapping("/songs/bulk-upload")
    public ResponseEntity<String> bulkUploadSongs(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo CSV está vacío.");
        }

        int cancionesImportadas = 0;
        List<String> errores = new ArrayList<>();

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            CsvToBean<SongDto> csvToBean = new CsvToBeanBuilder<SongDto>(reader)
                    .withType(SongDto.class) 
                    .withIgnoreLeadingWhiteSpace(true) 
                    .withSeparator(',') 
                    .build();

            List<SongDto> songDtos = csvToBean.parse();

            for (SongDto dto : songDtos) {
                try {
                    SongCreateDto createDto = new SongCreateDto();

                    if (dto.getTitulo() == null || dto.getArtista() == null || dto.getGenero() == null || dto.getFilename() == null) {
                        throw new IllegalArgumentException("Título, Artista, Género y Filename son obligatorios en el CSV.");
                    }
                    createDto.setTitulo(dto.getTitulo());
                    createDto.setArtista(dto.getArtista());
                    createDto.setGenero(dto.getGenero());
                    createDto.setAnio(dto.getAnio()); 
                    createDto.setDuracion(dto.getDuracion()); 
                    createDto.setFilename(dto.getFilename());

                    cancionService.crearCancion(createDto);
                    cancionesImportadas++;

                } catch (Exception e) {
                    String titulo = (dto.getTitulo() != null) ? dto.getTitulo() : "FILA DESCONOCIDA";
                    errores.add("Error al importar '" + titulo + "': " + e.getMessage());
                }
            }

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo CSV: " + ex.getMessage());
        }

        StringBuilder responseMessage = new StringBuilder();
        responseMessage.append("Importación completada. Canciones importadas: ").append(cancionesImportadas).append(".");
        if (!errores.isEmpty()) {
            responseMessage.append(" Errores encontrados:");
            errores.forEach(error -> responseMessage.append("\n - ").append(error));
        }

        return ResponseEntity.ok(responseMessage.toString());
    }
}