
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;

public abstract class ProductoCafeteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String nombre;
    private final double precio;

    protected ProductoCafeteria(
            String codigo,
            String nombre,
            double precio) {

        if (codigo == null || codigo.isBlank() ||
            nombre == null || nombre.isBlank() ||
            !Double.isFinite(precio) || precio <= 0) {

            throw new IllegalArgumentException(
                "Producto de cafeteria invalido"
            );
        }

        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.precio = precio;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }
}
