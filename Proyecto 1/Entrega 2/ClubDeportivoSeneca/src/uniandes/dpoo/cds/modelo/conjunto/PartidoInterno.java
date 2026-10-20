
package uniandes.dpoo.cds.modelo.conjunto;

import java.io.Serializable;
import java.time.LocalDate;
import java.text.Normalizer;
import java.util.Locale;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class PartidoInterno implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final Equipo equipoLocal;
    private final Equipo equipoVisitante;
    private Integer marcadorLocal;
    private Integer marcadorVisitante;

    public PartidoInterno(LocalDate fecha, Equipo equipoLocal,
                          Equipo equipoVisitante) {
        this.fecha = fecha;
        this.equipoLocal = equipoLocal;
        this.equipoVisitante = equipoVisitante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Equipo getEquipoLocal() {
        return equipoLocal;
    }

    public Equipo getEquipoVisitante() {
        return equipoVisitante;
    }

    public Integer getMarcadorLocal() {
        return marcadorLocal;
    }

    public Integer getMarcadorVisitante() {
        return marcadorVisitante;
    }

    public boolean estaFinalizado() {
        return marcadorLocal != null && marcadorVisitante != null;
    }

    public void registrarResultado(int puntosLocal, int puntosVisitante)
            throws ReglaNegocioException {

        if (estaFinalizado()) {
            throw new ReglaNegocioException(
                "El partido ya tiene un resultado registrado"
            );
        }

        if (puntosLocal < 0 || puntosVisitante < 0) {
            throw new ReglaNegocioException(
                "El marcador no puede tener valores negativos"
            );
        }

        String deporte = Normalizer.normalize(
            equipoLocal.getDeporte().getNombre(),
            Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);

        boolean sinEmpates = deporte.equals("baloncesto") ||
            deporte.equals("basket") ||
            deporte.equals("basquetbol") ||
            deporte.equals("voleibol") ||
            deporte.equals("volleyball") ||
            deporte.equals("volley");

        if (sinEmpates && puntosLocal == puntosVisitante) {
            throw new ReglaNegocioException(
                "Este deporte no permite empates"
            );
        }

        this.marcadorLocal = puntosLocal;
        this.marcadorVisitante = puntosVisitante;
    }
}
