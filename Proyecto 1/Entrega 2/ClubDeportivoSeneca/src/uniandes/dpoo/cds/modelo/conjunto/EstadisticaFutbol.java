
package uniandes.dpoo.cds.modelo.conjunto;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class EstadisticaFutbol extends EstadisticaJugador {

    private static final long serialVersionUID = 1L;

    private final int goles;
    private final int asistencias;
    private final int amarillas;
    private final int rojas;

    public EstadisticaFutbol(
            PartidoInterno partido,
            Equipo equipo,
            Socio jugador,
            int goles,
            int asistencias,
            int amarillas,
            int rojas)
            throws ReglaNegocioException {

        super(partido, equipo, jugador);

        if (goles < 0 || asistencias < 0 ||
            amarillas < 0 || rojas < 0) {

            throw new ReglaNegocioException(
                "Las estadisticas no pueden ser negativas"
            );
        }

        this.goles = goles;
        this.asistencias = asistencias;
        this.amarillas = amarillas;
        this.rojas = rojas;
    }

    public int getGoles() {
        return goles;
    }

    public int getAsistencias() {
        return asistencias;
    }

    public int getAmarillas() {
        return amarillas;
    }

    public int getRojas() {
        return rojas;
    }
}
