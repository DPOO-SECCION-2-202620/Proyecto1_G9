
package uniandes.dpoo.cds.modelo.individuales;

import java.io.Serializable;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public abstract class EstadisticaPractica implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Socio jugador;

    protected EstadisticaPractica(Socio jugador) {

        if (jugador == null || jugador.getLogin() == null) {
            throw new IllegalArgumentException(
                "El jugador es obligatorio"
            );
        }

        this.jugador = jugador;
    }

    public Socio getJugador() {
        return jugador;
    }
}
