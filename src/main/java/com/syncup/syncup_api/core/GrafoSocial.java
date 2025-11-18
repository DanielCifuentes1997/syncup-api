package com.syncup.syncup_api.core;

import com.syncup.syncup_api.domain.Usuario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class GrafoSocial {

    private final Map<Usuario, List<Usuario>> listaDeAdyacencia;

    public GrafoSocial() {
        this.listaDeAdyacencia = new HashMap<>();
    }

    public void agregarUsuario(Usuario usuario) {
        listaDeAdyacencia.computeIfAbsent(usuario, k -> new ArrayList<>());
    }

    public void agregarConexion(Usuario usuario1, Usuario usuario2) {
        agregarUsuario(usuario1);
        agregarUsuario(usuario2);

        if (!listaDeAdyacencia.get(usuario1).contains(usuario2)) {
            listaDeAdyacencia.get(usuario1).add(usuario2);
        }
        if (!listaDeAdyacencia.get(usuario2).contains(usuario1)) {
            listaDeAdyacencia.get(usuario2).add(usuario1);
        }
    }

    public void eliminarUsuario(Usuario usuario) {
        if (!listaDeAdyacencia.containsKey(usuario)) {
            return;
        }

        List<Usuario> vecinos = new ArrayList<>(listaDeAdyacencia.get(usuario));
        for (Usuario vecino : vecinos) {
            eliminarConexion(usuario, vecino);
        }

        listaDeAdyacencia.remove(usuario);
    }

    public void eliminarConexion(Usuario usuario1, Usuario usuario2) {
        if (listaDeAdyacencia.containsKey(usuario1)) {
            listaDeAdyacencia.get(usuario1).remove(usuario2);
        }
        if (listaDeAdyacencia.containsKey(usuario2)) {
            listaDeAdyacencia.get(usuario2).remove(usuario1);
        }
    }

    public Set<Usuario> bfsAmigosDeAmigos(Usuario usuarioInicio) {
        Set<Usuario> amigosDeAmigos = new HashSet<>();
        Queue<Usuario> cola = new LinkedList<>();
        Set<Usuario> visitados = new HashSet<>();
        Map<Usuario, Integer> distancia = new HashMap<>();

        cola.add(usuarioInicio);
        visitados.add(usuarioInicio);
        distancia.put(usuarioInicio, 0);

        while (!cola.isEmpty()) {
            Usuario actual = cola.poll();
            int distActual = distancia.get(actual);

            for (Usuario vecino : listaDeAdyacencia.getOrDefault(actual, new ArrayList<>())) {

                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    distancia.put(vecino, distActual + 1);
                    cola.add(vecino);

                    if (distActual + 1 == 2) {
                        amigosDeAmigos.add(vecino);
                    }
                }
            }
        }

        amigosDeAmigos.removeAll(listaDeAdyacencia.get(usuarioInicio));

        return amigosDeAmigos;
    }
}