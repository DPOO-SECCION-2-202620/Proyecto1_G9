
package uniandes.dpoo.cds.modelo.competencias;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PartidoCompetencia implements Serializable {

    private static final long serialVersionUID = 1L;

    // ATRIBUTOS

    private final InscripcionCompetencia inscripcion;
    private final InstitucionRival rival;
    private final Escenario escenario;
    private final LocalDate fecha;
    private final String fase;

    private final List<Socio> alineacion = new ArrayList<>();

    private boolean alineacionConfirmada;
    private Integer marcadorClub;
    private Integer marcadorRival;

    // CONSTRUCTOR

    public PartidoCompetencia(
            InscripcionCompetencia inscripcion,
            InstitucionRival rival,
            Escenario escenario,
            LocalDate fecha,
            String fase) {

        if (inscripcion == null ||
            !inscripcion.isOficial() ||
            rival == null ||
            escenario == null ||
            fecha == null ||
            fase == null ||
            fase.isBlank()) {

            throw new IllegalArgumentException(
                "Datos del partido oficial invalidos"
            );
        }

        this.inscripcion = inscripcion;
        this.rival = rival;
        this.escenario = escenario;
        this.fecha = fecha;
        this.fase = fase.trim();
    }

    // GETTERS

    public InscripcionCompetencia getInscripcion() {
        return inscripcion;
    }

    public InstitucionRival getRival() {
        return rival;
    }

    public Escenario getEscenario() {
        return escenario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getFase() {
        return fase;
    }

    public List<Socio> getAlineacion() {
        return Collections.unmodifiableList(alineacion);
    }

    public boolean isAlineacionConfirmada() {
        return alineacionConfirmada;
    }

    public Integer getMarcadorClub() {
        return marcadorClub;
    }

    public Integer getMarcadorRival() {
        return marcadorRival;
    }

    public boolean estaFinalizado() {
        return marcadorClub != null &&
               marcadorRival != null;
    }

    // ALINEAR JUGADORES

    public void alinearJugador(Socio jugador)
            throws ReglaNegocioException {

        if (alineacionConfirmada || estaFinalizado()) {
            throw new ReglaNegocioException(
                "La alineacion ya esta cerrada"
            );
        }

        if (!inscripcion.jugadorHabilitado(jugador)) {
            throw new ReglaNegocioException(
                "El jugador no esta en la nomina oficial"
            );
        }

        for (Socio anterior : alineacion) {

            if (anterior.getLogin()
                    .equals(jugador.getLogin())) {

                throw new ReglaNegocioException(
                    "Jugador repetido en la alineacion"
                );
            }
        }

        alineacion.add(jugador);
    }

    // CONFIRMAR ALINEACION

    public void confirmarAlineacion()
            throws ReglaNegocioException {

        if (alineacionConfirmada) {
            throw new ReglaNegocioException(
                "La alineacion ya fue confirmada"
            );
        }

        int minimo = inscripcion
            .getCompetencia()
            .getDeporte()
            .getMinimoJugadores();

        if (alineacion.size() < minimo) {
            throw new ReglaNegocioException(
                "No hay suficientes jugadores alineados"
            );
        }

        alineacionConfirmada = true;
    }

    // REGISTRAR RESULTADO

    public void registrarResultado(int club, int rival)
            throws ReglaNegocioException {

        if (!alineacionConfirmada) {
            throw new ReglaNegocioException(
                "Confirme primero la alineacion"
            );
        }

        if (estaFinalizado()) {
            throw new ReglaNegocioException(
                "El partido ya tiene resultado"
            );
        }

        if (club < 0 || rival < 0) {
            throw new ReglaNegocioException(
                "El resultado no puede ser negativo"
            );
        }

        String deporte = normalizar(
            inscripcion.getCompetencia()
                       .getDeporte().getNombre()
        );

        if ((deporte.equals("baloncesto") ||
             deporte.equals("basket") ||
             deporte.equals("voleibol") ||
             deporte.equals("volleyball") ||
             deporte.equals("volley")) &&
             club == rival) {

            throw new ReglaNegocioException(
                "Este deporte no permite empates"
            );
        }

        this.marcadorClub = club;
        this.marcadorRival = rival;
    }

    // CALCULAR PUNTOS DE LIGA DE FUTBOL

    public int getPuntosLigaFutbol()
            throws ReglaNegocioException {

        String deporte = normalizar(
            inscripcion.getCompetencia()
                       .getDeporte().getNombre()
        );

        String formato = normalizar(
            inscripcion.getCompetencia().getFormato()
        );

        if (!estaFinalizado() ||
            !deporte.equals("futbol") ||
            !formato.equals("liga")) {

            throw new ReglaNegocioException(
                "Puntos de liga no disponibles para este partido"
            );
        }

        if (marcadorClub > marcadorRival) {
            return 3;
        }

        if (marcadorClub.equals(marcadorRival)) {
            return 1;
        }

        return 0;
    }

    // METODO AUXILIAR

    private static String normalizar(String valor) {

        return Normalizer.normalize(
            valor, Normalizer.Form.NFD
        )
        .replaceAll("\\p{M}", "")
        .trim()
        .toLowerCase(Locale.ROOT);
    }
}
