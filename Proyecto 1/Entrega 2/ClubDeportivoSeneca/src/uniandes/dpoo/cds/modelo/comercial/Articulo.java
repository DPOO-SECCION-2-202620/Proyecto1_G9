
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;

public class Articulo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String nombre;
    private final String categoria;
    private final double precio;

    public Articulo(String codigo, String nombre,
                    String categoria, double precio) {

        if (codigo == null || codigo.isBlank()
                || nombre == null || nombre.isBlank()
                || categoria == null || categoria.isBlank()
                || !Double.isFinite(precio) || precio <= 0) {

            throw new IllegalArgumentException(
                "Datos del articulo invalidos"
            );
        }

        this.codigo = codigo.trim();
        this.nombre = nombre.trim();
        this.categoria = categoria.trim();
        this.precio = precio;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public double getPrecio() {
        return precio;
    }
}
