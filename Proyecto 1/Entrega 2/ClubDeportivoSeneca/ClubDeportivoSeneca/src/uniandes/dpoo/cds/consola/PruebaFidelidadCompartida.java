
package uniandes.dpoo.cds.consola;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.SistemaClub;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaFidelidadCompartida {

    private static int correctas = 0;
    private static int fallidas = 0;

    // VERIFICAR RESULTADOS

    private static void verificar(
            String titulo,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ") + titulo
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

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // VERIFICAR OPERACIONES RECHAZADAS

    private static void rechazar(
            String titulo,
            Accion accion) {

        try {
            accion.ejecutar();

            verificar(titulo, false);

        } catch (ReglaNegocioException e) {

            verificar(titulo, true);

        } catch (Exception e) {

            System.out.println(
                "Error inesperado: " + e
            );

            verificar(titulo, false);
        }
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // CREAR SISTEMA

        SistemaClub sistema = new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );

        // CREAR SOCIOS

        Socio ana = new Socio(
            "anaFid",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Socio bob = new Socio(
            "bobFid",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        // REGISTRAR ARTICULOS

        sistema.getTienda().registrarArticulo(
            new Articulo(
                "BAL-F",
                "Balon",
                "Deporte",
                100000
            ),
            2
        );

        sistema.getTienda().registrarArticulo(
            new Articulo(
                "MED-F",
                "Media",
                "Deporte",
                20000
            ),
            2
        );

        // REGISTRAR BEBIDA

        sistema.getCafeteria().registrarProducto(
            new Bebida(
                "AGU-F",
                "Agua",
                5000,
                Bebida.Temperatura.FRIA
            )
        );

        System.out.println(
            "PRUEBAS DE FIDELIDAD COMPARTIDA"
        );

        System.out.println(
            "----------------------------------"
        );

        // SALDOS INICIALES

        verificar(
            "Ambos saldos inician en cero",

            igual(
                sistema.getTienda().getSaldoPuntos(ana),
                0
            ) &&

            igual(
                sistema.getCafeteria().getSaldoPuntos(ana),
                0
            )
        );

        // COMPRA DE TIENDA

        sistema.getTienda().vender(
            ana,
            Map.of("BAL-F", 1),
            null,
            0
        );

        verificar(
            "Compra tienda genera 2380 puntos en ambos saldos",

            igual(
                sistema.getTienda().getSaldoPuntos(ana),
                2380
            ) &&

            igual(
                sistema.getCafeteria().getSaldoPuntos(ana),
                2380
            )
        );

        // USAR PUNTOS DE TIENDA EN CAFETERIA

        var cafe =
            sistema.getCafeteria().vender(
                ana,
                Map.of("AGU-F", 1),
                null,
                true,
                10,
                1000
            );

        verificar(
            "Cafeteria permite redimir puntos de tienda",

            igual(
                cafe.getTotalPagado(),
                4900
            ) &&

            igual(
                sistema.getTienda().getSaldoPuntos(ana),
                1478
            ) &&

            igual(
                sistema.getCafeteria().getSaldoPuntos(ana),
                1478
            )
        );

        // USAR SALDO ACTUALIZADO EN TIENDA

        var tienda =
            sistema.getTienda().vender(
                ana,
                Map.of("MED-F", 1),
                null,
                1000
            );

        verificar(
            "Tienda permite redimir puntos acumulados en cafeteria",

            igual(
                tienda.getTotalPagado(),
                22800
            ) &&

            igual(
                sistema.getTienda().getSaldoPuntos(ana),
                934
            ) &&

            igual(
                sistema.getCafeteria().getSaldoPuntos(ana),
                934
            )
        );

        // COMPROBAR SALDO DESPUES DE RECHAZOS

        double saldo =
            sistema.getFidelidad().getSaldo(ana);

        rechazar(
            "Rechazar puntos insuficientes sin cambiar saldo",

            () -> sistema.getCafeteria().vender(
                ana,
                Map.of("AGU-F", 1),
                null,
                true,
                0,
                9999
            )
        );

        verificar(
            "Saldo permanece tras rechazo de cafeteria",

            igual(
                sistema.getFidelidad().getSaldo(ana),
                saldo
            )
        );

        rechazar(
            "Rechazar inventario insuficiente sin modificar puntos",

            () -> sistema.getTienda().vender(
                ana,
                Map.of("MED-F", 50),
                null,
                0
            )
        );

        verificar(
            "Saldo permanece tras rechazo de tienda",

            igual(
                sistema.getFidelidad().getSaldo(ana),
                saldo
            )
        );

        // COMPROBAR CUENTAS DE SOCIOS DIFERENTES

        verificar(
            "Los socios tienen cuentas separadas",

            igual(
                sistema.getFidelidad().getSaldo(bob),
                0
            )
        );

        // COMPROBAR SISTEMAS INDEPENDIENTES

        SistemaClub otro = new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );

        verificar(
            "Dos sistemas no mezclan sus saldos",

            igual(
                otro.getFidelidad().getSaldo(ana),
                0
            )
        );

        // RESULTADOS FINALES

        System.out.println(
            "----------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Saldo de Ana: "
            + sistema.getFidelidad().getSaldo(ana)
        );
    }
}
