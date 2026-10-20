
package uniandes.dpoo.cds.modelo.individuales;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class EstadisticaRaqueta extends EstadisticaPractica {

    private static final long serialVersionUID = 1L;

    private final int setsGanados;
    private final int gamesGanados;
    private final int aces;
    private final int erroresNoForzados;

    public EstadisticaRaqueta(
            Socio jugador,
            int setsGanados,
            int gamesGanados,
            int aces,
            int erroresNoForzados) {

        super(jugador);

        if (setsGanados < 0 ||
            gamesGanados < 0 ||
            aces < 0 ||
            erroresNoForzados < 0) {

            throw new IllegalArgumentException(
                "Las estadisticas no pueden ser negativas"
            );
        }

        this.setsGanados = setsGanados;
        this.gamesGanados = gamesGanados;
        this.aces = aces;
        this.erroresNoForzados = erroresNoForzados;
    }

    public int getSetsGanados() {
        return setsGanados;
    }

    public int getGamesGanados() {
        return gamesGanados;
    }

    public int getAces() {
        return aces;
    }

    public int getErroresNoForzados() {
        return erroresNoForzados;
    }
}
