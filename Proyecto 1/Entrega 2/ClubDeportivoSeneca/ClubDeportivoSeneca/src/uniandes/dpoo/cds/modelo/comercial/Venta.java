
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;
import java.time.LocalDate;
import uniandes.dpoo.cds.modelo.usuarios.Usuario;

public abstract class Venta implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final Usuario comprador;

    private final double subtotal;
    private final double descuento;
    private final double impuesto;
    private final double puntosRedimidos;
    private final double totalPagado;

    protected Venta(
            Usuario comprador,
            double subtotal,
            double descuento,
            double impuesto,
            double puntosRedimidos,
            double totalPagado) {

        this.fecha = LocalDate.now();
        this.comprador = comprador;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.impuesto = impuesto;
        this.puntosRedimidos = puntosRedimidos;
        this.totalPagado = totalPagado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Usuario getComprador() {
        return comprador;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDescuento() {
        return descuento;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public double getPuntosRedimidos() {
        return puntosRedimidos;
    }

    public double getTotalPagado() {
        return totalPagado;
    }
}
