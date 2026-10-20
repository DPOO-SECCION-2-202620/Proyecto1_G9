
package uniandes.dpoo.cds.modelo.comercial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.modelo.usuarios.Usuario;

public class VentaTienda extends Venta {

    private static final long serialVersionUID = 1L;

    private final List<DetalleVenta> detalles;

    public VentaTienda(
            Usuario comprador,
            List<DetalleVenta> detalles,
            double subtotal,
            double descuento,
            double impuesto,
            double puntosRedimidos,
            double totalPagado) {

        super(
            comprador,
            subtotal,
            descuento,
            impuesto,
            puntosRedimidos,
            totalPagado
        );

        this.detalles = new ArrayList<>(detalles);
    }

    public List<DetalleVenta> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }
}
