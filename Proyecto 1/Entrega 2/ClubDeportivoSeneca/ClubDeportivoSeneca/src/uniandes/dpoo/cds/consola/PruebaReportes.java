
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.GestorCafeteria;
import uniandes.dpoo.cds.aplicacion.GestorReportes;
import uniandes.dpoo.cds.aplicacion.GestorTienda;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;
import uniandes.dpoo.cds.modelo.comercial.ResumenFinanciero;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaReportes {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // PRUEBA EXITOSA

    private static void aceptar(
            String titulo,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + titulo);
            correctas++;

        } catch (Exception e) {
            System.out.println(
                "[FALLO] " + titulo + " -> " + e
            );
            fallidas++;
        }
    }

    // PRUEBA QUE DEBE SER RECHAZADA

    private static void rechazar(
            String titulo,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + titulo + " (se acepto)"
            );

            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + titulo + " -> " + e.getMessage()
            );

            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + titulo + " -> " + e
            );

            fallidas++;
        }
    }

    // VERIFICAR CONDICIONES

    private static void verificar(
            String titulo,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ")
            + titulo
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    private static boolean igual(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // CREAR GESTORES

        GestorTienda tienda = new GestorTienda();

        GestorCafeteria cafeteria =
            new GestorCafeteria();

        GestorReportes reportes =
            new GestorReportes(tienda, cafeteria);

        // CREAR SOCIOS

        Socio ana = new Socio(
            "anaReporte",
            "demo",
            LocalDate.of(2002, 1, 1)
        );

        Socio juan = new Socio(
            "juanReporte",
            "demo",
            LocalDate.of(2001, 1, 1)
        );

        LocalDate hoy = LocalDate.now();

        YearMonth mesActual =
            YearMonth.from(hoy);

        YearMonth mesAnterior =
            mesActual.minusMonths(1);

        System.out.println(
            "PRUEBAS DE REPORTES FINANCIEROS"
        );

        System.out.println(
            "--------------------------------------"
        );

        // REPORTES INICIALES

        verificar(
            "Reporte diario inicialmente vacio",

            reportes.reporteDiario(hoy)
                .getNumeroOperaciones() == 0
        );

        verificar(
            "Reporte semanal inicialmente vacio",

            reportes.reporteSemanal(hoy)
                .getRecaudoTotal() == 0
        );

        verificar(
            "Reporte mensual inicialmente vacio",

            reportes.reporteMensual(mesActual)
                .getRecaudoTotal() == 0
        );

        // REGISTRAR PRODUCTOS

        aceptar(
            "Registrar articulo y producto de cafeteria",
            () -> {

                tienda.registrarArticulo(
                    new Articulo(
                        "BAL-01",
                        "Balon",
                        "Balones",
                        50000
                    ),
                    5
                );

                cafeteria.registrarProducto(
                    new Bebida(
                        "B-01",
                        "Limonada",
                        5000,
                        Bebida.Temperatura.FRIA
                    )
                );
            }
        );

        // REGISTRAR VENTA DE TIENDA

        aceptar(
            "Comprar un balon en tienda",
            () -> tienda.vender(
                ana,
                Map.of("BAL-01", 1),
                null,
                0
            )
        );

        // REGISTRAR VENTA DE CAFETERIA

        aceptar(
            "Comprar una bebida con propina del 10 por ciento",
            () -> cafeteria.vender(
                ana,
                Map.of("B-01", 1),
                null,
                true,
                10,
                0
            )
        );

        ResumenFinanciero primerReporte =
            reportes.reporteDiario(hoy);

        // VERIFICAR REPORTES

        verificar(
            "Reporte diario incluye ambas ventas",

            primerReporte.getVentasTienda() == 1 &&
            primerReporte.getVentasCafeteria() == 1 &&
            primerReporte.getPagosMensualidad() == 0
        );

        verificar(
            "Tienda recauda 59500 y cafeteria 5900",

            igual(
                primerReporte.getRecaudoTienda(),
                59500
            ) &&

            igual(
                primerReporte.getRecaudoCafeteria(),
                5900
            )
        );

        verificar(
            "Recaudo inicial total: 65400",

            igual(
                primerReporte.getRecaudoTotal(),
                65400
            )
        );

        verificar(
            "Reporte semanal incluye ambas ventas",

            reportes.reporteSemanal(hoy)
                .getNumeroOperaciones() == 2
        );

        verificar(
            "Reporte mensual incluye ambas ventas",

            reportes.reporteMensual(mesActual)
                .getNumeroOperaciones() == 2
        );

        // VALIDACIONES DE MENSUALIDADES

        rechazar(
            "Mensualidad sin socio",
            () -> reportes.registrarMensualidad(
                null,
                mesActual,
                hoy,
                100000
            )
        );

        rechazar(
            "Mensualidad con valor negativo",
            () -> reportes.registrarMensualidad(
                ana,
                mesActual,
                hoy,
                -10
            )
        );

        rechazar(
            "Mensualidad sin fecha",
            () -> reportes.registrarMensualidad(
                ana,
                mesActual,
                null,
                100000
            )
        );

        // REGISTRAR MENSUALIDADES

        aceptar(
            "Registrar mensualidad actual de Ana",
            () -> reportes.registrarMensualidad(
                ana,
                mesActual,
                hoy,
                100000
            )
        );

        rechazar(
            "No duplicar mensualidad del mismo socio y periodo",
            () -> reportes.registrarMensualidad(
                ana,
                mesActual,
                hoy,
                100000
            )
        );

        aceptar(
            "Registrar pago del mes anterior de Juan",
            () -> reportes.registrarMensualidad(
                juan,
                mesAnterior,
                hoy.minusMonths(1),
                80000
            )
        );

        // REPORTES CON LOS TRES RUBROS

        ResumenFinanciero actual =
            reportes.reporteDiario(hoy);

        verificar(
            "Reporte del dia tiene tres operaciones",

            actual.getNumeroOperaciones() == 3
        );

        verificar(
            "El recaudo del dia suma 165400",

            igual(
                actual.getRecaudoTotal(),
                165400
            )
        );

        verificar(
            "Rubros discriminados: tienda, cafeteria y mensualidades",

            igual(
                actual.getRecaudoTienda(),
                59500
            ) &&

            igual(
                actual.getRecaudoCafeteria(),
                5900
            ) &&

            igual(
                actual.getRecaudoMensualidades(),
                100000
            )
        );

        // REPORTES HISTORICOS

        verificar(
            "Reporte del mes anterior muestra una mensualidad",

            reportes.reporteMensual(mesAnterior)
                .getPagosMensualidad() == 1 &&

            igual(
                reportes.reporteMensual(mesAnterior)
                    .getRecaudoTotal(),
                80000
            )
        );

        verificar(
            "Reporte del dia anterior queda vacio",

            reportes.reporteDiario(
                hoy.minusDays(1)
            ).getNumeroOperaciones() == 0
        );

        // VALIDAR INTERVALOS

        rechazar(
            "No permitir intervalos de fechas invertidos",
            () -> reportes.reporteEntre(
                hoy,
                hoy.minusDays(1)
            )
        );

        verificar(
            "Rango amplio incluye 245400 de recaudo",

            igual(
                reportes.reporteEntre(
                    hoy.minusMonths(1),
                    hoy
                ).getRecaudoTotal(),
                245400
            )
        );

        // HISTORIAL

        verificar(
            "Historial contiene dos mensualidades",

            reportes.getMensualidades().size() == 2
        );

        try {
            reportes.getMensualidades().clear();

            verificar(
                "Historial protegido contra cambios externos",
                false
            );

        } catch (UnsupportedOperationException e) {

            verificar(
                "Historial protegido contra cambios externos",
                true
            );
        }

        // COMPROBAR QUE NO SE ALTERARON LAS VENTAS

        verificar(
            "El reporte no modifica las ventas originales",

            tienda.getVentas().size() == 1 &&
            cafeteria.getVentas().size() == 1
        );

        // RESULTADOS FINALES

        System.out.println(
            "--------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Recaudo diario: "
            + actual.getRecaudoTotal()
        );

        System.out.println(
            "Mensualidades registradas: "
            + reportes.getMensualidades().size()
        );
    }
}
