
package uniandes.dpoo.cds.modelo.individuales;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class EstadisticaTenisMesa extends EstadisticaPractica {

    private static final long serialVersionUID = 1L;

    private final int setsGanados;
    private final int puntosTotales;

    public EstadisticaTenisMesa(
            Socio jugador,
            int setsGanados,
            int puntosTotales) {

        super(jugador);

        if (setsGanados < 0 || puntosTotales < 0) {
            throw new IllegalArgumentException(
                "Las estadisticas no pueden ser negativas"
            );
        }

        this.setsGanados = setsGanados;
        this.puntosTotales = puntosTotales;
    }

    public int getSetsGanados() {
        return setsGanados;
    }

    public int getPuntosTotales() {
        return puntosTotales;
    }
}
