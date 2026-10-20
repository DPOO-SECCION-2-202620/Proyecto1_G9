
package uniandes.dpoo.cds.modelo.competencias;

import java.io.Serializable;

public class Escenario implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;

    public Escenario(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                "El escenario es obligatorio"
            );
        }

        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }
}
