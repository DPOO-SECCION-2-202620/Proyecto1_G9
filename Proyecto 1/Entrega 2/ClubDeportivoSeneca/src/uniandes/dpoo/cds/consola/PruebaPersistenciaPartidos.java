
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.modelo.comercial.Articulo;

import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.competencias.Escenario;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.competencias.InstitucionRival;
import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;

import uniandes.dpoo.cds.modelo.entrenamientos.SesionEntrenamiento;

import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.individuales.Modalidad;
import uniandes.dpoo.cds.modelo.individuales.Reserva;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class PruebaPersistenciaPartidos {

    private static int correctas = 0;
    private static int fallidas = 0;

    // COMPROBAR RESULTADOS

    private static void comprobar(
            String mensaje,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ")
            + mensaje
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // CREAR E INSCRIBIR JUGADORES

    private static Socio[] inscribir(
            SistemaClub sistema,
            Equipo equipo,
            String prefijo)
            throws Exception {

        Socio[] jugadores = new Socio[7];

        for (int i = 0; i < jugadores.length; i++) {

            jugadores[i] = new Socio(
                prefijo + i,
                "demo",
                LocalDate.of(2001, 1, 1)
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

    // CREAR SISTEMA NUEVO

    private static SistemaClub nuevo() {

        return new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    // CARGAR SISTEMA DESDE ARCHIVO

    private static SistemaClub cargar(Path archivo)
            throws Exception {

        return PersistenciaSistema.cargarSistema(
            archivo,
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    public static void main(String[] args)
            throws Exception {

        // ARCHIVO DE PRUEBA NUEVO

        Path archivo = Path.of(
            "datos",
            "sistema-partidos-prueba.dat"
        );

        // CREAR SISTEMA Y USUARIOS

        SistemaClub primero = nuevo();

        Administrador administrador =
            new Administrador(
                "adminP",
                "demo"
            );

        Entrenador entrenador =
            new Entrenador(
                "entP",
                "demo",
                "COD-P"
            );

        // CREAR DOS EQUIPOS DE FUTBOL

        Deporte futbol =
            new Deporte("Futbol", 7);

        Categoria adultos =
            new Categoria("Mayores", 18, 99);

        Equipo a = new Equipo(
            "Seneca A",
            futbol,
            adultos,
            15
        );

        Equipo b = new Equipo(
            "Seneca B",
            futbol,
            adultos,
            15
        );

        primero.getClub().registrarEquipo(a);
        primero.getClub().registrarEquipo(b);

        // INSCRIBIR SIETE JUGADORES POR EQUIPO

        Socio[] listaA =
            inscribir(primero, a, "pa");

        inscribir(primero, b, "pb");

        // CREAR PARTIDO INTERNO FINALIZADO

        PartidoInterno interno =
            primero.getPartidosInternos()
                .programarPartido(
                    LocalDate.of(2026, 11, 8),
                    a,
                    b
                );

        interno.registrarResultado(2, 1);

        // CREAR PARTIDO INTERNO PENDIENTE

        primero.getPartidosInternos()
            .programarPartido(
                LocalDate.of(2026, 11, 9),
                a,
                b
            );

        // CREAR COMPETENCIA

        Competencia competencia = new Competencia(
            "Cerros",
            "Cerros",
            "2026-2",
            futbol,
            adultos,
            "LIGA",
            7
        );

        primero.getCompetencias()
            .registrarCompetencia(competencia);

        // INSCRIBIR EQUIPO Y OFICIALIZAR NOMINA

        InscripcionCompetencia inscripcion =
            primero.getCompetencias()
                .inscribirEquipo(
                    a,
                    competencia
                );

        for (Socio socio : listaA) {
            inscripcion.agregarJugador(socio);
        }

        inscripcion.oficializarNomina();

        // CREAR PARTIDO OFICIAL FINALIZADO

        PartidoCompetencia oficial =
            primero.getPartidosOficiales()
                .programarPartido(
                    inscripcion,
                    new InstitucionRival(
                        "Universidad A"
                    ),
                    new Escenario(
                        "Estadio 1"
                    ),
                    LocalDate.of(2026, 11, 10),
                    "Liga"
                );

        // ALINEAR JUGADORES

        for (Socio socio : listaA) {
            oficial.alinearJugador(socio);
        }

        oficial.confirmarAlineacion();
        oficial.registrarResultado(3, 1);

        // CREAR OTRO PARTIDO OFICIAL PENDIENTE

        primero.getPartidosOficiales()
            .programarPartido(
                inscripcion,
                new InstitucionRival(
                    "Universidad B"
                ),
                new Escenario(
                    "Estadio 2"
                ),
                LocalDate.of(2026, 11, 11),
                "Liga"
            );

        // CREAR INSTALACION Y RESERVA

        Instalacion cancha = new Instalacion(
            "TEN-P",
            new Disciplina("Tenis", 2),
            4,
            false
        );

        primero.getClub().registrarReserva(
            new Reserva(
                listaA[0],
                cancha,
                LocalDate.of(2026, 11, 12),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                2,
                Modalidad.SENCILLOS
            )
        );

        // CREAR ENTRENAMIENTO

        SesionEntrenamiento sesion =
            new SesionEntrenamiento(
                LocalDate.of(2026, 11, 13),
                LocalTime.of(9, 0),
                60,
                entrenador,
                cancha
            );

        primero.getEntrenamientos()
            .programarSesion(sesion);

        // REGISTRAR PRODUCTO, VENTA Y MENSUALIDAD

        primero.getTienda().registrarArticulo(
            new Articulo(
                "BAL-P",
                "Balon",
                "Deporte",
                20000
            ),
            2
        );

        primero.getTienda().vender(
            listaA[0],
            Map.of("BAL-P", 1),
            null,
            0
        );

        primero.getReportesFinancieros()
            .registrarMensualidad(
                listaA[0],
                YearMonth.of(2026, 11),
                LocalDate.of(2026, 11, 13),
                100000
            );

        // INICIAR PRUEBAS

        System.out.println(
            "PRUEBAS DE PERSISTENCIA DE PARTIDOS Y COMPETENCIAS"
        );

        System.out.println(
            "-------------------------------------------------"
        );

        comprobar(
            "Dos equipos antes de guardar",
            primero.getClub().getEquipos().size() == 2
        );

        comprobar(
            "Dos partidos internos, uno terminado",

            primero.getPartidosInternos()
                .getPartidos().size() == 2 &&

            primero.getPartidosInternos()
                .getPartidos().get(0).estaFinalizado()
        );

        comprobar(
            "Competencia y nomina oficial disponibles",

            inscripcion.isOficial() &&

            inscripcion.getNomina().size() == 7
        );

        comprobar(
            "Dos partidos oficiales, uno terminado",

            primero.getPartidosOficiales()
                .getPartidos().size() == 2 &&

            !primero.getPartidosOficiales()
                .getPartidos().get(1).estaFinalizado()
        );

        // GUARDAR SISTEMA

        PersistenciaSistema.guardar(
            primero,
            archivo
        );

        comprobar(
            "Archivo independiente creado",
            Files.isRegularFile(archivo)
        );

        // PRIMER REINICIO

        SistemaClub segundo = cargar(archivo);

        comprobar(
            "Dos equipos y catorce jugadores recuperados",

            segundo.getClub().getEquipos().size() == 2 &&

            segundo.getClub().getEquipos()
                .get(0).getInscripciones().size() == 7 &&

            segundo.getClub().getEquipos()
                .get(1).getInscripciones().size() == 7
        );

        comprobar(
            "Reserva de tenis recuperada",

            segundo.getClub().getReservas().size() == 1
        );

        comprobar(
            "Dos partidos internos recuperados",

            segundo.getPartidosInternos()
                .getPartidos().size() == 2
        );

        comprobar(
            "Resultado interno y pendiente conservados",

            segundo.getPartidosInternos()
                .getPartidos().get(0)
                .getMarcadorLocal() == 2 &&

            !segundo.getPartidosInternos()
                .getPartidos().get(1)
                .estaFinalizado()
        );

        comprobar(
            "Competencia e inscripcion recuperadas",

            segundo.getCompetencias()
                .getCompetencias().size() == 1 &&

            segundo.getCompetencias()
                .getInscripciones().size() == 1
        );

        // CONSULTAR INSCRIPCION RECUPERADA

        InscripcionCompetencia cargada =
            segundo.getCompetencias()
                .getInscripciones().get(0);

        comprobar(
            "Nomina oficial de siete integrantes conservada",

            cargada.isOficial() &&

            cargada.getNomina().size() == 7
        );

        comprobar(
            "Los partidos oficiales apuntan a la inscripcion restaurada",

            segundo.getPartidosOficiales()
                .getPartidos().get(0)
                .getInscripcion() == cargada
        );

        comprobar(
            "Alineacion de siete jugadores recuperada",

            segundo.getPartidosOficiales()
                .getPartidos().get(0)
                .getAlineacion().size() == 7 &&

            segundo.getPartidosOficiales()
                .getPartidos().get(0)
                .isAlineacionConfirmada()
        );

        comprobar(
            "Resultado oficial y partido pendiente conservados",

            segundo.getPartidosOficiales()
                .getPartidos().get(0)
                .getMarcadorClub() == 3 &&

            !segundo.getPartidosOficiales()
                .getPartidos().get(1)
                .estaFinalizado()
        );

        // VERIFICAR REPORTE DEPORTIVO

        comprobar(
            "Reporte cuenta los dos partidos finalizados",

            segundo.getReportesDeportivos()
                .generarResumen(
                    administrador,
                    LocalDate.of(2026, 11, 1),
                    LocalDate.of(2026, 11, 30)
                )
                .getTotalPartidosFinalizados() == 2
        );

        // VERIFICAR MODULOS ANTERIORES

        comprobar(
            "Entrenamiento, venta y mensualidad siguen presentes",

            segundo.getEntrenamientos()
                .getSesiones().size() == 1 &&

            segundo.getTienda()
                .getVentas().size() == 1 &&

            segundo.getReportesFinancieros()
                .getMensualidades().size() == 1
        );

        comprobar(
            "Inscripcion restaurada se reconoce como propia",

            segundo.getCompetencias()
                .getInscripciones().contains(
                    segundo.getPartidosOficiales()
                        .getPartidos().get(0)
                        .getInscripcion()
                )
        );

        // TERMINAR PARTIDOS QUE ESTABAN PENDIENTES

        PartidoInterno pendiente =
            segundo.getPartidosInternos()
                .getPartidos().get(1);

        pendiente.registrarResultado(0, 0);

        PartidoCompetencia oficialPendiente =
            segundo.getPartidosOficiales()
                .getPartidos().get(1);

        for (Socio socio : cargada.getNomina()) {
            oficialPendiente.alinearJugador(socio);
        }

        oficialPendiente.confirmarAlineacion();
        oficialPendiente.registrarResultado(1, 0);

        comprobar(
            "Se pueden finalizar partidos recuperados",

            pendiente.estaFinalizado() &&

            oficialPendiente.estaFinalizado()
        );

        // SEGUNDO GUARDADO Y REINICIO

        PersistenciaSistema.guardar(
            segundo,
            archivo
        );

        SistemaClub tercero = cargar(archivo);

        comprobar(
            "Segundo reinicio conserva cuatro resultados",

            tercero.getPartidosInternos()
                .getPartidos().stream()
                .allMatch(
                    PartidoInterno::estaFinalizado
                ) &&

            tercero.getPartidosOficiales()
                .getPartidos().stream()
                .allMatch(
                    PartidoCompetencia::estaFinalizado
                )
        );

        comprobar(
            "Segundo reinicio conserva informacion comercial",

            tercero.getTienda()
                .getExistencias("BAL-P") == 1 &&

            tercero.getReportesFinancieros()
                .getMensualidades().size() == 1
        );

        // RESULTADOS FINALES

        System.out.println(
            "-------------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Partidos internos recuperados: "
            + tercero.getPartidosInternos()
                .getPartidos().size()
        );

        System.out.println(
            "Partidos oficiales recuperados: "
            + tercero.getPartidosOficiales()
                .getPartidos().size()
        );
    }
}
