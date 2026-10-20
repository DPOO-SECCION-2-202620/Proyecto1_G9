
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class PuntosFidelidad implements Serializable {

    private static final long serialVersionUID = 1L;

    private double saldo;

    public double getSaldo() {
        return saldo;
    }

    public void acumular(double puntos) {

        if (!Double.isFinite(puntos) || puntos < 0) {
            throw new IllegalArgumentException(
                "Puntos invalidos"
            );
        }

        saldo = Math.round(
            (saldo + puntos) * 100.0
        ) / 100.0;
    }

    public void redimir(int puntos)
            throws ReglaNegocioException {

        if (puntos < 0 || puntos > saldo) {
            throw new ReglaNegocioException(
                "Puntos insuficientes o invalidos"
            );
        }

        saldo = Math.round(
            (saldo - puntos) * 100.0
        ) / 100.0;
    }
}
