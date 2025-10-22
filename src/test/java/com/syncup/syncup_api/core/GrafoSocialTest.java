package com.syncup.syncup_api.core;

import com.syncup.syncup_api.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba Unitaria para la clase GrafoSocial.
 * Verifica el algoritmo BFS para "amigos de amigos".
 */
class GrafoSocialTest {

    private GrafoSocial grafo;

    // Crearemos algunos usuarios de prueba
    // Usamos Lombok @Data en Usuario, por lo que hashCode/equals se basan en 'username'
    // Para que funcionen en el HashMap del grafo, necesitamos setear el username.
    private Usuario daniel = new Usuario();
    private Usuario sharon = new Usuario();
    private Usuario ana = new Usuario();
    private Usuario luis = new Usuario();
    private Usuario maria = new Usuario();
    private Usuario aislado = new Usuario();

    /**
     * Este método se ejecuta antes de cada prueba.
     * Construye nuestro mini-mundo social.
     *
     * Estructura del Grafo:
     *
     * (Daniel) -- (Sharon) -- (Ana)
     * |
     * (Luis) -- (Maria)
     *
     * (Aislado)
     *
     */
    @BeforeEach
    void setUp() {
        // Inicializamos los usuarios
        daniel.setUsername("daniel");
        sharon.setUsername("sharon");
        ana.setUsername("ana");
        luis.setUsername("luis");
        maria.setUsername("maria");
        aislado.setUsername("aislado");

        // Creamos un grafo limpio
        grafo = new GrafoSocial();

        // Añadimos las conexiones
        grafo.agregarConexion(daniel, sharon); // Nivel 1 de Daniel
        grafo.agregarConexion(daniel, luis);   // Nivel 1 de Daniel

        grafo.agregarConexion(sharon, ana);    // Nivel 2 de Daniel
        grafo.agregarConexion(luis, maria);    // Nivel 2 de Daniel

        // Añadimos un usuario que no tiene conexiones
        grafo.agregarUsuario(aislado);
    }

    //Prueba principal: Encontrar amigos de amigos (Nivel 2).
    @Test
    void testBfsAmigosDeAmigos() {
        // Buscamos las sugerencias para "daniel"
        // Amigos de Daniel (Nivel 1): sharon, luis
        // Amigos de Amigos (Nivel 2): ana, maria
        Set<Usuario> sugerencias = grafo.bfsAmigosDeAmigos(daniel);

        // Verificamos que encontró 2 sugerencias
        assertEquals(2, sugerencias.size());

        // Verificamos que son las personas correctas
        assertTrue(sugerencias.contains(ana));
        assertTrue(sugerencias.contains(maria));

        // Verificamos que NO sugirió a amigos directos (Nivel 1)
        assertFalse(sugerencias.contains(sharon));
        assertFalse(sugerencias.contains(luis));

        // Verificamos que NO sugirió a sí mismo (Nivel 0)
        assertFalse(sugerencias.contains(daniel));
    }

    /**
     * Prueba para un usuario sin conexiones.
     */
    @Test
    void testBfsUsuarioAislado() {
        // Buscamos sugerencias para "aislado"
        Set<Usuario> sugerencias = grafo.bfsAmigosDeAmigos(aislado);

        // No debería encontrar ninguna sugerencia
        assertEquals(0, sugerencias.size());
    }

    /**
     * Prueba para un usuario en el borde del grafo.
     */
    @Test
    void testBfsUsuarioEnElBorde() {
        // Buscamos sugerencias para "ana"
        // Amigos de Ana (Nivel 1): sharon
        // Amigos de Amigos (Nivel 2): daniel
        Set<Usuario> sugerencias = grafo.bfsAmigosDeAmigos(ana);

        // Debería encontrar 1 sugerencia
        assertEquals(1, sugerencias.size());
        assertTrue(sugerencias.contains(daniel));
    }
}