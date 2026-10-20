
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.SistemaClub;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Articulo;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaFutbol;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class PruebaPersistenciaEstadisticas {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // COMPROBAR RESULTADOS

    private static void verificar(
            String nombre,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ") + nombre
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // COMPROBAR RECHAZOS

    private static void rechazar(
            String nombre,
            Accion accion) {

        try {

            accion.ejecutar();

            verificar(nombre, false);

        } catch (ReglaNegocioException esperado) {

            verificar(nombre, true);

        } catch (Exception otro) {

            System.out.println(
                "Error inesperado: " + otro
            );

            verificar(nombre, false);
        }
    }

    // CREAR SISTEMA NUEVO

    private static SistemaClub nuevo() {

        return new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    // RECUPERAR SISTEMA

    private static SistemaClub cargar(Path ruta)
            throws Exception {

        return PersistenciaSistema.cargarSistema(
            ruta,
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    // INSCRIBIR JUGADORES

    private static Socio[] inscribir(
            SistemaClub sistema,
            Equipo equipo,
            String prefijo,
            int cantidad)
            throws Exception {

        Socio[] jugadores = new Socio[cantidad];

        for (int i = 0; i < cantidad; i++) {

            jugadores[i] = new Socio(
                prefijo + i,
                "demo",
                LocalDate.of(2000, 1, 1)
            );

            sistema.getClub()
                .inscribirSocioEnEquipo(
                    jugadores[i],
                    equipo,
                    "Jugador",
                    i + 1
                );
        }

        return jugadores;
    }

    // PRUEBA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        Path ruta = Path.of(
            "datos",
            "sistema-estadisticas-prueba.dat"
        );

        // CREAR SISTEMA

        SistemaClub primero = nuevo();

        Deporte futbol =
            new Deporte("Futbol", 7);

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        Equipo local = new Equipo(
            "Seneca local",
            futbol,
            mayores,
            16
        );

        Equipo rival = new Equipo(
            "Seneca rival",
            futbol,
            mayores,
            16
        );

        primero.getClub().registrarEquipo(local);
        primero.getClub().registrarEquipo(rival);

        // OCHO JUGADORES EN LOCAL Y SIETE EN RIVAL

        Socio[] f = inscribir(
            primero,
            local,
            "F",
            8
        );

        inscribir(
            primero,
            rival,
            "R",
            7
        );

        // PROGRAMAR TRES PARTIDOS

        PartidoInterno p1 =
            primero.getPartidosInternos()
                .programarPartido(
                    LocalDate.of(2026, 11, 1),
                    local,
                    rival
                );

        primero.getPartidosInternos()
            .programarPartido(
                LocalDate.of(2026, 11, 8),
                local,
                rival
            );

        primero.getPartidosInternos()
            .programarPartido(
                LocalDate.of(2026, 11, 15),
                local,
                rival
            );

        // PRIMER PARTIDO: DOS AMARILLAS A F0

        primero.getEstadisticas()
            .iniciarPartido(p1);

        primero.getEstadisticas()
            .registrarEstadistica(
                new EstadisticaFutbol(
                    p1,
                    local,
                    f[0],
                    2,
                    1,
                    2,
                    0
                )
            );

        p1.registrarResultado(2, 1);

        // GUARDAR TAMBIEN UNA VENTA COMERCIAL

        primero.getTienda().registrarArticulo(
            new Articulo(
                "BAL-E",
                "Balon",
                "Deporte",
                10000
            ),
            2
        );

        primero.getTienda().vender(
            f[0],
            Map.of("BAL-E", 1),
            null,
            0
        );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA DE ESTADISTICAS Y SANCIONES"
        );

        System.out.println(
            "---------------------------------------------------"
        );

        verificar(
            "Un partido finalizado y dos pendientes",

            primero.getPartidosInternos()
                .getPartidos().size() == 3 &&

            p1.estaFinalizado()
        );

        verificar(
            "Estadistica inicial registrada",

            primero.getEstadisticas()
                .getEstadisticas(p1).size() == 1
        );

        verificar(
            "F0 suspendido para siguiente partido",

            primero.getEstadisticas()
                .suspendidoEnProximoPartido(
                    local,
                    "F0"
                )
        );

        // PRIMER GUARDADO

        PersistenciaSistema.guardar(
            primero,
            ruta
        );

        verificar(
            "Archivo de estadisticas creado",

            Files.isRegularFile(ruta)
        );

        // PRIMER REINICIO

        SistemaClub segundo = cargar(ruta);

        Equipo local2 =
            segundo.getClub().getEquipos().get(0);

        PartidoInterno rp1 =
            segundo.getPartidosInternos()
                .getPartidos().get(0);

        PartidoInterno rp2 =
            segundo.getPartidosInternos()
                .getPartidos().get(1);

        verificar(
            "Tres partidos recuperados",

            segundo.getPartidosInternos()
                .getPartidos().size() == 3
        );

        verificar(
            "Resultados de primer partido conservados",

            rp1.estaFinalizado() &&

            rp1.getMarcadorLocal() == 2 &&

            !rp2.estaFinalizado()
        );

        verificar(
            "Estadisticas asociadas a partido restaurado",

            segundo.getEstadisticas()
                .getEstadisticas(rp1).size() == 1
        );

        verificar(
            "Goles, asistencias y tarjetas recuperados",

            ((EstadisticaFutbol) segundo.getEstadisticas()
                .getEstadisticas(rp1).get(0))
                .getGoles() == 2 &&

            ((EstadisticaFutbol) segundo.getEstadisticas()
                .getEstadisticas(rp1).get(0))
                .getAmarillas() == 2
        );

        verificar(
            "Sancion de F0 pendiente tras reinicio",

            segundo.getEstadisticas()
                .suspendidoEnProximoPartido(
                    local2,
                    "F0"
                )
        );

        verificar(
            "El partido restaurado permite iniciar segundo encuentro",

            !rp2.estaFinalizado()
        );

        // SEGUNDO PARTIDO: SE CUMPLE SANCION DE F0

        segundo.getEstadisticas()
            .iniciarPartido(rp2);

        verificar(
            "Al iniciar el segundo encuentro se consume la sancion",

            !segundo.getEstadisticas()
                .suspendidoEnProximoPartido(
                    local2,
                    "F0"
                )
        );

        rechazar(
            "F0 no puede participar en el encuentro que cumple sancion",

            () -> segundo.getEstadisticas()
                .registrarEstadistica(
                    new EstadisticaFutbol(
                        rp2,
                        local2,
                        f[0],
                        0,
                        0,
                        0,
                        0
                    )
                )
        );

        // F1 RECIBE TARJETA ROJA

        segundo.getEstadisticas()
            .registrarEstadistica(
                new EstadisticaFutbol(
                    rp2,
                    local2,
                    f[1],
                    1,
                    0,
                    0,
                    1
                )
            );

        verificar(
            "Tarjeta roja de F1 genera nueva suspension",

            segundo.getEstadisticas()
                .suspendidoEnProximoPartido(
                    local2,
                    "F1"
                )
        );

        rp2.registrarResultado(1, 0);

        // SEGUNDO GUARDADO

        PersistenciaSistema.guardar(
            segundo,
            ruta
        );

        // SEGUNDO REINICIO

        SistemaClub tercero = cargar(ruta);

        Equipo local3 =
            tercero.getClub().getEquipos().get(0);

        PartidoInterno rp2c =
            tercero.getPartidosInternos()
                .getPartidos().get(1);

        PartidoInterno rp3 =
            tercero.getPartidosInternos()
                .getPartidos().get(2);

        verificar(
            "Segundo reinicio conserva estadisticas de ambos partidos",

            tercero.getEstadisticas()
                .getEstadisticas(rp2c).size() == 1 &&

            tercero.getEstadisticas()
                .getEstadisticas(
                    tercero.getPartidosInternos()
                        .getPartidos().get(0)
                ).size() == 1
        );

        verificar(
            "Se conserva la sancion roja de F1",

            tercero.getEstadisticas()
                .suspendidoEnProximoPartido(
                    local3,
                    "F1"
                )
        );

        verificar(
            "Sancion de F0 no revive en el segundo reinicio",

            !tercero.getEstadisticas()
                .suspendidoEnProximoPartido(
                    local3,
                    "F0"
                )
        );

        // TERCER PARTIDO

        tercero.getEstadisticas()
            .iniciarPartido(rp3);

        rechazar(
            "F1 bloqueado durante el tercer partido",

            () -> tercero.getEstadisticas()
                .registrarEstadistica(
                    new EstadisticaFutbol(
                        rp3,
                        local3,
                        f[1],
                        0,
                        0,
                        0,
                        0
                    )
                )
        );

        // F0 YA PUEDE VOLVER A JUGAR

        tercero.getEstadisticas()
            .registrarEstadistica(
                new EstadisticaFutbol(
                    rp3,
                    local3,
                    f[0],
                    0,
                    1,
                    0,
                    0
                )
            );

        verificar(
            "F0 vuelve a jugar y registra estadisticas",

            tercero.getEstadisticas()
                .getEstadisticas(rp3).size() == 1
        );

        verificar(
            "F1 cumple suspension y queda habilitado para siguiente",

            !tercero.getEstadisticas()
                .suspendidoEnProximoPartido(
                    local3,
                    "F1"
                )
        );

        // COMPROBAR INFORMACION COMERCIAL

        verificar(
            "Venta e inventario comercial se conservaron",

            tercero.getTienda()
                .getVentas().size() == 1 &&

            tercero.getTienda()
                .getExistencias("BAL-E") == 1
        );

        verificar(
            "Historial de partidos sigue conectado",

            tercero.getPartidosInternos()
                .getPartidos().get(0)
                .estaFinalizado() &&

            tercero.getPartidosInternos()
                .getPartidos().get(1)
                .estaFinalizado()
        );

        // RESULTADOS

        System.out.println(
            "---------------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Partidos internos recuperados: " +

            tercero.getPartidosInternos()
                .getPartidos().size()
        );
    }
}
