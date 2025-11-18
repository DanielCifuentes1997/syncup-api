package com.syncup.syncup_api.service;

import com.syncup.syncup_api.core.GrafoSocial;
import com.syncup.syncup_api.domain.Usuario;
import com.syncup.syncup_api.repository.UsuarioRepository;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class GrafoSocialService {

    private final GrafoSocial grafo = new GrafoSocial();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostConstruct
    @Transactional(readOnly = true)
    public void init() {
        System.out.println("--- [GrafoSocialService] Poblando el Grafo Social...");

        List<Usuario> todosLosUsuarios = usuarioRepository.findAllWithSeguidos();

        if (todosLosUsuarios.isEmpty()) {
            System.out.println("--- [GrafoSocialService] No hay usuarios en la BD para construir el grafo.");
            return;
        }

        for (Usuario usuario : todosLosUsuarios) {
            grafo.agregarUsuario(usuario);
        }

        for (Usuario usuario : todosLosUsuarios) {
            for (Usuario seguido : usuario.getSeguidos()) {
                grafo.agregarConexion(usuario, seguido);
            }
        }

        System.out.println("--- [GrafoSocialService] Grafo Social poblado con " + todosLosUsuarios.size() + " nodos.");
    }

    public Set<Usuario> obtenerSugerencias(Usuario usuario) {
        return grafo.bfsAmigosDeAmigos(usuario);
    }

    public void agregarConexion(Usuario seguidor, Usuario seguido) {
        grafo.agregarConexion(seguidor, seguido);
    }

    public void agregarUsuario(Usuario usuario) {
        grafo.agregarUsuario(usuario);
    }
    
    public void eliminarUsuario(Usuario usuario) {
        grafo.eliminarUsuario(usuario);
    }

    public void eliminarConexion(Usuario usuario1, Usuario usuario2) {
        grafo.eliminarConexion(usuario1, usuario2);
    }
}