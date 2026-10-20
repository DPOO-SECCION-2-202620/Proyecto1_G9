
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class Inventario implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Articulo articulo;
    private int cantidad;

    public Inventario(Articulo articulo, int cantidadInicial) {

        if (articulo == null || cantidadInicial < 0) {
            throw new IllegalArgumentException(
                "Inventario inicial invalido"
            );
        }

        this.articulo = articulo;
        this.cantidad = cantidadInicial;
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void descontar(int unidades)
            throws ReglaNegocioException {

        if (unidades <= 0 || unidades > cantidad) {
            throw new ReglaNegocioException(
                "Inventario insuficiente o cantidad invalida"
            );
        }

        cantidad -= unidades;
    }

    public void reabastecer(int unidades)
            throws ReglaNegocioException {

        if (unidades <= 0 ||
            unidades > Integer.MAX_VALUE - cantidad) {

            throw new ReglaNegocioException(
                "Cantidad de reabastecimiento invalida"
            );
        }

        cantidad += unidades;
    }
}
