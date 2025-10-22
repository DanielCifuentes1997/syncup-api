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

// Controlador REST para gestionar las operaciones administrativas,
// como la gestión del catálogo de canciones y usuarios.
@RestController
@RequestMapping("/api/admin") // Prefijo base para todas las rutas de admin
public class AdminController {

    @Autowired
    private CancionService cancionService;

    // Endpoint para (Admin) añadir una nueva canción al catálogo.
    // @param songDto El DTO con los datos de la canción a crear.
    // @return Un ResponseEntity con el DTO de la canción creada y un estado 201 (Created).
    @PostMapping("/songs")
    public ResponseEntity<SongDto> addSong(@RequestBody SongCreateDto songDto) {

        // 1. Llama al servicio para crear la canción y guardarla en la BD
        Cancion cancionGuardada = cancionService.crearCancion(songDto);

        // 2. Convierte la entidad guardada a un DTO para la respuesta
        SongDto respuestaDto = new SongDto(cancionGuardada);

        // 3. Devuelve el DTO con el estado HTTP 201 (Created)
        return new ResponseEntity<>(respuestaDto, HttpStatus.CREATED);
    }

    // Endpoint para cargar canciones masivamente desde un archivo CSV
    @PostMapping("/songs/bulk-upload")
    public ResponseEntity<String> bulkUploadSongs(@RequestParam("file") MultipartFile file) {

        // Verificar si el archivo está vacío
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo CSV está vacío.");
        }

        int cancionesImportadas = 0;
        List<String> errores = new ArrayList<>();

        // Leer y procesar el archivo CSV
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            // Configurar el parser de CSV a objetos SongDto
            CsvToBean<SongDto> csvToBean = new CsvToBeanBuilder<SongDto>(reader)
                    .withType(SongDto.class) // Clase destino del mapeo
                    .withIgnoreLeadingWhiteSpace(true) // Ignorar espacios en blanco iniciales
                    .withSeparator(',') // Usar coma como separador
                    // .withSkipLines(1) // Opcional: Descomentar si el CSV tiene cabecera y se desea omitir
                    .build();

            // Parsear todas las filas del CSV a una lista de DTOs
            List<SongDto> songDtos = csvToBean.parse();

            // Intentar guardar cada canción leída del CSV
            for (SongDto dto : songDtos) {
                try {
                    // Mapear DTO de lectura (SongDto) a DTO de creación (SongCreateDto)
                    SongCreateDto createDto = new SongCreateDto();

                    // Validar campos obligatorios leídos del CSV antes de crear
                    if (dto.getTitulo() == null || dto.getArtista() == null || dto.getGenero() == null) {
                        throw new IllegalArgumentException("Título, Artista y Género son obligatorios en el CSV.");
                    }
                    createDto.setTitulo(dto.getTitulo());
                    createDto.setArtista(dto.getArtista());
                    createDto.setGenero(dto.getGenero());
                    createDto.setAnio(dto.getAnio()); // Asume 0 si no presente/inválido en CSV
                    createDto.setDuracion(dto.getDuracion()); // Asume 0 si no presente/inválido en CSV

                    // Llamar al servicio para guardar la canción
                    cancionService.crearCancion(createDto);
                    cancionesImportadas++;

                } catch (Exception e) {
                    // Registrar error si una canción específica del CSV falla al procesar/guardar
                    String titulo = (dto.getTitulo() != null) ? dto.getTitulo() : "FILA DESCONOCIDA";
                    errores.add("Error al importar '" + titulo + "': " + e.getMessage());
                }
            }

        } catch (Exception ex) {
            // Error general al leer o parsear el archivo CSV
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar el archivo CSV: " + ex.getMessage());
        }

        // Construir mensaje de respuesta final
        StringBuilder responseMessage = new StringBuilder();
        responseMessage.append("Importación completada. Canciones importadas: ").append(cancionesImportadas).append(".");
        if (!errores.isEmpty()) {
            responseMessage.append(" Errores encontrados:");
            errores.forEach(error -> responseMessage.append("\n - ").append(error));
        }

        return ResponseEntity.ok(responseMessage.toString());
    }
}