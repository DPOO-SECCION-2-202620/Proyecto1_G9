
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.aplicacion.GestorCompetencias;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaCompetencias {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws ReglaNegocioException;
    }

    // PRUEBA QUE DEBE SER ACEPTADA

    private static void aceptar(String titulo, Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + titulo);
            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + titulo + ": " + e.getMessage()
            );

            fallidas++;
        }
    }

    // PRUEBA QUE DEBE SER RECHAZADA

    private static void rechazar(String titulo, Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + titulo + ": no fue rechazada"
            );

            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + titulo + " -> " + e.getMessage()
            );

            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + titulo + ": " + e
            );

            fallidas++;
        }
    }

    // COMPROBAR RESULTADOS

    private static void verificar(
            String titulo, boolean condicion) {

        if (condicion) {
            System.out.println("[OK] " + titulo);
            correctas++;

        } else {
            System.out.println("[FALLO] " + titulo);
            fallidas++;
        }
    }

    // PROGRAMA PRINCIPAL DE PRUEBAS

    public static void main(String[] args) throws Exception {

        ClubDeportivoSeneca club =
            new ClubDeportivoSeneca();

        GestorCompetencias gestor =
            new GestorCompetencias(club);

        // CREAR DEPORTES

        Deporte futbol = new Deporte("Futbol", 7);

        Deporte baloncesto =
            new Deporte("Baloncesto", 5);

        // CREAR CATEGORIAS

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        Categoria sub17 =
            new Categoria("Sub-17", 15, 17);

        // CREAR EQUIPOS

        Equipo seneca =
            new Equipo("Seneca A", futbol, mayores, 20);

        Equipo basket =
            new Equipo("Basket A", baloncesto, mayores, 20);

        Equipo juveniles =
            new Equipo("Juveniles", futbol, sub17, 20);

        Equipo externo =
            new Equipo("Externo", futbol, mayores, 20);

        // REGISTRAR EQUIPOS

        club.registrarEquipo(seneca);
        club.registrarEquipo(basket);
        club.registrarEquipo(juveniles);

        // CREAR OCHO JUGADORES

        Socio[] jugadores = new Socio[8];

        for (int i = 0; i < jugadores.length; i++) {

            jugadores[i] = new Socio(
                "jugador" + (i + 1),
                "clavePrueba",
                LocalDate.of(2001, 3, 12)
            );

            club.inscribirSocioEnEquipo(
                jugadores[i],
                seneca,
                "Defensa",
                i + 1
            );
        }

        // CREAR COMPETENCIAS

        Competencia cerros = new Competencia(
            "Torneo de los Cerros",
            "Cerros",
            "2026-2",
            futbol,
            mayores,
            "LIGA",
            7
        );

        Competencia ascun = new Competencia(
            "ASCUN",
            "ASCUN",
            "2026-2",
            futbol,
            mayores,
            "GRUPOS_Y_ELIMINACION",
            7
        );

        System.out.println("PRUEBAS DE COMPETENCIAS");
        System.out.println("----------------------------");

        // REGISTRAR COMPETENCIAS

        aceptar("Registrar Torneo de los Cerros", () ->
            gestor.registrarCompetencia(cerros)
        );

        aceptar("Registrar ASCUN", () ->
            gestor.registrarCompetencia(ascun)
        );

        rechazar("Rechazar competencia duplicada", () ->
            gestor.registrarCompetencia(cerros)
        );

        // INSCRIBIR EQUIPO

        InscripcionCompetencia inscripcion =
            gestor.inscribirEquipo(seneca, cerros);

        verificar(
            "Inscribir equipo de futbol",
            inscripcion != null
        );

        // VALIDAR EQUIPOS INCORRECTOS

        rechazar("Rechazar equipo no registrado", () ->
            gestor.inscribirEquipo(externo, cerros)
        );

        rechazar("Rechazar deporte incorrecto", () ->
            gestor.inscribirEquipo(basket, cerros)
        );

        rechazar("Rechazar categoria incorrecta", () ->
            gestor.inscribirEquipo(juveniles, cerros)
        );

        rechazar("Rechazar inscripcion duplicada", () ->
            gestor.inscribirEquipo(seneca, cerros)
        );

        // JUGADOR AJENO AL EQUIPO

        Socio desconocido = new Socio(
            "desconocido",
            "clavePrueba",
            LocalDate.of(2001, 1, 1)
        );

        rechazar("Rechazar jugador ajeno al equipo", () ->
            inscripcion.agregarJugador(desconocido)
        );

        // AGREGAR PRIMER JUGADOR

        aceptar("Agregar primer jugador", () ->
            inscripcion.agregarJugador(jugadores[0])
        );

        // EVITAR JUGADORES DUPLICADOS

        rechazar("Rechazar jugador duplicado", () ->
            inscripcion.agregarJugador(jugadores[0])
        );

        // VALIDAR NOMINA INCOMPLETA

        rechazar("Rechazar nomina incompleta", () ->
            inscripcion.oficializarNomina()
        );

        // COMPLETAR NOMINA

        aceptar("Completar siete jugadores", () -> {

            for (int i = 1; i < 7; i++) {
                inscripcion.agregarJugador(jugadores[i]);
            }
        });

        // OFICIALIZAR NOMINA

        aceptar("Oficializar nomina", () ->
            inscripcion.oficializarNomina()
        );

        // VERIFICAR NOMINA OFICIAL

        verificar(
            "Nomina oficial con siete jugadores",
            inscripcion.isOficial() &&
            inscripcion.getNomina().size() == 7
        );

        // JUGADOR HABILITADO

        verificar(
            "Jugador en nomina habilitado",
            inscripcion.jugadorHabilitado(jugadores[0])
        );

        // JUGADOR NO HABILITADO

        verificar(
            "Jugador fuera de nomina no habilitado",
            !inscripcion.jugadorHabilitado(jugadores[7])
        );

        // NOMINA CERRADA

        rechazar("Rechazar cambios en nomina cerrada", () ->
            inscripcion.agregarJugador(jugadores[7])
        );

        // INSCRIPCION EN OTRA COMPETENCIA

        aceptar("Mismo equipo en otra competencia", () ->
            gestor.inscribirEquipo(seneca, ascun)
        );

        // COMPROBAR TOTALES

        verificar(
            "Dos competencias y dos inscripciones",
            gestor.getCompetencias().size() == 2 &&
            gestor.getInscripciones().size() == 2
        );

        System.out.println("----------------------------");

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Competencias registradas: "
            + gestor.getCompetencias().size()
        );

        System.out.println(
            "Inscripciones registradas: "
            + gestor.getInscripciones().size()
        );

        System.out.println(
            "Jugadores en nomina oficial: "
            + inscripcion.getNomina().size()
        );
    }
}
