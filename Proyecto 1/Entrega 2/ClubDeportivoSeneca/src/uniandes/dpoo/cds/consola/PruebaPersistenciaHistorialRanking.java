
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Articulo;

import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.EstadisticaRaqueta;
import uniandes.dpoo.cds.modelo.individuales.InscripcionDisciplina;
import uniandes.dpoo.cds.modelo.individuales.Modalidad;
import uniandes.dpoo.cds.modelo.individuales.Nivel;
import uniandes.dpoo.cds.modelo.individuales.PartidoPractica;
import uniandes.dpoo.cds.modelo.individuales.RegistroRanking;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class PruebaPersistenciaHistorialRanking {

    private static int correctas = 0;
    private static int fallidas = 0;

    // VERIFICAR RESULTADOS

    private static void verificar(
            String mensaje,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ") + mensaje
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // COMPARAR ELO

    private static boolean igual(double a, double b) {

        return Math.abs(a - b) < 0.0001;
    }

    // CREAR SISTEMA NUEVO

    private static SistemaClub crear() {

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

    public static void main(String[] args)
            throws Exception {

        // ARCHIVO DE PRUEBA

        Path ruta = Path.of(
            "datos",
            "sistema-historial-ranking-prueba.dat"
        );

        LocalDate dia1 =
            LocalDate.of(2026, 11, 15);

        LocalDate dia2 =
            dia1.plusDays(1);

        LocalDate dia3 =
            dia1.plusDays(2);

        Disciplina tenis =
            new Disciplina("Tenis", 2);

        // CREAR USUARIOS

        Socio ana = new Socio(
            "anaHist",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Socio juan = new Socio(
            "juanHist",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Administrador admin = new Administrador(
            "adminHist",
            "demo"
        );

        // CREAR SISTEMA INICIAL

        SistemaClub primero = crear();

        // INSCRIBIR JUGADORES

        primero.getPartidosIndividuales()
            .registrarInscripcion(
                new InscripcionDisciplina(
                    ana,
                    tenis,
                    Nivel.PRINCIPIANTE
                )
            );

        primero.getPartidosIndividuales()
            .registrarInscripcion(
                new InscripcionDisciplina(
                    juan,
                    tenis,
                    Nivel.INTERMEDIO
                )
            );

        // REGISTRAR RANKING ANTES DE COMPETIR

        primero.getReportesDeportivos()
            .registrarInstantaneaRanking(
                dia1,
                ana,
                tenis
            );

        primero.getReportesDeportivos()
            .registrarInstantaneaRanking(
                dia1,
                juan,
                tenis
            );

        // PROGRAMAR PARTIDO

        PartidoPractica partido =
            primero.getPartidosIndividuales()
                .programarPartido(
                    dia2,
                    tenis,
                    Modalidad.SENCILLOS,
                    ana,
                    juan
                );

        // REGISTRAR ESTADISTICAS

        primero.getPartidosIndividuales()
            .registrarEstadistica(
                partido,
                new EstadisticaRaqueta(
                    ana,
                    2,
                    12,
                    3,
                    1
                )
            );

        primero.getPartidosIndividuales()
            .registrarEstadistica(
                partido,
                new EstadisticaRaqueta(
                    juan,
                    0,
                    6,
                    0,
                    4
                )
            );

        // GANA ANA

        primero.getPartidosIndividuales()
            .finalizarPartido(
                partido,
                ana
            );

        // REGISTRAR RANKING DESPUES DEL PARTIDO

        primero.getReportesDeportivos()
            .registrarInstantaneaRanking(
                dia2,
                ana,
                tenis
            );

        primero.getReportesDeportivos()
            .registrarInstantaneaRanking(
                dia2,
                juan,
                tenis
            );

        // AGREGAR VENTA COMERCIAL

        primero.getTienda()
            .registrarArticulo(
                new Articulo(
                    "BAL-H",
                    "Balon",
                    "Deportes",
                    10000
                ),
                2
            );

        primero.getTienda().vender(
            ana,
            Map.of("BAL-H", 1),
            null,
            0
        );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA HISTORICA DE RANKING"
        );

        System.out.println(
            "-------------------------------------------"
        );

        // CONSULTAR HISTORIAL ORIGINAL

        List<RegistroRanking> antes =
            primero.getReportesDeportivos()
                .evolucionRanking(
                    admin,
                    ana,
                    tenis,
                    dia1,
                    dia3
                );

        verificar(
            "Dos instantaneas originales de Ana",
            antes.size() == 2
        );

        verificar(
            "El historial conserva 1000 y 1016 sin recalcular",

            igual(
                antes.get(0).getPuntos(),
                1000
            ) &&

            igual(
                antes.get(1).getPuntos(),
                1016
            )
        );

        verificar(
            "El ranking actual de Ana es 1016",

            igual(
                primero.getPartidosIndividuales()
                    .getRanking(ana, tenis),
                1016
            )
        );

        verificar(
            "Cuatro registros historicos en total",

            primero.getReportesDeportivos()
                .getHistorialRankings().size() == 4
        );

        // GUARDAR SISTEMA

        PersistenciaSistema.guardar(
            primero,
            ruta
        );

        verificar(
            "Archivo independiente creado",
            Files.isRegularFile(ruta)
        );

        // PRIMER REINICIO

        SistemaClub segundo = cargar(ruta);

        List<RegistroRanking> anaRestaurada =
            segundo.getReportesDeportivos()
                .evolucionRanking(
                    admin,
                    ana,
                    tenis,
                    dia1,
                    dia3
                );

        List<RegistroRanking> juanRestaurado =
            segundo.getReportesDeportivos()
                .evolucionRanking(
                    admin,
                    juan,
                    tenis,
                    dia1,
                    dia3
                );

        verificar(
            "Se recuperan las cuatro instantaneas",

            segundo.getReportesDeportivos()
                .getHistorialRankings().size() == 4
        );

        verificar(
            "Historial de Ana recuperado y en orden",

            anaRestaurada.size() == 2 &&

            anaRestaurada.get(0)
                .getFecha().equals(dia1) &&

            anaRestaurada.get(1)
                .getFecha().equals(dia2)
        );

        verificar(
            "Historial de Ana conserva los valores originales",

            igual(
                anaRestaurada.get(0).getPuntos(),
                1000
            ) &&

            igual(
                anaRestaurada.get(1).getPuntos(),
                1016
            )
        );

        verificar(
            "Historial de Juan conserva los valores originales",

            juanRestaurado.size() == 2 &&

            igual(
                juanRestaurado.get(0).getPuntos(),
                1000
            ) &&

            igual(
                juanRestaurado.get(1).getPuntos(),
                984
            )
        );

        verificar(
            "Los ELO actuales tambien se conservan",

            igual(
                segundo.getPartidosIndividuales()
                    .getRanking(ana, tenis),
                1016
            ) &&

            igual(
                segundo.getPartidosIndividuales()
                    .getRanking(juan, tenis),
                984
            )
        );

        verificar(
            "Las ventas e inventarios siguen presentes",

            segundo.getTienda()
                .getVentas().size() == 1 &&

            segundo.getTienda()
                .getExistencias("BAL-H") == 1
        );

        // INTENTAR DUPLICAR UNA INSTANTANEA

        boolean duplicadaRechazada = false;

        try {

            segundo.getReportesDeportivos()
                .registrarInstantaneaRanking(
                    dia2,
                    ana,
                    tenis
                );

        } catch (ReglaNegocioException esperado) {

            duplicadaRechazada = true;
        }

        verificar(
            "Se sigue rechazando una instantanea duplicada",
            duplicadaRechazada
        );

        verificar(
            "El rechazo no altera el historial",

            segundo.getReportesDeportivos()
                .getHistorialRankings().size() == 4
        );

        // EVITAR MODIFICACION EXTERNA DEL HISTORIAL

        boolean inmodificable = false;

        try {

            segundo.getReportesDeportivos()
                .getHistorialRankings()
                .clear();

        } catch (UnsupportedOperationException esperado) {

            inmodificable = true;
        }

        verificar(
            "Consulta del historial no permite borrarlo",
            inmodificable
        );

        // PROGRAMAR UNA REVANCHA

        PartidoPractica revancha =
            segundo.getPartidosIndividuales()
                .programarPartido(
                    dia3,
                    tenis,
                    Modalidad.SENCILLOS,
                    ana,
                    juan
                );

        segundo.getPartidosIndividuales()
            .registrarEstadistica(
                revancha,
                new EstadisticaRaqueta(
                    ana,
                    0,
                    3,
                    0,
                    3
                )
            );

        segundo.getPartidosIndividuales()
            .registrarEstadistica(
                revancha,
                new EstadisticaRaqueta(
                    juan,
                    2,
                    12,
                    2,
                    1
                )
            );

        // AHORA GANA JUAN

        segundo.getPartidosIndividuales()
            .finalizarPartido(
                revancha,
                juan
            );

        double eloActual =
            segundo.getPartidosIndividuales()
                .getRanking(ana, tenis);

        // REGISTRAR NUEVA INSTANTANEA

        segundo.getReportesDeportivos()
            .registrarInstantaneaRanking(
                dia3,
                ana,
                tenis
            );

        verificar(
            "Revancha recuperada modifica ranking ELO",
            eloActual < 1016
        );

        List<RegistroRanking> evolucion =
            segundo.getReportesDeportivos()
                .evolucionRanking(
                    admin,
                    ana,
                    tenis,
                    dia1,
                    dia3
                );

        verificar(
            "El historial antiguo no cambia tras la revancha",

            evolucion.size() == 3 &&

            igual(
                evolucion.get(0).getPuntos(),
                1000
            ) &&

            igual(
                evolucion.get(1).getPuntos(),
                1016
            )
        );

        verificar(
            "Nueva instantanea refleja el ELO actualizado",

            igual(
                evolucion.get(2).getPuntos(),
                eloActual
            )
        );

        // SEGUNDO GUARDADO

        PersistenciaSistema.guardar(
            segundo,
            ruta
        );

        // SEGUNDO REINICIO

        SistemaClub tercero = cargar(ruta);

        List<RegistroRanking> finalAna =
            tercero.getReportesDeportivos()
                .evolucionRanking(
                    admin,
                    ana,
                    tenis,
                    dia1,
                    dia3
                );

        verificar(
            "Segundo reinicio conserva cinco instantaneas",

            tercero.getReportesDeportivos()
                .getHistorialRankings().size() == 5
        );

        verificar(
            "Segundo reinicio conserva los tres valores de Ana",

            finalAna.size() == 3 &&

            igual(
                finalAna.get(0).getPuntos(),
                1000
            ) &&

            igual(
                finalAna.get(1).getPuntos(),
                1016
            ) &&

            igual(
                finalAna.get(2).getPuntos(),
                eloActual
            )
        );

        verificar(
            "Segundo reinicio conserva los dos partidos",

            tercero.getPartidosIndividuales()
                .getPartidos().size() == 2
        );

        verificar(
            "Los reportes siguen viendo ambos partidos finalizados",

            tercero.getReportesDeportivos()
                .generarResumen(
                    admin,
                    dia1,
                    dia3
                )
                .getPartidosPracticaFinalizados() == 2
        );

        // RESULTADOS FINALES

        System.out.println(
            "-------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Instantaneas recuperadas: " +

            tercero.getReportesDeportivos()
                .getHistorialRankings().size()
        );

        System.out.println(
            "ELO actual de Ana: " +

            tercero.getPartidosIndividuales()
                .getRanking(ana, tenis)
        );
    }
}
