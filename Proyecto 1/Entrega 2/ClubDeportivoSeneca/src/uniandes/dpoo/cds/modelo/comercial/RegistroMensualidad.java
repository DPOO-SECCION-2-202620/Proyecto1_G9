
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class RegistroMensualidad implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Socio socio;
    private final YearMonth periodoCorrespondiente;
    private final LocalDate fechaPago;
    private final double valorPagado;

    public RegistroMensualidad(
            Socio socio,
            YearMonth periodoCorrespondiente,
            LocalDate fechaPago,
            double valorPagado) {

        if (socio == null ||
            socio.getLogin() == null ||
            socio.getLogin().isBlank() ||
            periodoCorrespondiente == null ||
            fechaPago == null ||
            !Double.isFinite(valorPagado) ||
            valorPagado <= 0) {

            throw new IllegalArgumentException(
                "Datos de mensualidad invalidos"
            );
        }

        this.socio = socio;
        this.periodoCorrespondiente = periodoCorrespondiente;
        this.fechaPago = fechaPago;
        this.valorPagado = valorPagado;
    }

    public Socio getSocio() {
        return socio;
    }

    public YearMonth getPeriodoCorrespondiente() {
        return periodoCorrespondiente;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public double getValorPagado() {
        return valorPagado;
    }
}
