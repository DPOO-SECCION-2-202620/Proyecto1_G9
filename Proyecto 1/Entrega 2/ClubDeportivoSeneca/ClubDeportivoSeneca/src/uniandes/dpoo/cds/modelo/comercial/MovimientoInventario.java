
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MovimientoInventario implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Tipo {
        REABASTECIMIENTO,
        TRASLADO,
        VENTA
    }

    private final LocalDateTime fecha;
    private final Tipo tipo;
    private final String codigoArticulo;
    private final UbicacionInventario origen;
    private final UbicacionInventario destino;
    private final int cantidad;
    private final String responsable;

    public MovimientoInventario(
            Tipo tipo,
            String codigoArticulo,
            UbicacionInventario origen,
            UbicacionInventario destino,
            int cantidad,
            String responsable) {

        if (tipo == null ||
            codigoArticulo == null ||
            codigoArticulo.isBlank() ||
            cantidad <= 0 ||
            responsable == null ||
            responsable.isBlank()) {

            throw new IllegalArgumentException(
                "Datos del movimiento invalidos"
            );
        }

        this.fecha = LocalDateTime.now();
        this.tipo = tipo;
        this.codigoArticulo = codigoArticulo;
        this.origen = origen;
        this.destino = destino;
        this.cantidad = cantidad;
        this.responsable = responsable;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getCodigoArticulo() {
        return codigoArticulo;
    }

    public UbicacionInventario getOrigen() {
        return origen;
    }

    public UbicacionInventario getDestino() {
        return destino;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getResponsable() {
        return responsable;
    }
}
