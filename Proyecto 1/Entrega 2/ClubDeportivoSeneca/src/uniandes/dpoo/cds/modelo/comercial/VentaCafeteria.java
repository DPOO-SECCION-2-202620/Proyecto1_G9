
package uniandes.dpoo.cds.modelo.comercial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class VentaCafeteria extends Venta {

    private static final long serialVersionUID = 1L;

    private final List<DetalleVentaCafeteria> detalles;
    private final double propina;

    public VentaCafeteria(
            Socio comprador,
            List<DetalleVentaCafeteria> detalles,
            double subtotal,
            double impuesto,
            double propina,
            double puntosRedimidos,
            double totalPagado) {

        super(
            comprador,
            subtotal,
            0,
            impuesto,
            puntosRedimidos,
            totalPagado
        );

        this.detalles = new ArrayList<>(detalles);
        this.propina = propina;
    }

    public List<DetalleVentaCafeteria> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public double getPropina() {
        return propina;
    }
}
