
package uniandes.dpoo.cds.modelo.comercial;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Snack extends ProductoCafeteria {

    private static final long serialVersionUID = 1L;

    private final Set<Alergeno> alergenos;

    public Snack(
            String codigo,
            String nombre,
            double precio,
            Set<Alergeno> alergenos) {

        super(codigo, nombre, precio);

        if (alergenos == null) {
            throw new IllegalArgumentException(
                "La lista de alergenos es obligatoria"
            );
        }

        for (Alergeno alergeno : alergenos) {

            if (alergeno == null) {
                throw new IllegalArgumentException(
                    "No se permiten alergenos nulos"
                );
            }
        }

        this.alergenos = alergenos.isEmpty()
            ? EnumSet.noneOf(Alergeno.class)
            : EnumSet.copyOf(alergenos);
    }

    public Set<Alergeno> getAlergenos() {
        return Collections.unmodifiableSet(alergenos);
    }
}
