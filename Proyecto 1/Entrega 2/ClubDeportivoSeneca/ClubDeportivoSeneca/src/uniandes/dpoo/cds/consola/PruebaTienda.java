
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.GestorTienda;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.VentaTienda;
import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaTienda {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void aceptar(
            String nombre,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + nombre);
            correctas++;

        } catch (Exception e) {
            System.out.println(
                "[FALLO] " + nombre + " -> " + e
            );
            fallidas++;
        }
    }

    private static void rechazar(
            String nombre,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + nombre + " (se acepto)"
            );

            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + nombre
                + " -> " + e.getMessage()
            );

            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + nombre + " -> " + e
            );

            fallidas++;
        }
    }

    private static void verificar(
            String nombre,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ")
            + nombre
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    private static boolean cerca(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        GestorTienda tienda =
            new GestorTienda();

        // CREAR ARTICULOS

        Articulo balon = new Articulo(
            "BAL-01",
            "Balon de futbol",
            "Balones",
            50000
        );

        Articulo raqueta = new Articulo(
            "RAQ-01",
            "Raqueta",
            "Raquetas",
            120000
        );

        // CREAR USUARIOS

        Empleado empleado = new Empleado(
            "tienda01", "demo", "EMP15"
        );

        Empleado otroEmpleado = new Empleado(
            "tienda02", "demo", "EMPOTRO"
        );

        Socio socio = new Socio(
            "juan01",
            "demo",
            LocalDate.of(2003, 5, 15)
        );

        Administrador administrador =
            new Administrador("admin01", "demo");

        System.out.println(
            "PRUEBAS DE TIENDA Y FIDELIZACION"
        );

        System.out.println(
            "----------------------------------------"
        );

        // REGISTRAR ARTICULOS

        aceptar("Registrar dos articulos", () -> {

            tienda.registrarArticulo(balon, 5);
            tienda.registrarArticulo(raqueta, 2);
        });

        aceptar("Registrar codigo de empleado", () ->
            tienda.registrarEmpleado(empleado)
        );

        rechazar("Rechazar articulo duplicado", () ->
            tienda.registrarArticulo(balon, 5)
        );

        rechazar(
            "Rechazar cantidad de stock negativa",
            () -> tienda.registrarArticulo(
                new Articulo(
                    "BAD-01",
                    "Otro",
                    "Otro",
                    100
                ),
                -1
            )
        );

        rechazar("Rechazar articulo inexistente", () ->
            tienda.vender(
                socio,
                Map.of("XX", 1),
                null,
                0
            )
        );

        rechazar("Rechazar cantidades negativas", () ->
            tienda.vender(
                socio,
                Map.of("BAL-01", -1),
                null,
                0
            )
        );

        rechazar("Rechazar inventario insuficiente", () ->
            tienda.vender(
                socio,
                Map.of("BAL-01", 99),
                null,
                0
            )
        );

        rechazar("Rechazar codigo desconocido", () ->
            tienda.vender(
                socio,
                Map.of("BAL-01", 1),
                "FALSO",
                0
            )
        );

        // COMPRA DE SOCIO CON DESCUENTO 8%

        final VentaTienda[] socioDescuento =
            new VentaTienda[1];

        aceptar("Socio usa codigo compartido del 8%", () ->
            socioDescuento[0] = tienda.vender(
                socio,
                Map.of("BAL-01", 2),
                "EMP15",
                0
            )
        );

        verificar(
            "Descuento socio 8000, IVA 17480 y total 109480",

            cerca(
                socioDescuento[0].getDescuento(),
                8000
            ) &&

            cerca(
                socioDescuento[0].getImpuesto(),
                17480
            ) &&

            cerca(
                socioDescuento[0].getTotalPagado(),
                109480
            )
        );

        verificar(
            "Saldo de puntos del socio: 2189.60",

            cerca(
                tienda.getSaldoPuntos(socio),
                2189.60
            )
        );

        // COMPRA EMPLEADO CON DESCUENTO 15%

        final VentaTienda[] empleadoDescuento =
            new VentaTienda[1];

        aceptar("Empleado usa codigo propio del 15%", () ->
            empleadoDescuento[0] = tienda.vender(
                empleado,
                Map.of("RAQ-01", 1),
                "EMP15",
                0
            )
        );

        verificar(
            "Descuento empleado 18000 y total 121380",

            cerca(
                empleadoDescuento[0].getDescuento(),
                18000
            ) &&

            cerca(
                empleadoDescuento[0].getTotalPagado(),
                121380
            )
        );

        rechazar("Empleado no usa codigo ajeno", () ->
            tienda.vender(
                otroEmpleado,
                Map.of("BAL-01", 1),
                "EMP15",
                0
            )
        );

        rechazar(
            "Administrador no compra como socio",
            () -> tienda.vender(
                administrador,
                Map.of("BAL-01", 1),
                null,
                0
            )
        );

        // COMPRA SIN DESCUENTO

        aceptar(
            "Socio compra sin descuento y gana puntos",
            () -> tienda.vender(
                socio,
                Map.of("BAL-01", 1),
                null,
                0
            )
        );

        verificar(
            "Saldo socio antes de redimir: 3379.60",

            cerca(
                tienda.getSaldoPuntos(socio),
                3379.60
            )
        );

        // VALIDAR REDENCION

        rechazar(
            "No acumular puntos y codigo empleado",
            () -> tienda.vender(
                socio,
                Map.of("BAL-01", 1),
                "EMP15",
                500
            )
        );

        rechazar(
            "No redimir puntos superiores al saldo",
            () -> tienda.vender(
                socio,
                Map.of("BAL-01", 1),
                null,
                4000
            )
        );

        // REDIMIR PUNTOS

        final VentaTienda[] redencion =
            new VentaTienda[1];

        aceptar(
            "Redimir 3000 puntos en nueva compra",
            () -> redencion[0] = tienda.vender(
                socio,
                Map.of("BAL-01", 1),
                null,
                3000
            )
        );

        verificar(
            "Pago 56500 y puntos restantes 1509.60",

            cerca(
                redencion[0].getTotalPagado(),
                56500
            ) &&

            cerca(
                tienda.getSaldoPuntos(socio),
                1509.60
            )
        );

        // COMPROBACIONES FINALES

        verificar(
            "Inventario actualizado: 1 balon y 1 raqueta",

            tienda.getExistencias("BAL-01") == 1 &&
            tienda.getExistencias("RAQ-01") == 1
        );

        verificar(
            "Cuatro ventas registradas",
            tienda.getVentas().size() == 4
        );

        System.out.println(
            "----------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Ventas registradas: "
            + tienda.getVentas().size()
        );

        System.out.println(
            "Balones en inventario: "
            + tienda.getExistencias("BAL-01")
        );
    }
}
