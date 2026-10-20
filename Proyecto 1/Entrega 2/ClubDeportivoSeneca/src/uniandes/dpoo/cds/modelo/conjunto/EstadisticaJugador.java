
package uniandes.dpoo.cds.modelo.conjunto;

import java.io.Serializable;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public abstract class EstadisticaJugador implements Serializable {

    private static final long serialVersionUID = 1L;

    // ATRIBUTOS

    private final PartidoInterno partido;
    private final Equipo equipo;
    private final Socio jugador;

    // CONSTRUCTOR

    protected EstadisticaJugador(
            PartidoInterno partido,
            Equipo equipo,
            Socio jugador) {

        this.partido = partido;
        this.equipo = equipo;
        this.jugador = jugador;
    }

    // METODOS GETTERS

    public PartidoInterno getPartido() {
        return partido;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public Socio getJugador() {
        return jugador;
    }
}
