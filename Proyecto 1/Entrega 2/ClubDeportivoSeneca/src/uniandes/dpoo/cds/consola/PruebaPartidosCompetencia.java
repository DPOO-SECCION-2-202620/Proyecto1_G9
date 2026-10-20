
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.aplicacion.GestorCompetencias;
import uniandes.dpoo.cds.aplicacion.GestorPartidosCompetencia;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.competencias.Escenario;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.competencias.InstitucionRival;
import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;
import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaPartidosCompetencia {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // PRUEBA EXITOSA

    private static void aceptar(
            String titulo, Accion accion) {

        try {
            accion.ejecutar();
            System.out.println("[OK] " + titulo);
            correctas++;

        } catch (Exception e) {
            System.out.println(
                "[FALLO] " + titulo + " -> " + e.getMessage()
            );
            fallidas++;
        }
    }

    // PRUEBA QUE DEBE SER RECHAZADA

    private static void rechazar(
            String titulo, Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + titulo + " (se permitio)"
            );
            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + titulo + " -> " + e.getMessage()
            );
            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + titulo + " -> " + e
            );
            fallidas++;
        }
    }

    // VERIFICAR CONDICION

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

    // REGISTRAR JUGADORES

    private static Socio[] registrarJugadores(
            ClubDeportivoSeneca club,
            Equipo equipo,
            String prefijo,
            int cantidad)
            throws ReglaNegocioException {

        Socio[] jugadores = new Socio[cantidad];

        for (int i = 0; i < cantidad; i++) {

            jugadores[i] = new Socio(
                prefijo + (i + 1),
                "claveDemo",
                LocalDate.of(2001, 5, 10)
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

    // INSCRIBIR EQUIPO Y CREAR NOMINA

    private static InscripcionCompetencia inscribir(
            GestorCompetencias gestor,
            Equipo equipo,
            Competencia competencia,
            Socio[] jugadores,
            int enNomina)
            throws ReglaNegocioException {

        gestor.registrarCompetencia(competencia);

        InscripcionCompetencia inscripcion =
            gestor.inscribirEquipo(equipo, competencia);

        for (int i = 0; i < enNomina; i++) {
            inscripcion.agregarJugador(jugadores[i]);
        }

        return inscripcion;
    }

    // ALINEAR JUGADORES

    private static void alinear(
            PartidoCompetencia partido,
            Socio[] jugadores,
            int cantidad)
            throws ReglaNegocioException {

        for (int i = 0; i < cantidad; i++) {
            partido.alinearJugador(jugadores[i]);
        }
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        ClubDeportivoSeneca club =
            new ClubDeportivoSeneca();

        GestorCompetencias gestor =
            new GestorCompetencias(club);

        GestorPartidosCompetencia oficiales =
            new GestorPartidosCompetencia(gestor);

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        Deporte futbol = new Deporte("Futbol", 7);
        Deporte basket = new Deporte("Baloncesto", 5);
        Deporte voley = new Deporte("Voleibol", 6);

        Equipo eqF =
            new Equipo("Seneca Futbol", futbol, mayores, 15);

        Equipo eqB =
            new Equipo("Seneca Basket", basket, mayores, 15);

        Equipo eqV =
            new Equipo("Seneca Voley", voley, mayores, 15);

        club.registrarEquipo(eqF);
        club.registrarEquipo(eqB);
        club.registrarEquipo(eqV);

        // CREAR JUGADORES

        Socio[] f = registrarJugadores(
            club, eqF, "F", 8
        );

        Socio[] b = registrarJugadores(
            club, eqB, "B", 5
        );

        Socio[] v = registrarJugadores(
            club, eqV, "V", 6
        );

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
            "ASCUN Basket",
            "ASCUN",
            "2026-2",
            basket,
            mayores,
            "GRUPOS_Y_ELIMINACION",
            5
        );

        Competencia voleyLiga = new Competencia(
            "Liga Voleibol",
            "Universidades",
            "2026-2",
            voley,
            mayores,
            "LIGA",
            6
        );

        // INSCRIBIR EQUIPOS

        InscripcionCompetencia ifut = inscribir(
            gestor, eqF, cerros, f, 7
        );

        InscripcionCompetencia ib = inscribir(
            gestor, eqB, ascun, b, 5
        );

        InscripcionCompetencia iv = inscribir(
            gestor, eqV, voleyLiga, v, 6
        );

        InstitucionRival rival =
            new InstitucionRival("Universidad Rival");

        Escenario sede =
            new Escenario("Cancha central");

        LocalDate fecha =
            LocalDate.of(2026, 11, 5);

        System.out.println(
            "PRUEBAS DE PARTIDOS OFICIALES"
        );

        System.out.println(
            "---------------------------------"
        );

        // VALIDAR NOMINAS

        rechazar("Nomina no oficial", () ->
            oficiales.programarPartido(
                ifut, rival, sede, fecha, "Liga"
            )
        );

        ifut.oficializarNomina();
        ib.oficializarNomina();
        iv.oficializarNomina();

        // PRUEBAS FUTBOL

        final PartidoCompetencia[] pf =
            new PartidoCompetencia[1];

        aceptar("Programar encuentro de futbol", () ->
            pf[0] = oficiales.programarPartido(
                ifut, rival, sede, fecha, "Liga"
            )
        );

        rechazar("Duplicado oficial", () ->
            oficiales.programarPartido(
                ifut, rival, sede, fecha, "Liga"
            )
        );

        rechazar("Inscripcion ajena al gestor", () ->
            oficiales.programarPartido(
                new InscripcionCompetencia(eqF, cerros),
                rival, sede, fecha, "Liga"
            )
        );

        rechazar("Rival obligatorio", () ->
            oficiales.programarPartido(
                ifut, null, sede,
                fecha.plusDays(1), "Liga"
            )
        );

        rechazar("Jugador fuera de nomina", () ->
            pf[0].alinearJugador(f[7])
        );

        rechazar("No resultado sin alineacion", () ->
            pf[0].registrarResultado(1, 0)
        );

        aceptar("Alinear seis futbolistas", () ->
            alinear(pf[0], f, 6)
        );

        rechazar("Alineacion insuficiente", () ->
            pf[0].confirmarAlineacion()
        );

        aceptar("Alinear septimo futbolista", () ->
            pf[0].alinearJugador(f[6])
        );

        rechazar("No repetir jugador", () ->
            pf[0].alinearJugador(f[6])
        );

        aceptar("Confirmar alineacion completa", () ->
            pf[0].confirmarAlineacion()
        );

        aceptar("Empate permitido en futbol", () ->
            pf[0].registrarResultado(2, 2)
        );

        verificar(
            "Un punto por empate de liga",
            pf[0].getPuntosLigaFutbol() == 1
        );

        rechazar("No duplicar resultado", () ->
            pf[0].registrarResultado(3, 0)
        );

        // PRUEBAS BALONCESTO

        final PartidoCompetencia[] pb =
            new PartidoCompetencia[1];

        aceptar("Programar basket ASCUN", () ->
            pb[0] = oficiales.programarPartido(
                ib, rival, sede,
                fecha.plusDays(1), "Grupos"
            )
        );

        aceptar("Alinear basket y confirmar", () -> {
            alinear(pb[0], b, 5);
            pb[0].confirmarAlineacion();
        });

        rechazar("No empates en baloncesto", () ->
            pb[0].registrarResultado(60, 60)
        );

        aceptar("Registrar resultado de baloncesto", () ->
            pb[0].registrarResultado(65, 61)
        );

        // PRUEBAS VOLEIBOL

        final PartidoCompetencia[] pv =
            new PartidoCompetencia[1];

        aceptar("Programar partido de voleibol", () ->
            pv[0] = oficiales.programarPartido(
                iv, rival, sede,
                fecha.plusDays(2), "Liga"
            )
        );

        aceptar("Alinear voleibol y confirmar", () -> {
            alinear(pv[0], v, 6);
            pv[0].confirmarAlineacion();
        });

        rechazar("No empates en sets de voleibol", () ->
            pv[0].registrarResultado(2, 2)
        );

        aceptar("Registrar sets 3 a 2", () ->
            pv[0].registrarResultado(3, 2)
        );

        verificar(
            "Tres partidos oficiales registrados",
            oficiales.getPartidos().size() == 3
        );

        // RESULTADOS

        System.out.println(
            "---------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Partidos registrados: "
            + oficiales.getPartidos().size()
        );
    }
}
