
package uniandes.dpoo.cds.modelo.conjunto;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class EstadisticaBaloncesto extends EstadisticaJugador {

    private static final long serialVersionUID = 1L;

    private final int puntos;
    private final int rebotes;

    public EstadisticaBaloncesto(
            PartidoInterno partido,
            Equipo equipo,
            Socio jugador,
            int puntos,
            int rebotes)
            throws ReglaNegocioException {

        super(partido, equipo, jugador);

        if (puntos < 0 || rebotes < 0) {
            throw new ReglaNegocioException(
                "Las estadisticas no pueden ser negativas"
            );
        }

        this.puntos = puntos;
        this.rebotes = rebotes;
    }

    public int getPuntos() {
        return puntos;
    }

    public int getRebotes() {
        return rebotes;
    }
}
