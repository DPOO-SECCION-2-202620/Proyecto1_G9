
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.GestorTienda;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.MovimientoInventario;
import uniandes.dpoo.cds.modelo.comercial.UbicacionInventario;
import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaInventarioAdministrativo {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // PRUEBA QUE DEBE FUNCIONAR

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

    // PRUEBA QUE DEBE SER RECHAZADA

    private static void rechazar(
            String nombre,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + nombre + " (fue aceptado)"
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

    // VERIFICAR RESULTADO

    private static void verificar(
            String nombre,
            boolean ok) {

        System.out.println(
            (ok ? "[OK] " : "[FALLO] ") + nombre
        );

        if (ok) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        GestorTienda tienda =
            new GestorTienda();

        Administrador admin =
            new Administrador(
                "admin01",
                "demo"
            );

        Socio socio =
            new Socio(
                "ana01",
                "demo",
                LocalDate.of(2002, 1, 1)
            );

        Articulo balon =
            new Articulo(
                "BAL-01",
                "Balon",
                "Balones",
                50000
            );

        Articulo raqueta =
            new Articulo(
                "RAQ-01",
                "Raqueta",
                "Raquetas",
                120000
            );

        System.out.println(
            "PRUEBAS DE INVENTARIO ADMINISTRATIVO"
        );

        System.out.println(
            "----------------------------------------"
        );

        // REGISTRAR INVENTARIO INICIAL

        aceptar(
            "Registrar productos y stock inicial",
            () -> {

                tienda.registrarArticulo(balon, 2);
                tienda.registrarArticulo(raqueta, 1);
            }
        );

        verificar(
            "Stock inicial en tienda",
            tienda.getExistencias("BAL-01") == 2
        );

        verificar(
            "Bodega inicia en cero",

            tienda.getExistencias(
                "BAL-01",
                UbicacionInventario.BODEGA
            ) == 0
        );

        // VALIDAR REABASTECIMIENTOS

        rechazar(
            "Rechazar articulo no registrado",
            () -> tienda.reabastecer(
                admin,
                "FALSO",
                UbicacionInventario.BODEGA,
                2
            )
        );

        rechazar(
            "Rechazar operacion sin administrador",
            () -> tienda.reabastecer(
                null,
                "BAL-01",
                UbicacionInventario.BODEGA,
                2
            )
        );

        rechazar(
            "Rechazar ingreso de cantidad negativa",
            () -> tienda.reabastecer(
                admin,
                "BAL-01",
                UbicacionInventario.BODEGA,
                -1
            )
        );

        aceptar(
            "Reabastecer bodega con diez balones",
            () -> tienda.reabastecer(
                admin,
                "BAL-01",
                UbicacionInventario.BODEGA,
                10
            )
        );

        verificar(
            "Bodega con diez balones",

            tienda.getExistencias(
                "BAL-01",
                UbicacionInventario.BODEGA
            ) == 10
        );

        // VALIDAR TRASLADOS

        rechazar(
            "Rechazar traslado mayor que stock",
            () -> tienda.trasladar(
                admin,
                "BAL-01",
                UbicacionInventario.BODEGA,
                UbicacionInventario.TIENDA,
                11
            )
        );

        rechazar(
            "Rechazar traslado a misma ubicacion",
            () -> tienda.trasladar(
                admin,
                "BAL-01",
                UbicacionInventario.BODEGA,
                UbicacionInventario.BODEGA,
                2
            )
        );

        aceptar(
            "Trasladar cuatro balones a tienda",
            () -> tienda.trasladar(
                admin,
                "BAL-01",
                UbicacionInventario.BODEGA,
                UbicacionInventario.TIENDA,
                4
            )
        );

        verificar(
            "Stock sincronizado despues de traslado",

            tienda.getExistencias(
                "BAL-01",
                UbicacionInventario.BODEGA
            ) == 6 &&

            tienda.getExistencias("BAL-01") == 6
        );

        aceptar(
            "Devolver un balon a bodega",
            () -> tienda.trasladar(
                admin,
                "BAL-01",
                UbicacionInventario.TIENDA,
                UbicacionInventario.BODEGA,
                1
            )
        );

        verificar(
            "Devolucion correcta",

            tienda.getExistencias(
                "BAL-01",
                UbicacionInventario.BODEGA
            ) == 7 &&

            tienda.getExistencias("BAL-01") == 5
        );

        // CONFIGURAR MINIMOS

        rechazar(
            "No permitir minimo negativo",
            () -> tienda.configurarMinimo(
                admin,
                "BAL-01",
                -1
            )
        );

        aceptar(
            "Configurar minimo de seis balones",
            () -> tienda.configurarMinimo(
                admin,
                "BAL-01",
                6
            )
        );

        verificar(
            "Alerta de bajo inventario",

            tienda.getArticulosStockBajo().size() == 1 &&

            tienda.getArticulosStockBajo()
                  .get(0) == balon
        );

        verificar(
            "Sugerir reposicion de siete balones",

            tienda.getSugerenciasReposicion()
                  .get("BAL-01") == 7
        );

        // COMPROBAR VENTAS DESDE TIENDA

        aceptar(
            "Venta descuenta stock de tienda",
            () -> tienda.vender(
                socio,
                Map.of("BAL-01", 2),
                null,
                0
            )
        );

        verificar(
            "Venta no descuenta inventario de bodega",

            tienda.getExistencias("BAL-01") == 3 &&

            tienda.getExistencias(
                "BAL-01",
                UbicacionInventario.BODEGA
            ) == 7
        );

        verificar(
            "Venta y puntos siguen funcionando",

            tienda.getVentas().size() == 1 &&

            Math.abs(
                tienda.getSaldoPuntos(socio) - 2380
            ) < 0.001
        );

        rechazar(
            "No vender mercancia solo disponible en bodega",
            () -> tienda.vender(
                socio,
                Map.of("BAL-01", 4),
                null,
                0
            )
        );

        verificar(
            "Venta rechazada conserva existencias",

            tienda.getExistencias("BAL-01") == 3
        );

        // REABASTECER TIENDA DIRECTAMENTE

        aceptar(
            "Reabastecer tienda directamente",
            () -> tienda.reabastecer(
                admin,
                "BAL-01",
                UbicacionInventario.TIENDA,
                3
            )
        );

        verificar(
            "Stock directo en tienda",

            tienda.getExistencias("BAL-01") == 6
        );

        // VERIFICAR HISTORIAL

        verificar(
            "Historial contiene cinco movimientos",

            tienda.getMovimientos().size() == 5
        );

        verificar(
            "Historial registra venta y responsable",

            tienda.getMovimientos()
                  .get(3).getTipo() ==
                MovimientoInventario.Tipo.VENTA &&

            tienda.getMovimientos()
                  .get(3).getResponsable()
                  .equals("ana01")
        );

        verificar(
            "Sin alerta cuando el minimo esta cubierto",

            tienda.getArticulosStockBajo().isEmpty()
        );

        // RESULTADOS FINALES

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
            "Existencias en tienda: "
            + tienda.getExistencias("BAL-01")
        );

        System.out.println(
            "Existencias en bodega: "
            + tienda.getExistencias(
                "BAL-01",
                UbicacionInventario.BODEGA
            )
        );

        System.out.println(
            "Movimientos registrados: "
            + tienda.getMovimientos().size()
        );
    }
}
