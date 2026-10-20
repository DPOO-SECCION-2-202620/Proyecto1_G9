
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.PuntosFidelidad;
import uniandes.dpoo.cds.modelo.usuarios.Usuario;

public class GestorFidelidad implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double PORCENTAJE = 0.02;

    private final Map<String, PuntosFidelidad> cuentas =
        new LinkedHashMap<>();

    private static String clave(Usuario usuario) {

        return usuario == null || usuario.getLogin() == null
            ? ""
            : usuario.getLogin().trim().toLowerCase(Locale.ROOT);
    }

    // CONSULTAR EL SALDO UNIFICADO

    public double getSaldo(Usuario usuario) {

        PuntosFidelidad cuenta =
            cuentas.get(clave(usuario));

        return cuenta == null ? 0 : cuenta.getSaldo();
    }

    // VALIDAR LOS PUNTOS ANTES DE UNA COMPRA

    public void validarRedencion(
            Usuario usuario,
            int puntos,
            double totalAntesPuntos)
            throws ReglaNegocioException {

        if (clave(usuario).isEmpty() ||
            puntos < 0 ||
            !Double.isFinite(totalAntesPuntos) ||
            totalAntesPuntos < 0 ||
            puntos > getSaldo(usuario) ||
            puntos > totalAntesPuntos) {

            throw new ReglaNegocioException(
                "Puntos insuficientes o superiores al valor de la venta"
            );
        }
    }

    // REDIMIR PUNTOS Y ACUMULAR LOS NUEVOS

    public void aplicarCompra(
            Usuario usuario,
            int puntosRedimidos,
            double totalPagado)
            throws ReglaNegocioException {

        validarRedencion(
            usuario,
            puntosRedimidos,
            totalPagado + puntosRedimidos
        );

        PuntosFidelidad cuenta =
            cuentas.computeIfAbsent(
                clave(usuario),
                clave -> new PuntosFidelidad()
            );

        cuenta.redimir(puntosRedimidos);

        cuenta.acumular(
            Math.round(
                totalPagado * PORCENTAJE * 100.0
            ) / 100.0
        );
    }
}
