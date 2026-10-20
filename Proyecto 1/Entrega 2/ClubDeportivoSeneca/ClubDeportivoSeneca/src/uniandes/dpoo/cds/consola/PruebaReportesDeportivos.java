
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import uniandes.dpoo.cds.aplicacion.*;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.competencias.*;
import uniandes.dpoo.cds.modelo.conjunto.*;
import uniandes.dpoo.cds.modelo.entrenamientos.*;
import uniandes.dpoo.cds.modelo.individuales.*;
import uniandes.dpoo.cds.modelo.usuarios.*;

public class PruebaReportesDeportivos {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // PRUEBA QUE DEBE FUNCIONAR

    private static void aceptar(
            String caso,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + caso);
            correctas++;

        } catch (Exception ex) {

            System.out.println(
                "[FALLO] " + caso + " -> " + ex
            );

            fallidas++;
        }
    }

    // PRUEBA QUE DEBE SER RECHAZADA

    private static void rechazar(
            String caso,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + caso + " (se acepto)"
            );

            fallidas++;

        } catch (ReglaNegocioException ex) {

            System.out.println(
                "[OK] " + caso +
                " -> " + ex.getMessage()
            );

            correctas++;

        } catch (Exception ex) {

            System.out.println(
                "[FALLO] " + caso + " -> " + ex
            );

            fallidas++;
        }
    }

    // VERIFICAR CONDICIONES

    private static void verificar(
            String caso,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ") + caso
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // CREAR JUGADORES PARA LOS EQUIPOS

    private static Socio[] crearJugadores(
            ClubDeportivoSeneca club,
            Equipo equipo,
            String prefijo)
            throws ReglaNegocioException {

        Socio[] jugadores = new Socio[7];

        for (int i = 0; i < jugadores.length; i++) {

            jugadores[i] = new Socio(
                prefijo + i,
                "demo",
                LocalDate.of(2001, 1, 1)
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

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // CREAR CLUB Y GESTORES

        ClubDeportivoSeneca club =
            new ClubDeportivoSeneca();

        GestorEntrenamientos entrenamientos =
            new GestorEntrenamientos();

        GestorPartidosInternos internos =
            new GestorPartidosInternos(club);

        GestorCompetencias competencias =
            new GestorCompetencias(club);

        GestorPartidosCompetencia oficiales =
            new GestorPartidosCompetencia(competencias);

        GestorPartidosPractica individuales =
            new GestorPartidosPractica();

        GestorReportesDeportivos reportes =
            new GestorReportesDeportivos(
                entrenamientos,
                internos,
                oficiales,
                individuales
            );

        // CREAR USUARIOS

        Administrador admin = new Administrador(
            "adminReportes",
            "demo"
        );

        Entrenador entrenador = new Entrenador(
            "entReportes",
            "demo",
            "DES-01"
        );

        // CREAR DEPORTE, CATEGORIA Y EQUIPOS

        Deporte futbol = new Deporte("Futbol", 7);

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        Equipo equipoA = new Equipo(
            "Seneca A",
            futbol,
            mayores,
            15
        );

        Equipo equipoB = new Equipo(
            "Seneca B",
            futbol,
            mayores,
            15
        );

        club.registrarEquipo(equipoA);
        club.registrarEquipo(equipoB);

        // CREAR JUGADORES

        Socio[] a = crearJugadores(
            club,
            equipoA,
            "RA"
        );

        Socio[] b = crearJugadores(
            club,
            equipoB,
            "RB"
        );

        // CREAR INSTALACION

        Disciplina tenis =
            new Disciplina("Tenis", 2);

        Instalacion cancha = new Instalacion(
            "TEN-R",
            tenis,
            4,
            false
        );

        LocalDate cinco =
            LocalDate.of(2026, 11, 5);

        LocalDate seis =
            LocalDate.of(2026, 11, 6);

        System.out.println(
            "PRUEBAS DE REPORTES DEPORTIVOS"
        );

        System.out.println(
            "--------------------------------------"
        );

        // PRUEBAS INICIALES

        verificar(
            "Reporte inicialmente vacio",

            reportes.generarResumen(
                admin,
                cinco,
                cinco.plusDays(10)
            ).getTotalPartidosFinalizados() == 0
        );

        rechazar(
            "Reporte requiere administrador",
            () -> reportes.generarResumen(
                null,
                cinco,
                seis
            )
        );

        rechazar(
            "Rechazar fechas invertidas",
            () -> reportes.generarResumen(
                admin,
                seis,
                cinco
            )
        );

        rechazar(
            "Rechazar fecha nula",
            () -> reportes.generarResumen(
                admin,
                null,
                cinco
            )
        );

        // CREAR ENTRENAMIENTOS

        SesionEntrenamiento sesion1 =
            new SesionEntrenamiento(
                cinco,
                LocalTime.of(8, 0),
                90,
                entrenador,
                cancha
            );

        SesionEntrenamiento sesion2 =
            new SesionEntrenamiento(
                seis,
                LocalTime.of(10, 0),
                60,
                entrenador,
                cancha
            );

        aceptar(
            "Programar dos entrenamientos",
            () -> {

                entrenamientos.programarSesion(sesion1);
                entrenamientos.programarSesion(sesion2);
            }
        );

        // CONVOCAR Y REGISTRAR ASISTENCIAS

        aceptar(
            "Convocar y registrar asistencias",
            () -> {

                // PRIMERA SESION

                sesion1.convocarSocio(a[0]);
                sesion1.convocarSocio(a[1]);
                sesion1.convocarSocio(a[2]);

                sesion1.finalizarSesion(entrenador);

                sesion1.registrarAsistencia(
                    entrenador,
                    a[0],
                    EstadoAsistencia.ASISTIO
                );

                sesion1.registrarAsistencia(
                    entrenador,
                    a[1],
                    EstadoAsistencia.TARDE
                );

                // SEGUNDA SESION

                sesion2.convocarSocio(a[3]);
                sesion2.convocarSocio(a[4]);

                sesion2.finalizarSesion(entrenador);

                sesion2.registrarAsistencia(
                    entrenador,
                    a[3],
                    EstadoAsistencia.FALTO
                );

                sesion2.registrarAsistencia(
                    entrenador,
                    a[4],
                    EstadoAsistencia.ASISTIO
                );
            }
        );

        // CONSULTAR RESUMEN DE ASISTENCIA

        ResumenDeportivo resumenAsistencia =
            reportes.generarResumen(
                admin,
                cinco,
                seis
            );

        verificar(
            "Dos sesiones programadas y finalizadas",

            resumenAsistencia
                .getSesionesProgramadas() == 2 &&

            resumenAsistencia
                .getSesionesFinalizadas() == 2
        );

        verificar(
            "Cinco convocatorias",

            resumenAsistencia.getConvocatorias() == 5
        );

        verificar(
            "Dos asistieron y uno llego tarde",

            resumenAsistencia.getAsistieron() == 2 &&

            resumenAsistencia.getLlegaronTarde() == 1
        );

        verificar(
            "Uno falto y uno sin asistencia registrada",

            resumenAsistencia.getFaltaron() == 1 &&

            resumenAsistencia.getSinRegistrar() == 1
        );

        verificar(
            "Porcentaje de asistencia 60%",

            Math.abs(
                resumenAsistencia.getPorcentajeAsistencia()
                - 60
            ) < 0.001
        );

        verificar(
            "Reporte de un dia solo incluye su sesion",

            reportes.generarResumen(
                admin,
                cinco,
                cinco
            ).getConvocatorias() == 3
        );

        // PROGRAMAR PARTIDOS INTERNOS

        PartidoInterno interno1 =
            internos.programarPartido(
                LocalDate.of(2026, 11, 7),
                equipoA,
                equipoB
            );

        PartidoInterno internoPendiente =
            internos.programarPartido(
                LocalDate.of(2026, 11, 11),
                equipoA,
                equipoB
            );

        aceptar(
            "Registrar un resultado interno",
            () -> interno1.registrarResultado(2, 1)
        );

        verificar(
            "Partido interno pendiente no cuenta",

            !internoPendiente.estaFinalizado() &&

            reportes.generarResumen(
                admin,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
            ).getPartidosInternosFinalizados() == 1
        );

        // CREAR COMPETENCIA OFICIAL

        Competencia torneo = new Competencia(
            "Cerros",
            "Cerros",
            "2026-2",
            futbol,
            mayores,
            "LIGA",
            7
        );

        competencias.registrarCompetencia(torneo);

        InscripcionCompetencia inscripcion =
            competencias.inscribirEquipo(
                equipoA,
                torneo
            );

        for (Socio socio : a) {
            inscripcion.agregarJugador(socio);
        }

        inscripcion.oficializarNomina();

        // CREAR DOS PARTIDOS OFICIALES

        PartidoCompetencia oficial1 =
            oficiales.programarPartido(
                inscripcion,
                new InstitucionRival("Universidad 1"),
                new Escenario("Cancha Central"),
                LocalDate.of(2026, 11, 8),
                "Liga"
            );

        PartidoCompetencia oficial2 =
            oficiales.programarPartido(
                inscripcion,
                new InstitucionRival("Universidad 2"),
                new Escenario("Cancha Central"),
                LocalDate.of(2026, 11, 9),
                "Liga"
            );

        aceptar(
            "Registrar victoria y empate oficiales",
            () -> {

                for (Socio socio : a) {

                    oficial1.alinearJugador(socio);
                    oficial2.alinearJugador(socio);
                }

                oficial1.confirmarAlineacion();
                oficial2.confirmarAlineacion();

                oficial1.registrarResultado(3, 1);
                oficial2.registrarResultado(2, 2);
            }
        );

        ResumenDeportivo resultados =
            reportes.generarResumen(
                admin,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
            );

        verificar(
            "Dos partidos oficiales finalizados",

            resultados
                .getPartidosOficialesFinalizados() == 2
        );

        verificar(
            "Una victoria y un empate oficiales",

            resultados.getVictoriasOficiales() == 1 &&

            resultados.getEmpatesOficiales() == 1 &&

            resultados.getDerrotasOficiales() == 0
        );

        // INSCRIBIR JUGADORES EN TENIS

        individuales.registrarInscripcion(
            new InscripcionDisciplina(
                a[0],
                tenis,
                Nivel.INTERMEDIO
            )
        );

        individuales.registrarInscripcion(
            new InscripcionDisciplina(
                a[1],
                tenis,
                Nivel.INTERMEDIO
            )
        );

        // VALIDAR RANKING NO REGISTRADO

        rechazar(
            "No guardar ranking de socio no inscrito",
            () -> reportes.registrarInstantaneaRanking(
                LocalDate.of(2026, 11, 1),
                a[2],
                tenis
            )
        );

        // GUARDAR RANKINGS INICIALES

        aceptar(
            "Guardar rankings iniciales",
            () -> {

                reportes.registrarInstantaneaRanking(
                    LocalDate.of(2026, 11, 1),
                    a[0],
                    tenis
                );

                reportes.registrarInstantaneaRanking(
                    LocalDate.of(2026, 11, 1),
                    a[1],
                    tenis
                );
            }
        );

        // PROGRAMAR PARTIDO INDIVIDUAL

        PartidoPractica practica =
            individuales.programarPartido(
                LocalDate.of(2026, 11, 10),
                tenis,
                Modalidad.SENCILLOS,
                a[0],
                a[1]
            );

        aceptar(
            "Finalizar partido individual",
            () -> {

                individuales.registrarEstadistica(
                    practica,
                    new EstadisticaRaqueta(
                        a[0], 2, 12, 3, 1
                    )
                );

                individuales.registrarEstadistica(
                    practica,
                    new EstadisticaRaqueta(
                        a[1], 1, 7, 1, 4
                    )
                );

                individuales.finalizarPartido(
                    practica,
                    a[0]
                );
            }
        );

        // GUARDAR RANKINGS ACTUALIZADOS

        aceptar(
            "Guardar rankings despues del partido",
            () -> {

                reportes.registrarInstantaneaRanking(
                    LocalDate.of(2026, 11, 11),
                    a[0],
                    tenis
                );

                reportes.registrarInstantaneaRanking(
                    LocalDate.of(2026, 11, 11),
                    a[1],
                    tenis
                );
            }
        );

        rechazar(
            "No repetir instantanea del mismo dia",
            () -> reportes.registrarInstantaneaRanking(
                LocalDate.of(2026, 11, 11),
                a[0],
                tenis
            )
        );

        // CONSULTAR EVOLUCION

        List<RegistroRanking> evolucion =
            reportes.evolucionRanking(
                admin,
                a[0],
                tenis,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
            );

        verificar(
            "Dos registros ordenados en evolucion ELO",

            evolucion.size() == 2 &&

            evolucion.get(0).getFecha().isBefore(
                evolucion.get(1).getFecha()
            )
        );

        verificar(
            "ELO inicial 1000 y posterior 1016",

            evolucion.get(0).getPuntos() == 1000.0 &&

            evolucion.get(1).getPuntos() == 1016.0
        );

        verificar(
            "Ranking por persona independiente",

            reportes.evolucionRanking(
                admin,
                a[1],
                tenis,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
            ).get(1).getPuntos() == 984.0
        );

        verificar(
            "Filtro de fechas en evolucion ELO",

            reportes.evolucionRanking(
                admin,
                a[0],
                tenis,
                LocalDate.of(2026, 11, 11),
                LocalDate.of(2026, 11, 30)
            ).size() == 1
        );

        rechazar(
            "Evolucion requiere administrador",
            () -> reportes.evolucionRanking(
                null,
                a[0],
                tenis,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
            )
        );

        // REPORTE GENERAL DEL MES

        ResumenDeportivo finalNov =
            reportes.generarResumen(
                admin,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 30)
            );

        verificar(
            "Cuatro partidos finalizados entre todos los tipos",

            finalNov.getTotalPartidosFinalizados() == 4 &&

            finalNov.getPartidosInternosFinalizados() == 1 &&

            finalNov.getPartidosPracticaFinalizados() == 1
        );

        verificar(
            "En diciembre no hay actividades",

            reportes.generarResumen(
                admin,
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 31)
            ).getTotalPartidosFinalizados() == 0
        );

        // PROTEGER EL HISTORIAL

        try {

            evolucion.clear();

            verificar(
                "Historial protegido de modificaciones",
                false
            );

        } catch (UnsupportedOperationException ex) {

            verificar(
                "Historial protegido de modificaciones",
                true
            );
        }

        // RESULTADOS FINALES

        System.out.println(
            "--------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Asistencia: "
            + finalNov.getPorcentajeAsistencia()
            + "%"
        );

        System.out.println(
            "Partidos finalizados: "
            + finalNov.getTotalPartidosFinalizados()
        );

        System.out.println(
            "Ranking final RA0: "
            + evolucion.get(1).getPuntos()
        );
    }
}
