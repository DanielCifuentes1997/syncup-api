package com.syncup.syncup_api.service;

import com.syncup.syncup_api.core.GrafoSocial;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.repository.UsuarioRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Servicio Singleton que gestiona la instancia del GrafoSocial en memoria.
 * Se encarga de inicializar el grafo y mantenerlo sincronizado
 * con las acciones del usuario (ej. seguir a alguien).
 */
@Service
public class GrafoSocialService {

    // Instancia única del grafo
    private final GrafoSocial grafo = new GrafoSocial();

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Método de inicialización.
     * Se ejecuta una vez al arrancar la aplicación.
     * Carga todos los usuarios y sus conexiones desde la BD
     * para construir el grafo en memoria.
     */
    @PostConstruct
    public void init() {
        System.out.println("--- [GrafoSocialService] Poblando el Grafo Social...");

        // 1. Obtener todos los usuarios de la BD
        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();

        // 2. Añadir a cada usuario como un nodo en el grafo
        for (Usuario usuario : todosLosUsuarios) {
            grafo.agregarUsuario(usuario);
        }

        // 3. Añadir las conexiones (aristas)
        // Este segundo bucle es necesario porque todos los nodos
        // deben existir en el grafo ANTES de crear las aristas.
        for (Usuario usuario : todosLosUsuarios) {
            for (Usuario seguido : usuario.getSeguidos()) {
                grafo.agregarConexion(usuario, seguido);
            }
        }

        System.out.println("--- [GrafoSocialService] Grafo Social poblado con " + todosLosUsuarios.size() + " nodos.");
    }

    /**
     * Obtiene sugerencias de "amigos de amigos" para un usuario específico.
     * Llama directamente al algoritmo BFS implementado en el grafo.
     * Cumple con RF-008.
     *
     * @param usuario El usuario para el cual se buscan sugerencias.
     * @return Un Set de Usuarios sugeridos (amigos de nivel 2).
     */
    public Set<Usuario> obtenerSugerencias(Usuario usuario) {
        return grafo.bfsAmigosDeAmigos(usuario);
    }

    /**
     * Añade una nueva conexión al grafo en tiempo real.
     * Se llama cuando un usuario decide seguir a otro.
     * Cumple con RF-007.
     *
     * @param seguidor El usuario que realiza la acción de seguir.
     * @param seguido El usuario que está siendo seguido.
     */
    public void agregarConexion(Usuario seguidor, Usuario seguido) {
        grafo.agregarConexion(seguidor, seguido);
    }

    /**
     * Añade un nuevo usuario (nodo) al grafo en tiempo real.
     * Se llama cuando un nuevo usuario se registra.
     */
    public void agregarUsuario(Usuario usuario) {
        grafo.agregarUsuario(usuario);
    }


}