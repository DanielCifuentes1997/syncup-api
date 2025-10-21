package com.syncup.syncup_api.core;

import com.syncup.syncup_api.domain.Cancion;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Representa una arista (conexión) ponderada en el GrafoDeSimilitud.
 * Almacena el nodo de destino (la canción) y el peso (la similitud).
 */
@AllArgsConstructor // Crea un constructor que acepta todos los campos
@Getter // Usa @Getter en lugar de @Data para evitar problemas con hashCode/equals
public class AristaPonderada {

    private final Cancion destino;
    private final double peso;

}