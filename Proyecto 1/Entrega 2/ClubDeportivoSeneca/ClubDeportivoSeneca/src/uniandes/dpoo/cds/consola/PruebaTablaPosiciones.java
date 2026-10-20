
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.util.List;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.aplicacion.GestorCompetencias;
import uniandes.dpoo.cds.aplicacion.GestorPartidosCompetencia;
import uniandes.dpoo.cds.aplicacion.TablaPosicionesLiga;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.competencias.Escenario;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.competencias.InstitucionRival;
import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;
import uniandes.dpoo.cds.modelo.competencias.PosicionEquipo;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaTablaPosiciones {

    private static int correctas = 0;
    private static int fallidas = 0;

    // VERIFICAR RESULTADOS

    private static void verificar(
            String nombre, boolean resultado) {

        System.out.println(
            (resultado ? "[OK] " : "[FALLO] ") + nombre
        );

        if (resultado) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // COMPROBAR RECHAZOS

    private static void rechazar(
            String nombre, Accion accion) {

        try {
            accion.ejecutar();
            verificar(nombre, false);

        } catch (ReglaNegocioException e) {

            System.out.println(
                "     Motivo: " + e.getMessage()
            );

            verificar(nombre, true);

        } catch (Exception e) {

            System.out.println(
                "     Error inesperado: " + e
            );

            verificar(nombre, false);
        }
    }

    // INSCRIBIR JUGADORES

    private static Socio[] inscribirJugadores(
            ClubDeportivoSeneca club,
            Equipo equipo,
            String prefijo)
            throws ReglaNegocioException {

        Socio[] jugadores = new Socio[7];

        for (int i = 0; i < 7; i++) {

            jugadores[i] = new Socio(
                prefijo + i,
                "claveDemo",
                LocalDate.of(2000, 1, 1)
            );

            club.inscribirSocioEnEquipo(
                jugadores[i],
                equipo,
                "Jugador",
                i + 1
            );
        }

        return jugadores;
    }

    // INSCRIBIR EQUIPO EN TORNEO

    private static InscripcionCompetencia inscribirEnTorneo(
            GestorCompetencias gestor,
            Equipo equipo,
            Competencia competencia,
            Socio[] jugadores)
            throws ReglaNegocioException {

        InscripcionCompetencia inscrita =
            gestor.inscribirEquipo(equipo, competencia);

        for (Socio socio : jugadores) {
            inscrita.agregarJugador(socio);
        }

        inscrita.oficializarNomina();

        return inscrita;
    }

    // CREAR PARTIDO OFICIAL

    private static PartidoCompetencia partido(
            GestorPartidosCompetencia gestor,
            InscripcionCompetencia inscrita,
            Socio[] jugadores,
            String rival,
            int dia)
            throws ReglaNegocioException {

        PartidoCompetencia partido =
            gestor.programarPartido(
                inscrita,
                new InstitucionRival(rival),
                new Escenario("Cancha central"),
                LocalDate.of(2026, 11, dia),
                "Liga"
            );

        for (Socio socio : jugadores) {
            partido.alinearJugador(socio);
        }

        partido.confirmarAlineacion();

        return partido;
    }

    // BUSCAR EQUIPO EN TABLA

    private static PosicionEquipo buscar(
            List<PosicionEquipo> tabla,
            Equipo equipo) {

        for (PosicionEquipo fila : tabla) {

            if (fila.getEquipo() == equipo) {
                return fila;
            }
        }

        throw new IllegalArgumentException(
            "Equipo no encontrado"
        );
    }

    // PRUEBAS PRINCIPALES

    public static void main(String[] args)
            throws Exception {

        ClubDeportivoSeneca club =
            new ClubDeportivoSeneca();

        GestorCompetencias gestor =
            new GestorCompetencias(club);

        GestorPartidosCompetencia oficiales =
            new GestorPartidosCompetencia(gestor);

        TablaPosicionesLiga calculadora =
            new TablaPosicionesLiga(gestor, oficiales);

        // CREAR DEPORTES Y CATEGORIA

        Deporte futbol = new Deporte("Futbol", 7);
        Deporte baloncesto =
            new Deporte("Baloncesto", 5);

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        // CREAR EQUIPOS

        Equipo a =
            new Equipo("Seneca A", futbol, mayores, 15);

        Equipo b =
            new Equipo("Seneca B", futbol, mayores, 15);

        Equipo c =
            new Equipo("Seneca C", futbol, mayores, 15);

        club.registrarEquipo(a);
        club.registrarEquipo(b);
        club.registrarEquipo(c);

        // INSCRIBIR JUGADORES

        Socio[] jA = inscribirJugadores(club, a, "A");
        Socio[] jB = inscribirJugadores(club, b, "B");
        Socio[] jC = inscribirJugadores(club, c, "C");

        // CREAR COMPETENCIAS

        Competencia cerros = new Competencia(
            "Cerros", "Cerros", "2026-2",
            futbol, mayores, "LIGA", 7
        );

        Competencia ascun = new Competencia(
            "ASCUN", "ASCUN", "2026-2",
            futbol, mayores,
            "GRUPOS_Y_ELIMINACION", 7
        );

        Competencia basketLiga = new Competencia(
            "Basketliga", "LIGA", "2026-2",
            baloncesto, mayores, "LIGA", 5
        );

        gestor.registrarCompetencia(cerros);
        gestor.registrarCompetencia(ascun);
        gestor.registrarCompetencia(basketLiga);

        // INSCRIBIR EQUIPOS EN CERROS

        InscripcionCompetencia iA =
            inscribirEnTorneo(
                gestor, a, cerros, jA
            );

        InscripcionCompetencia iB =
            inscribirEnTorneo(
                gestor, b, cerros, jB
            );

        inscribirEnTorneo(
            gestor, c, cerros, jC
        );

        System.out.println(
            "PRUEBAS DE TABLA DE POSICIONES"
        );

        System.out.println(
            "----------------------------------------"
        );

        // VALIDACIONES INICIALES

        rechazar(
            "Competencia nula rechazada",
            () -> calculadora.calcular(null)
        );

        rechazar(
            "Competencia no registrada rechazada",
            () -> calculadora.calcular(
                new Competencia(
                    "Otra", "Otra", "2026-2",
                    futbol, mayores, "LIGA", 7
                )
            )
        );

        rechazar(
            "Formato de ASCUN no es liga simple",
            () -> calculadora.calcular(ascun)
        );

        rechazar(
            "Liga de otro deporte no soportada aun",
            () -> calculadora.calcular(basketLiga)
        );

        // TABLA SIN RESULTADOS

        List<PosicionEquipo> inicial =
            calculadora.calcular(cerros);

        verificar(
            "Tres equipos en la tabla",
            inicial.size() == 3
        );

        verificar(
            "Tabla inicial sin partidos",
            buscar(inicial, a).getPuntos() == 0 &&
            buscar(inicial, b).getJugados() == 0 &&
            buscar(inicial, c).getJugados() == 0
        );

        verificar(
            "Orden alfabetico en empate total",
            inicial.get(0).getEquipo() == a &&
            inicial.get(1).getEquipo() == b &&
            inicial.get(2).getEquipo() == c
        );

        // PRIMER PARTIDO DEL EQUIPO A

        PartidoCompetencia a1 = partido(
            oficiales, iA, jA, "Universidad 1", 1
        );

        verificar(
            "Partido programado no suma puntos",
            buscar(
                calculadora.calcular(cerros), a
            ).getJugados() == 0
        );

        a1.registrarResultado(3, 1);

        verificar(
            "Victoria A: 3 puntos y diferencia +2",
            buscar(
                calculadora.calcular(cerros), a
            ).getPuntos() == 3 &&

            buscar(
                calculadora.calcular(cerros), a
            ).getDiferenciaGoles() == 2
        );

        // PRIMER PARTIDO DEL EQUIPO B

        PartidoCompetencia b1 = partido(
            oficiales, iB, jB, "Universidad 1", 1
        );

        b1.registrarResultado(2, 0);

        verificar(
            "Victoria B: 3 puntos y diferencia +2",
            buscar(
                calculadora.calcular(cerros), b
            ).getPuntos() == 3 &&

            buscar(
                calculadora.calcular(cerros), b
            ).getDiferenciaGoles() == 2
        );

        verificar(
            "Desempate por goles a favor",
            calculadora.calcular(cerros)
                .get(0).getEquipo() == a
        );

        // EMPATES

        PartidoCompetencia a2 = partido(
            oficiales, iA, jA, "Universidad 2", 2
        );

        a2.registrarResultado(1, 1);

        PartidoCompetencia b2 = partido(
            oficiales, iB, jB, "Universidad 2", 2
        );

        b2.registrarResultado(0, 0);

        verificar(
            "Empate suma un punto",
            buscar(
                calculadora.calcular(cerros), a
            ).getPuntos() == 4 &&

            buscar(
                calculadora.calcular(cerros), b
            ).getPuntos() == 4
        );

        verificar(
            "Partidos y goles acumulados",
            buscar(
                calculadora.calcular(cerros), a
            ).getJugados() == 2 &&

            buscar(
                calculadora.calcular(cerros), a
            ).getGolesFavor() == 4 &&

            buscar(
                calculadora.calcular(cerros), a
            ).getGolesContra() == 2
        );

        // DERROTAS

        PartidoCompetencia a3 = partido(
            oficiales, iA, jA, "Universidad 3", 3
        );

        a3.registrarResultado(0, 2);

        PartidoCompetencia b3 = partido(
            oficiales, iB, jB, "Universidad 3", 3
        );

        b3.registrarResultado(0, 2);

        List<PosicionEquipo> trasTres =
            calculadora.calcular(cerros);

        verificar(
            "Derrota no suma puntos",
            buscar(trasTres, a).getPuntos() == 4 &&
            buscar(trasTres, a).getPerdidos() == 1
        );

        verificar(
            "A delante de B con mismos puntos y diferencia",
            trasTres.get(0).getEquipo() == a &&
            trasTres.get(1).getEquipo() == b
        );

        verificar(
            "Equipo sin encuentros se mantiene en cero",
            buscar(trasTres, c).getPuntos() == 0 &&
            buscar(trasTres, c).getJugados() == 0
        );

        // NUEVA VICTORIA DEL EQUIPO B

        PartidoCompetencia b4 = partido(
            oficiales, iB, jB, "Universidad 4", 4
        );

        b4.registrarResultado(1, 0);

        List<PosicionEquipo> finalTabla =
            calculadora.calcular(cerros);

        verificar(
            "Nueva victoria coloca B primero",
            finalTabla.get(0).getEquipo() == b &&
            finalTabla.get(0).getPuntos() == 7
        );

        verificar(
            "Resumen de A: 1 victoria, 1 empate, 1 derrota",
            buscar(finalTabla, a).getGanados() == 1 &&
            buscar(finalTabla, a).getEmpatados() == 1 &&
            buscar(finalTabla, a).getPerdidos() == 1
        );

        // COMPROBAR PROTECCION DE LA TABLA

        try {
            finalTabla.clear();

            verificar(
                "Tabla protegida de modificaciones externas",
                false
            );

        } catch (UnsupportedOperationException e) {

            verificar(
                "Tabla protegida de modificaciones externas",
                true
            );
        }

        // MOSTRAR TABLA

        System.out.println(
            "----------------------------------------"
        );

        System.out.println(
            "EQUIPO       PJ  G  E  P GF GC DG PTS"
        );

        for (PosicionEquipo p :
                calculadora.calcular(cerros)) {

            System.out.printf(
                "%-12s %2d %2d %2d %2d %2d %2d %+3d %3d%n",
                p.getEquipo().getNombre(),
                p.getJugados(),
                p.getGanados(),
                p.getEmpatados(),
                p.getPerdidos(),
                p.getGolesFavor(),
                p.getGolesContra(),
                p.getDiferenciaGoles(),
                p.getPuntos()
            );
        }

        System.out.println(
            "----------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );
    }
}
