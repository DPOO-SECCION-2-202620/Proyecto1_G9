
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.aplicacion.GestorEstadisticas;
import uniandes.dpoo.cds.aplicacion.GestorPartidosInternos;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaBaloncesto;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaFutbol;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaVoleibol;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaEstadisticas {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws ReglaNegocioException;
    }

    private static void aceptar(String nombre, Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + nombre);
            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + nombre + ": " + e.getMessage()
            );
            fallidas++;
        }
    }

    private static void rechazar(String nombre, Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + nombre + ": fue aceptado"
            );
            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + nombre + " -> " + e.getMessage()
            );
            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + nombre + ": " + e.getMessage()
            );
            fallidas++;
        }
    }

    private static void verificar(
            String nombre, boolean condicion) {

        if (condicion) {
            System.out.println("[OK] " + nombre);
            correctas++;
        } else {
            System.out.println("[FALLO] " + nombre);
            fallidas++;
        }
    }

    private static Socio[] llenar(
            ClubDeportivoSeneca club,
            Equipo equipo,
            String prefijo,
            int cantidad)
            throws ReglaNegocioException {

        Socio[] jugadores = new Socio[cantidad];

        for (int i = 0; i < cantidad; i++) {

            jugadores[i] = new Socio(
                prefijo + i,
                "claveDemo",
                LocalDate.of(2000, 1, 1)
            );

            club.inscribirSocioEnEquipo(
                jugadores[i], equipo, "Jugador", i + 1
            );
        }

        return jugadores;
    }

    public static void main(String[] args) throws Exception {

        ClubDeportivoSeneca club =
            new ClubDeportivoSeneca();

        GestorPartidosInternos partidos =
            new GestorPartidosInternos(club);

        GestorEstadisticas estadisticas =
            new GestorEstadisticas();

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        Deporte futbol = new Deporte("Futbol", 7);
        Deporte basket = new Deporte("Baloncesto", 5);
        Deporte volley = new Deporte("Voleibol", 6);

        Equipo local =
            new Equipo("Futbol A", futbol, mayores, 15);

        Equipo visitante =
            new Equipo("Futbol B", futbol, mayores, 15);

        Equipo basketA =
            new Equipo("Basket A", basket, mayores, 15);

        Equipo basketB =
            new Equipo("Basket B", basket, mayores, 15);

        Equipo volleyA =
            new Equipo("Voley A", volley, mayores, 15);

        Equipo volleyB =
            new Equipo("Voley B", volley, mayores, 15);

        // REGISTRAR EQUIPOS

        for (Equipo equipo : new Equipo[] {
                local, visitante,
                basketA, basketB,
                volleyA, volleyB}) {

            club.registrarEquipo(equipo);
        }

        // INSCRIBIR JUGADORES

        Socio[] jugadoresLocal =
            llenar(club, local, "F", 8);

        llenar(club, visitante, "R", 7);

        Socio[] jugadoresBasket =
            llenar(club, basketA, "BA", 5);

        llenar(club, basketB, "BB", 5);

        Socio[] jugadoresVoley =
            llenar(club, volleyA, "VA", 6);

        llenar(club, volleyB, "VB", 6);

        // PROGRAMAR PARTIDOS

        PartidoInterno p1 = partidos.programarPartido(
            LocalDate.of(2026, 11, 1),
            local, visitante
        );

        PartidoInterno p2 = partidos.programarPartido(
            LocalDate.of(2026, 11, 8),
            local, visitante
        );

        PartidoInterno p3 = partidos.programarPartido(
            LocalDate.of(2026, 11, 15),
            local, visitante
        );

        PartidoInterno pb = partidos.programarPartido(
            LocalDate.of(2026, 11, 2),
            basketA, basketB
        );

        PartidoInterno pv = partidos.programarPartido(
            LocalDate.of(2026, 11, 3),
            volleyA, volleyB
        );

        System.out.println(
            "PRUEBAS DE ESTADISTICAS Y SANCIONES"
        );
        System.out.println(
            "------------------------------------"
        );

        // PRIMER PARTIDO: SANCION POR DOS AMARILLAS

        aceptar("Iniciar primer partido de futbol", () ->
            estadisticas.iniciarPartido(p1)
        );

        aceptar("Estadistica de futbol y dos amarillas", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    p1, local, jugadoresLocal[0],
                    2, 1, 2, 0
                )
            )
        );

        verificar(
            "Suspension pendiente para siguiente partido",
            estadisticas.suspendidoEnProximoPartido(
                local, jugadoresLocal[0].getLogin()
            )
        );

        rechazar("Estadistica duplicada rechazada", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    p1, local, jugadoresLocal[0],
                    1, 0, 0, 0
                )
            )
        );

        rechazar("No se permiten estadisticas negativas", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    p1, local, jugadoresLocal[1],
                    -1, 0, 0, 0
                )
            )
        );

        p1.registrarResultado(2, 1);

        rechazar("No se registran estadisticas tras finalizar", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    p1, local, jugadoresLocal[1],
                    1, 0, 0, 0
                )
            )
        );

        // SEGUNDO PARTIDO: CUMPLIR SANCION

        aceptar("Iniciar segundo partido con 7 habilitados", () ->
            estadisticas.iniciarPartido(p2)
        );

        rechazar("Suspendido no participa en siguiente partido", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    p2, local, jugadoresLocal[0],
                    0, 0, 0, 0
                )
            )
        );

        aceptar("Jugador habilitado puede participar", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    p2, local, jugadoresLocal[1],
                    0, 1, 0, 0
                )
            )
        );

        p2.registrarResultado(1, 0);

        // TERCER PARTIDO: REGRESO DEL JUGADOR

        aceptar("Iniciar tercer partido tras cumplir sancion", () ->
            estadisticas.iniciarPartido(p3)
        );

        aceptar("Jugador reincorporado puede participar", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    p3, local, jugadoresLocal[0],
                    1, 0, 0, 0
                )
            )
        );

        // ESTADISTICAS DE BALONCESTO

        aceptar("Iniciar partido de baloncesto", () ->
            estadisticas.iniciarPartido(pb)
        );

        aceptar("Estadistica de baloncesto", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaBaloncesto(
                    pb, basketA, jugadoresBasket[0],
                    18, 7
                )
            )
        );

        rechazar(
            "No se permiten goles de futbol en baloncesto", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaFutbol(
                    pb, basketA, jugadoresBasket[1],
                    1, 0, 0, 0
                )
            )
        );

        // ESTADISTICAS DE VOLEIBOL

        aceptar("Iniciar partido de voleibol", () ->
            estadisticas.iniciarPartido(pv)
        );

        aceptar("Estadistica de voleibol", () ->
            estadisticas.registrarEstadistica(
                new EstadisticaVoleibol(
                    pv, volleyA, jugadoresVoley[0],
                    3, 4
                )
            )
        );

        verificar(
            "Estadisticas del primer partido: 1",
            estadisticas.getEstadisticas(p1).size() == 1
        );

        System.out.println(
            "------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Estadisticas primer partido: "
            + estadisticas.getEstadisticas(p1).size()
        );
    }
}
