
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;

public class DetalleVentaCafeteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String codigo;
    private final String nombre;
    private final int cantidad;
    private final double precioUnitario;

    public DetalleVentaCafeteria(
            ProductoCafeteria producto,
            int cantidad) {

        if (producto == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                "Detalle de venta invalido"
            );
        }

        this.codigo = producto.getCodigo();
        this.nombre = producto.getNombre();
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
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
