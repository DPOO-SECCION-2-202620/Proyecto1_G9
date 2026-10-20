
package uniandes.dpoo.cds.modelo.competencias;

import java.io.Serializable;

public class InstitucionRival implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nombre;

    public InstitucionRival(String nombre) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                "El nombre de la institucion es obligatorio"
            );
        }

        this.nombre = nombre.trim();
    }

    public String getNombre() {
        return nombre;
    }
}
