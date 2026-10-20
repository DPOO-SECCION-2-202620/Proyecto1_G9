
package uniandes.dpoo.cds.aplicacion;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.RegistroMensualidad;
import uniandes.dpoo.cds.modelo.comercial.ResumenFinanciero;
import uniandes.dpoo.cds.modelo.comercial.VentaCafeteria;
import uniandes.dpoo.cds.modelo.comercial.VentaTienda;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class GestorReportes {

    // ATRIBUTOS

    private final GestorTienda tienda;
    private final GestorCafeteria cafeteria;

    private final List<RegistroMensualidad> mensualidades =
        new ArrayList<>();

    // CONSTRUCTOR

    public GestorReportes(
            GestorTienda tienda,
            GestorCafeteria cafeteria) {

        if (tienda == null || cafeteria == null) {
            throw new IllegalArgumentException(
                "Se requieren los gestores de tienda y cafeteria"
            );
        }

        this.tienda = tienda;
        this.cafeteria = cafeteria;
    }

    // REGISTRAR MENSUALIDAD

    public RegistroMensualidad registrarMensualidad(
            Socio socio,
            YearMonth periodoCorrespondiente,
            LocalDate fechaPago,
            double valorPagado)
            throws ReglaNegocioException {

        if (socio == null ||
            socio.getLogin() == null ||
            socio.getLogin().isBlank() ||
            periodoCorrespondiente == null ||
            fechaPago == null ||
            !Double.isFinite(valorPagado) ||
            valorPagado <= 0) {

            throw new ReglaNegocioException(
                "Datos de mensualidad invalidos"
            );
        }

        // IMPEDIR PAGOS DUPLICADOS
        // DEL MISMO SOCIO Y MES

        for (RegistroMensualidad anterior : mensualidades) {

            if (anterior.getSocio().getLogin()
                    .equalsIgnoreCase(socio.getLogin()) &&
                anterior.getPeriodoCorrespondiente()
                    .equals(periodoCorrespondiente)) {

                throw new ReglaNegocioException(
                    "La mensualidad de este periodo ya fue registrada"
                );
            }
        }

        RegistroMensualidad nuevo =
            new RegistroMensualidad(
                socio,
                periodoCorrespondiente,
                fechaPago,
                valorPagado
            );

        mensualidades.add(nuevo);

        return nuevo;
    }

    // CONSULTAR HISTORIAL DE MENSUALIDADES

    public List<RegistroMensualidad> getMensualidades() {
        return Collections.unmodifiableList(mensualidades);
    }

    // REPORTE DIARIO

    public ResumenFinanciero reporteDiario(
            LocalDate fecha)
            throws ReglaNegocioException {

        if (fecha == null) {
            throw new ReglaNegocioException(
                "La fecha es obligatoria"
            );
        }

        return reporteEntre(fecha, fecha);
    }

    // REPORTE SEMANAL (LUNES A DOMINGO)

    public ResumenFinanciero reporteSemanal(
            LocalDate fecha)
            throws ReglaNegocioException {

        if (fecha == null) {
            throw new ReglaNegocioException(
                "La fecha es obligatoria"
            );
        }

        LocalDate lunes = fecha.minusDays(
            fecha.getDayOfWeek().getValue() - 1
        );

        return reporteEntre(
            lunes,
            lunes.plusDays(6)
        );
    }

    // REPORTE MENSUAL

    public ResumenFinanciero reporteMensual(
            YearMonth periodo)
            throws ReglaNegocioException {

        if (periodo == null) {
            throw new ReglaNegocioException(
                "El mes es obligatorio"
            );
        }

        return reporteEntre(
            periodo.atDay(1),
            periodo.atEndOfMonth()
        );
    }

    // REPORTE CON FECHAS PERSONALIZADAS

    public ResumenFinanciero reporteEntre(
            LocalDate inicio,
            LocalDate fin)
            throws ReglaNegocioException {

        if (inicio == null ||
            fin == null ||
            inicio.isAfter(fin)) {

            throw new ReglaNegocioException(
                "Intervalo de fechas invalido"
            );
        }

        int cantidadTienda = 0;
        int cantidadCafeteria = 0;
        int cantidadMensualidades = 0;

        double totalTienda = 0;
        double totalCafeteria = 0;
        double totalMensualidades = 0;

        // RECORRER VENTAS DE TIENDA

        for (VentaTienda venta : tienda.getVentas()) {

            if (enRango(
                    venta.getFecha(),
                    inicio,
                    fin)) {

                cantidadTienda++;
                totalTienda += venta.getTotalPagado();
            }
        }

        // RECORRER VENTAS DE CAFETERIA

        for (VentaCafeteria venta : cafeteria.getVentas()) {

            if (enRango(
                    venta.getFecha(),
                    inicio,
                    fin)) {

                cantidadCafeteria++;
                totalCafeteria += venta.getTotalPagado();
            }
        }

        // RECORRER MENSUALIDADES PAGADAS

        for (RegistroMensualidad pago : mensualidades) {

            if (enRango(
                    pago.getFechaPago(),
                    inicio,
                    fin)) {

                cantidadMensualidades++;
                totalMensualidades += pago.getValorPagado();
            }
        }

        // GENERAR RESUMEN

        return new ResumenFinanciero(
            inicio,
            fin,
            cantidadTienda,
            cantidadCafeteria,
            cantidadMensualidades,
            totalTienda,
            totalCafeteria,
            totalMensualidades
        );
    }

    // COMPROBAR SI UNA FECHA ESTA EN EL INTERVALO

    private static boolean enRango(
            LocalDate fecha,
            LocalDate inicio,
            LocalDate fin) {

        return fecha != null &&
            !fecha.isBefore(inicio) &&
            !fecha.isAfter(fin);
    }
}
