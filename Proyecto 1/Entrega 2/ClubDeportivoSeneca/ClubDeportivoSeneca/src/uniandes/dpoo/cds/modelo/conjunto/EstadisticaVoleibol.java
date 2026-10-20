
package uniandes.dpoo.cds.modelo.conjunto;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class EstadisticaVoleibol extends EstadisticaJugador {

    private static final long serialVersionUID = 1L;

    private final int aces;
    private final int bloqueos;

    public EstadisticaVoleibol(
            PartidoInterno partido,
            Equipo equipo,
            Socio jugador,
            int aces,
            int bloqueos)
            throws ReglaNegocioException {

        super(partido, equipo, jugador);

        if (aces < 0 || bloqueos < 0) {
            throw new ReglaNegocioException(
                "Las estadisticas no pueden ser negativas"
            );
        }

        this.aces = aces;
        this.bloqueos = bloqueos;
    }

    public int getAces() {
        return aces;
    }

    public int getBloqueos() {
        return bloqueos;
    }
}
