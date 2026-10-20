
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;

public class DetalleVenta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String nombre;
    private final int cantidad;
    private final double precioUnitario;

    public DetalleVenta(Articulo articulo, int cantidad) {

        if (articulo == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                "Detalle de venta invalido"
            );
        }

        this.codigo = articulo.getCodigo();
        this.nombre = articulo.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = articulo.getPrecio();
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double getSubtotal() {
        return Math.round(
            precioUnitario * cantidad * 100.0
        ) / 100.0;
    }
}
