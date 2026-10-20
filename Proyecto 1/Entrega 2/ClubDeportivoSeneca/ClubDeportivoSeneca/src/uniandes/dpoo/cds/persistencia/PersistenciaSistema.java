
package uniandes.dpoo.cds.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.time.DayOfWeek;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.aplicacion.FaseASCUN;
import uniandes.dpoo.cds.aplicacion.GestorTurnos;
import uniandes.dpoo.cds.aplicacion.GestorUsuarios;
import uniandes.dpoo.cds.aplicacion.GestorCafeteria;
import uniandes.dpoo.cds.aplicacion.GestorFidelidad;
import uniandes.dpoo.cds.aplicacion.GestorEstadisticas;
import uniandes.dpoo.cds.aplicacion.GestorPartidosPractica;
import uniandes.dpoo.cds.aplicacion.GestorTienda;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.RegistroMensualidad;
import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto;

import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;

import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;

import uniandes.dpoo.cds.modelo.entrenamientos.SesionEntrenamiento;

import uniandes.dpoo.cds.modelo.individuales.Reserva;
import uniandes.dpoo.cds.modelo.individuales.RegistroRanking;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public final class PersistenciaSistema {

    private PersistenciaSistema() {
    }

    // ESTADO GENERAL DEL SISTEMA

    private static class Estado implements Serializable {

        private static final long serialVersionUID = 1L;

        // DATOS COMERCIALES

        private final GestorFidelidad fidelidad;
        private final GestorTienda tienda;
        private final GestorCafeteria cafeteria;

        private final List<RegistroMensualidad> mensualidades;
        private final List<SugerenciaProducto> sugerencias;

        // ENTRENAMIENTOS

        private final List<SesionEntrenamiento> sesiones;

        // EQUIPOS Y RESERVAS

        private final List<Equipo> equipos;
        private final List<Reserva> reservas;

        // PARTIDOS INTERNOS Y ESTADISTICAS

        private final List<PartidoInterno> internos;
        private final GestorEstadisticas estadisticasConjunto;

        // COMPETENCIAS

        private final List<Competencia> competencias;
        private final List<InscripcionCompetencia> inscripciones;
        private final List<PartidoCompetencia> oficiales;

        // DEPORTES INDIVIDUALES

        private final GestorPartidosPractica individuales;

        // HISTORIAL DE RANKINGS

        private final List<RegistroRanking> historialRankings;

        // TURNOS Y ASCUN

        private final GestorTurnos turnos;
        private final List<FaseASCUN> fasesASCUN;

        // NUEVO: CUENTAS DE USUARIO

        private final GestorUsuarios usuarios;

        // CAPTURAR ESTADO ACTUAL

        private Estado(SistemaClub sistema) {

            // COMERCIAL

            fidelidad = sistema.getFidelidad();

            tienda = sistema.getTienda();

            cafeteria = sistema.getCafeteria();

            mensualidades = new ArrayList<>(
                sistema.getReportesFinancieros()
                    .getMensualidades()
            );

            sugerencias = new ArrayList<>(
                sistema.getSugerencias()
                    .getSugerencias()
            );

            // ENTRENAMIENTOS

            sesiones = new ArrayList<>(
                sistema.getEntrenamientos()
                    .getSesiones()
            );

            // EQUIPOS Y RESERVAS

            equipos = new ArrayList<>(
                sistema.getClub().getEquipos()
            );

            reservas = new ArrayList<>(
                sistema.getClub().getReservas()
            );

            // PARTIDOS Y ESTADISTICAS

            internos = new ArrayList<>(
                sistema.getPartidosInternos()
                    .getPartidos()
            );

            estadisticasConjunto =
                sistema.getEstadisticas();

            // COMPETENCIAS

            competencias = new ArrayList<>(
                sistema.getCompetencias()
                    .getCompetencias()
            );

            inscripciones = new ArrayList<>(
                sistema.getCompetencias()
                    .getInscripciones()
            );

            oficiales = new ArrayList<>(
                sistema.getPartidosOficiales()
                    .getPartidos()
            );

            // INDIVIDUALES Y RANKINGS

            individuales =
                sistema.getPartidosIndividuales();

            historialRankings = new ArrayList<>(
                sistema.getReportesDeportivos()
                    .getHistorialRankings()
            );

            // TURNOS Y ASCUN

            turnos = sistema.getTurnos();

            fasesASCUN = new ArrayList<>(
                sistema.getFasesASCUN()
            );

            // NUEVO: GUARDAR CUENTAS

            usuarios = sistema.getUsuarios();
        }
    }

    // GUARDAR SISTEMA

    public static void guardar(
            SistemaClub sistema,
            Path archivo)
            throws IOException {

        if (sistema == null || archivo == null) {

            throw new IllegalArgumentException(
                "Sistema o archivo nulos"
            );
        }

        Path destino =
            archivo.toAbsolutePath().normalize();

        Path carpeta =
            destino.getParent();

        Files.createDirectories(carpeta);

        // CREAR ARCHIVO TEMPORAL

        Path temporal = Files.createTempFile(
            carpeta,
            "seneca-",
            ".tmp"
        );

        try {

            // ESCRIBIR TODOS LOS DATOS

            try (ObjectOutputStream salida =
                    new ObjectOutputStream(
                        Files.newOutputStream(temporal))) {

                salida.writeObject(
                    new Estado(sistema)
                );
            }

            // REEMPLAZAR ARCHIVO FINAL

            try {

                Files.move(
                    temporal,
                    destino,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                    temporal,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {

            Files.deleteIfExists(temporal);
        }
    }

    // CARGAR SISTEMA

    public static SistemaClub cargarSistema(
            Path archivo,
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion)
            throws IOException, ClassNotFoundException {

        if (archivo == null) {

            throw new IllegalArgumentException(
                "La ruta es obligatoria"
            );
        }

        Estado estado;

        // LEER ARCHIVO GUARDADO

        try (ObjectInputStream entrada =
                new ObjectInputStream(
                    Files.newInputStream(archivo))) {

            Object leido =
                entrada.readObject();

            if (!(leido instanceof Estado)) {

                throw new IOException(
                    "Formato de sistema no reconocido"
                );
            }

            estado = (Estado) leido;
        }

        // VALIDAR DATOS OBLIGATORIOS

        if (estado.fidelidad == null ||
            estado.tienda == null ||
            estado.cafeteria == null ||
            estado.mensualidades == null ||
            estado.sugerencias == null ||
            estado.sesiones == null) {

            throw new IOException(
                "Archivo de sistema incompleto"
            );
        }

        // RESTAURAR GESTORES PRINCIPALES

        // SI EL ARCHIVO ES DE UNA VERSION ANTERIOR,
        // estado.usuarios PUEDE SER NULL.

        SistemaClub sistema = new SistemaClub(
            apertura,
            cierre,
            diasAtencion,
            estado.fidelidad,
            estado.tienda,
            estado.cafeteria,
            estado.individuales,
            estado.estadisticasConjunto,
            estado.turnos,
            estado.usuarios
        );

        try {

            // 1. RECUPERAR MENSUALIDADES

            for (RegistroMensualidad pago :
                    estado.mensualidades) {

                if (pago == null) {

                    throw new IOException(
                        "Mensualidad nula en el archivo"
                    );
                }

                sistema.getReportesFinancieros()
                    .registrarMensualidad(
                        pago.getSocio(),
                        pago.getPeriodoCorrespondiente(),
                        pago.getFechaPago(),
                        pago.getValorPagado()
                    );
            }

            // 2. RECUPERAR SUGERENCIAS

            sistema.getSugerencias()
                .restaurarHistorial(
                    estado.sugerencias
                );

            // 3. RECUPERAR HISTORIAL DE RANKINGS

            if (estado.historialRankings != null) {

                sistema.getReportesDeportivos()
                    .restaurarHistorialRankings(
                        estado.historialRankings
                    );
            }

            // 4. RECUPERAR ENTRENAMIENTOS

            for (SesionEntrenamiento sesion :
                    estado.sesiones) {

                if (sesion == null) {

                    throw new IOException(
                        "Sesion nula en el archivo"
                    );
                }

                sistema.getEntrenamientos()
                    .programarSesion(sesion);
            }

            // 5. RECUPERAR EQUIPOS

            if (estado.equipos != null) {

                for (Equipo equipo :
                        estado.equipos) {

                    sistema.getClub()
                        .registrarEquipo(equipo);
                }
            }

            // 6. RECUPERAR RESERVAS

            if (estado.reservas != null) {

                for (Reserva reserva :
                        estado.reservas) {

                    sistema.getClub()
                        .registrarReserva(reserva);
                }
            }

            // 7. RECUPERAR COMPETENCIAS

            if (estado.competencias != null) {

                for (Competencia competencia :
                        estado.competencias) {

                    sistema.getCompetencias()
                        .registrarCompetencia(
                            competencia
                        );
                }
            }

            // 8. RECUPERAR FASES ASCUN

            if (estado.fasesASCUN != null) {

                for (FaseASCUN fase :
                        estado.fasesASCUN) {

                    sistema.registrarFaseASCUN(fase);
                }
            }

            // 9. RECUPERAR INSCRIPCIONES OFICIALES

            Map<InscripcionCompetencia, InscripcionCompetencia>
                equivalencias = new IdentityHashMap<>();

            if (estado.inscripciones != null) {

                for (InscripcionCompetencia original :
                        estado.inscripciones) {

                    InscripcionCompetencia nueva =
                        sistema.getCompetencias()
                            .inscribirEquipo(
                                original.getEquipo(),
                                original.getCompetencia()
                            );

                    // RESTAURAR NOMINA

                    for (Socio socio :
                            original.getNomina()) {

                        nueva.agregarJugador(socio);
                    }

                    // CONSERVAR NOMINA OFICIAL

                    if (original.isOficial()) {

                        nueva.oficializarNomina();
                    }

                    equivalencias.put(
                        original,
                        nueva
                    );
                }
            }

            // 10. RECUPERAR PARTIDOS INTERNOS

            // CONSERVAMOS LOS OBJETOS ORIGINALES
            // PARA MANTENER SUS ESTADISTICAS.

            if (estado.internos != null) {

                for (PartidoInterno original :
                        estado.internos) {

                    sistema.getPartidosInternos()
                        .restaurarPartido(original);
                }
            }

            // 11. RECUPERAR PARTIDOS OFICIALES

            if (estado.oficiales != null) {

                for (PartidoCompetencia original :
                        estado.oficiales) {

                    // IDENTIFICAR INSCRIPCION RESTAURADA

                    InscripcionCompetencia nuevaInscripcion =
                        equivalencias.get(
                            original.getInscripcion()
                        );

                    if (nuevaInscripcion == null) {

                        throw new IOException(
                            "Partido oficial sin inscripcion recuperada"
                        );
                    }

                    // PROGRAMAR PARTIDO RESTAURADO

                    PartidoCompetencia nuevo =
                        sistema.getPartidosOficiales()
                            .programarPartido(
                                nuevaInscripcion,
                                original.getRival(),
                                original.getEscenario(),
                                original.getFecha(),
                                original.getFase()
                            );

                    // RESTAURAR ALINEACION

                    for (Socio socio :
                            original.getAlineacion()) {

                        nuevo.alinearJugador(socio);
                    }

                    // CONSERVAR ALINEACION CONFIRMADA

                    if (original.isAlineacionConfirmada()) {

                        nuevo.confirmarAlineacion();
                    }

                    // RECUPERAR RESULTADO

                    if (original.estaFinalizado()) {

                        nuevo.registrarResultado(
                            original.getMarcadorClub(),
                            original.getMarcadorRival()
                        );
                    }
                }
            }

            // LAS CUENTAS DE USUARIO YA QUEDARON
            // RECUPERADAS EN EL CONSTRUCTOR.

            // NO RECUPERAMOS SESIONES ABIERTAS.
            // CADA USUARIO DEBERA INICIAR SESION OTRA VEZ.

        } catch (ReglaNegocioException |
                 IllegalArgumentException e) {

            throw new IOException(
                "Datos guardados inconsistentes",
                e
            );
        }

        return sistema;
    }
}
