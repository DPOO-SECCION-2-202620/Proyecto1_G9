
package uniandes.dpoo.cds.modelo.comercial;

import java.time.LocalDate;

public class ResumenFinanciero {

    private final LocalDate inicio;
    private final LocalDate fin;

    private final int ventasTienda;
    private final int ventasCafeteria;
    private final int pagosMensualidad;

    private final double recaudoTienda;
    private final double recaudoCafeteria;
    private final double recaudoMensualidades;

    public ResumenFinanciero(
            LocalDate inicio,
            LocalDate fin,
            int ventasTienda,
            int ventasCafeteria,
            int pagosMensualidad,
            double recaudoTienda,
            double recaudoCafeteria,
            double recaudoMensualidades) {

        this.inicio = inicio;
        this.fin = fin;

        this.ventasTienda = ventasTienda;
        this.ventasCafeteria = ventasCafeteria;
        this.pagosMensualidad = pagosMensualidad;

        this.recaudoTienda = redondear(recaudoTienda);
        this.recaudoCafeteria = redondear(recaudoCafeteria);
        this.recaudoMensualidades =
            redondear(recaudoMensualidades);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    public int getVentasTienda() {
        return ventasTienda;
    }

    public int getVentasCafeteria() {
        return ventasCafeteria;
    }

    public int getPagosMensualidad() {
        return pagosMensualidad;
    }

    public int getNumeroOperaciones() {
        return ventasTienda
            + ventasCafeteria
            + pagosMensualidad;
    }

    public double getRecaudoTienda() {
        return recaudoTienda;
    }

    public double getRecaudoCafeteria() {
        return recaudoCafeteria;
    }

    public double getRecaudoMensualidades() {
        return recaudoMensualidades;
    }

    public double getRecaudoTotal() {
        return redondear(
            recaudoTienda
            + recaudoCafeteria
            + recaudoMensualidades
        );
    }
}
