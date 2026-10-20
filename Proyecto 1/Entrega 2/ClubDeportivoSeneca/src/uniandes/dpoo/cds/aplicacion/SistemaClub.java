
package uniandes.dpoo.cds.aplicacion;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class SistemaClub {

    // MODULOS PRINCIPALES

    private final ClubDeportivoSeneca club;

    private final GestorCompetencias competencias;
    private final GestorPartidosCompetencia partidosOficiales;
    private final GestorPartidosInternos partidosInternos;
    private final GestorEstadisticas estadisticas;
    private final GestorPartidosPractica partidosIndividuales;
    private final GestorEntrenamientos entrenamientos;

    // MODULOS COMERCIALES

    private final GestorFidelidad fidelidad;
    private final GestorTienda tienda;
    private final GestorCafeteria cafeteria;
    private final GestorSugerenciasProducto sugerencias;

    // REPORTES Y ADMINISTRACION

    private final GestorReportes reportesFinancieros;
    private final GestorReportesDeportivos reportesDeportivos;
    private final GestorTurnos turnos;
    private final TablaPosicionesLiga tablaLiga;

    // NUEVO: GESTOR CENTRAL DE USUARIOS

    private final GestorUsuarios usuarios;

    // TORNEOS ASCUN

    private final List<FaseASCUN> fasesASCUN =
        new ArrayList<>();

    // CONSTRUCTOR DE SISTEMA NUEVO

    public SistemaClub(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion) {

        this(
            apertura,
            cierre,
            diasAtencion,
            new GestorFidelidad(),
            null,
            null
        );
    }

    // CONSTRUCTOR PARA RECUPERAR FIDELIDAD

    public SistemaClub(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion,
            GestorFidelidad fidelidadRecuperada) {

        this(
            apertura,
            cierre,
            diasAtencion,
            fidelidadRecuperada,
            null,
            null
        );
    }

    // CONSTRUCTOR PARA RECUPERAR TIENDA Y CAFETERIA

    public SistemaClub(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion,
            GestorFidelidad fidelidadRecuperada,
            GestorTienda tiendaRecuperada,
            GestorCafeteria cafeteriaRecuperada) {

        this(
            apertura,
            cierre,
            diasAtencion,
            fidelidadRecuperada,
            tiendaRecuperada,
            cafeteriaRecuperada,
            null
        );
    }

    // CONSTRUCTOR PARA RECUPERAR INDIVIDUALES

    public SistemaClub(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion,
            GestorFidelidad fidelidadRecuperada,
            GestorTienda tiendaRecuperada,
            GestorCafeteria cafeteriaRecuperada,
            GestorPartidosPractica individualesRecuperados) {

        this(
            apertura,
            cierre,
            diasAtencion,
            fidelidadRecuperada,
            tiendaRecuperada,
            cafeteriaRecuperada,
            individualesRecuperados,
            null
        );
    }

    // CONSTRUCTOR PARA RECUPERAR ESTADISTICAS

    public SistemaClub(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion,
            GestorFidelidad fidelidadRecuperada,
            GestorTienda tiendaRecuperada,
            GestorCafeteria cafeteriaRecuperada,
            GestorPartidosPractica individualesRecuperados,
            GestorEstadisticas estadisticasRecuperadas) {

        this(
            apertura,
            cierre,
            diasAtencion,
            fidelidadRecuperada,
            tiendaRecuperada,
            cafeteriaRecuperada,
            individualesRecuperados,
            estadisticasRecuperadas,
            null
        );
    }

    // CONSTRUCTOR PARA RECUPERAR TURNOS

    public SistemaClub(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion,
            GestorFidelidad fidelidadRecuperada,
            GestorTienda tiendaRecuperada,
            GestorCafeteria cafeteriaRecuperada,
            GestorPartidosPractica individualesRecuperados,
            GestorEstadisticas estadisticasRecuperadas,
            GestorTurnos turnosRecuperados) {

        this(
            apertura,
            cierre,
            diasAtencion,
            fidelidadRecuperada,
            tiendaRecuperada,
            cafeteriaRecuperada,
            individualesRecuperados,
            estadisticasRecuperadas,
            turnosRecuperados,
            null
        );
    }

    // NUEVO CONSTRUCTOR COMPLETO:
    // TAMBIEN PERMITE RECUPERAR LAS CUENTAS

    public SistemaClub(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion,
            GestorFidelidad fidelidadRecuperada,
            GestorTienda tiendaRecuperada,
            GestorCafeteria cafeteriaRecuperada,
            GestorPartidosPractica individualesRecuperados,
            GestorEstadisticas estadisticasRecuperadas,
            GestorTurnos turnosRecuperados,
            GestorUsuarios usuariosRecuperados) {

        // VALIDAR FIDELIDAD

        if (fidelidadRecuperada == null) {

            throw new IllegalArgumentException(
                "El gestor de fidelidad es obligatorio"
            );
        }

        // VALIDAR GESTORES COMERCIALES

        if ((tiendaRecuperada == null) !=
            (cafeteriaRecuperada == null)) {

            throw new IllegalArgumentException(
                "Tienda y cafeteria deben recuperarse juntas"
            );
        }

        // CREAR CLUB PRINCIPAL

        club = new ClubDeportivoSeneca();

        // CREAR GESTORES DEPORTIVOS

        competencias =
            new GestorCompetencias(club);

        partidosOficiales =
            new GestorPartidosCompetencia(competencias);

        partidosInternos =
            new GestorPartidosInternos(club);

        // ESTADISTICAS RECUPERADAS O NUEVAS

        estadisticas =
            estadisticasRecuperadas == null
                ? new GestorEstadisticas()
                : estadisticasRecuperadas;

        // INDIVIDUALES RECUPERADOS O NUEVOS

        partidosIndividuales =
            individualesRecuperados == null
                ? new GestorPartidosPractica()
                : individualesRecuperados;

        entrenamientos =
            new GestorEntrenamientos();

        // RECUPERAR FIDELIDAD

        fidelidad = fidelidadRecuperada;

        // RECUPERAR TIENDA Y CAFETERIA

        tienda = tiendaRecuperada == null
            ? new GestorTienda(fidelidad)
            : tiendaRecuperada;

        cafeteria = cafeteriaRecuperada == null
            ? new GestorCafeteria(fidelidad)
            : cafeteriaRecuperada;

        // CONECTAR GESTORES DEPENDIENTES

        sugerencias =
            new GestorSugerenciasProducto(
                tienda,
                cafeteria
            );

        reportesFinancieros =
            new GestorReportes(
                tienda,
                cafeteria
            );

        reportesDeportivos =
            new GestorReportesDeportivos(
                entrenamientos,
                partidosInternos,
                partidosOficiales,
                partidosIndividuales
            );

        // VALIDAR CONFIGURACION DE TURNOS

        if (turnosRecuperados != null &&
            (!turnosRecuperados.getApertura().equals(apertura) ||
             !turnosRecuperados.getCierre().equals(cierre) ||
             !turnosRecuperados.getDiasAtencion()
                 .equals(diasAtencion))) {

            throw new IllegalArgumentException(
                "La configuracion de los turnos recuperados "
                + "no coincide con el club"
            );
        }

        turnos = turnosRecuperados == null
            ? new GestorTurnos(
                apertura,
                cierre,
                diasAtencion
            )
            : turnosRecuperados;

        tablaLiga =
            new TablaPosicionesLiga(
                competencias,
                partidosOficiales
            );

        // NUEVO: RECUPERAR O CREAR USUARIOS

        usuarios = usuariosRecuperados == null
            ? new GestorUsuarios()
            : usuariosRecuperados;
    }

    // REGISTRAR FASE ASCUN

    public void registrarFaseASCUN(FaseASCUN fase)
            throws ReglaNegocioException {

        if (fase == null ||
            !competencias.getCompetencias()
                .contains(fase.getCompetencia())) {

            throw new ReglaNegocioException(
                "La fase ASCUN necesita una competencia registrada"
            );
        }

        for (FaseASCUN registrada : fasesASCUN) {

            if (registrada.getCompetencia() ==
                fase.getCompetencia()) {

                throw new ReglaNegocioException(
                    "La competencia ya tiene una fase ASCUN"
                );
            }
        }

        fasesASCUN.add(fase);
    }

    // CONSULTAR FASES ASCUN

    public List<FaseASCUN> getFasesASCUN() {

        return Collections.unmodifiableList(
            fasesASCUN
        );
    }

    // GETTERS

    public ClubDeportivoSeneca getClub() {
        return club;
    }

    public GestorCompetencias getCompetencias() {
        return competencias;
    }

    public GestorPartidosCompetencia getPartidosOficiales() {
        return partidosOficiales;
    }

    public GestorPartidosInternos getPartidosInternos() {
        return partidosInternos;
    }

    public GestorEstadisticas getEstadisticas() {
        return estadisticas;
    }

    public GestorPartidosPractica getPartidosIndividuales() {
        return partidosIndividuales;
    }

    public GestorEntrenamientos getEntrenamientos() {
        return entrenamientos;
    }

    public GestorFidelidad getFidelidad() {
        return fidelidad;
    }

    public GestorTienda getTienda() {
        return tienda;
    }

    public GestorCafeteria getCafeteria() {
        return cafeteria;
    }

    public GestorSugerenciasProducto getSugerencias() {
        return sugerencias;
    }

    public GestorReportes getReportesFinancieros() {
        return reportesFinancieros;
    }

    public GestorReportesDeportivos getReportesDeportivos() {
        return reportesDeportivos;
    }

    public GestorTurnos getTurnos() {
        return turnos;
    }

    public TablaPosicionesLiga getTablaLiga() {
        return tablaLiga;
    }

    // NUEVO: ACCESO AL GESTOR DE CUENTAS

    public GestorUsuarios getUsuarios() {
        return usuarios;
    }
}
