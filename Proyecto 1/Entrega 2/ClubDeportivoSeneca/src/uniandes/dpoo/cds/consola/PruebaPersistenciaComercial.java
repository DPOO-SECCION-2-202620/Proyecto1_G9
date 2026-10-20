
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.GestorCafeteria;
import uniandes.dpoo.cds.aplicacion.GestorTienda;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;
import uniandes.dpoo.cds.modelo.comercial.UbicacionInventario;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaComercial;
import uniandes.dpoo.cds.persistencia.PersistenciaComercial.EstadoComercial;

public class PruebaPersistenciaComercial {

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

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // ARCHIVO DE PRUEBA

        Path archivo = Path.of(
            "datos",
            "comercial-prueba.dat"
        );

        // CREAR SISTEMA CENTRAL

        SistemaClub sistema = new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );

        // CREAR USUARIOS

        Socio ana = new Socio(
            "anaComercial",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Administrador admin = new Administrador(
            "adminComercial",
            "demo"
        );

        Empleado empleado = new Empleado(
            "empComercial",
            "demo",
            "COD-COM"
        );

        // REGISTRAR ARTICULOS

        sistema.getTienda().registrarArticulo(
            new Articulo(
                "BAL-C",
                "Balon",
                "Deportes",
                100000
            ),
            3
        );

        sistema.getTienda().registrarArticulo(
            new Articulo(
                "MED-C",
                "Medias",
                "Deportes",
                20000
            ),
            2
        );

        sistema.getTienda().registrarEmpleado(
            empleado
        );

        // REABASTECER BODEGA

        sistema.getTienda().reabastecer(
            admin,
            "BAL-C",
            UbicacionInventario.BODEGA,
            4
        );

        // REGISTRAR BEBIDA

        sistema.getCafeteria().registrarProducto(
            new Bebida(
                "AGU-C",
                "Agua",
                5000,
                Bebida.Temperatura.FRIA
            )
        );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA COMERCIAL"
        );

        System.out.println(
            "------------------------------------"
        );

        // REALIZAR PRIMERAS COMPRAS

        sistema.getTienda().vender(
            ana,
            Map.of("BAL-C", 1),
            null,
            0
        );

        sistema.getCafeteria().vender(
            ana,
            Map.of("AGU-C", 1),
            null,
            true,
            0,
            0
        );

        // VERIFICAR ESTADO ANTES DE GUARDAR

        verificar(
            "Saldo compartido antes de guardar: 2488",

            igual(
                sistema.getTienda().getSaldoPuntos(ana),
                2488
            ) &&

            igual(
                sistema.getCafeteria().getSaldoPuntos(ana),
                2488
            )
        );

        verificar(
            "Inventario de tienda y bodega antes de guardar",

            sistema.getTienda().getExistencias("BAL-C") == 2 &&

            sistema.getTienda().getExistencias(
                "BAL-C",
                UbicacionInventario.BODEGA
            ) == 4
        );

        verificar(
            "Dos historiales de ventas antes de guardar",

            sistema.getTienda().getVentas().size() == 1 &&

            sistema.getCafeteria().getVentas().size() == 1
        );

        // GUARDAR AMBOS MODULOS

        PersistenciaComercial.guardar(
            sistema,
            archivo
        );

        verificar(
            "Archivo comercial creado fuera de src",
            Files.isRegularFile(archivo)
        );

        // RECUPERAR LOS OBJETOS DESDE DISCO

        EstadoComercial recuperado =
            PersistenciaComercial.cargar(archivo);

        GestorTienda tienda =
            recuperado.getTienda();

        GestorCafeteria cafeteria =
            recuperado.getCafeteria();

        // VERIFICAR INVENTARIOS RECUPERADOS

        verificar(
            "Se recupera el inventario de las dos ubicaciones",

            tienda.getExistencias("BAL-C") == 2 &&

            tienda.getExistencias(
                "BAL-C",
                UbicacionInventario.BODEGA
            ) == 4
        );

        // VERIFICAR VENTAS RECUPERADAS

        verificar(
            "Se recuperan todas las ventas",

            tienda.getVentas().size() == 1 &&

            cafeteria.getVentas().size() == 1
        );

        // VERIFICAR MOVIMIENTOS

        verificar(
            "Movimientos de bodega y venta recuperados",

            tienda.getMovimientos().size() == 2
        );

        // VERIFICAR PUNTOS COMPARTIDOS

        verificar(
            "Saldo sigue compartido tras recuperar",

            igual(
                tienda.getSaldoPuntos(ana),
                2488
            ) &&

            igual(
                cafeteria.getSaldoPuntos(ana),
                2488
            )
        );

        // COMPRAR EN CAFETERIA RECUPERADA

        var compraCafe = cafeteria.vender(
            ana,
            Map.of("AGU-C", 1),
            null,
            true,
            0,
            1000
        );

        verificar(
            "Menu recuperado permite vender sin volver a registrarlo",

            igual(
                compraCafe.getTotalPagado(),
                4400
            )
        );

        verificar(
            "Redencion en cafeteria actualiza saldo de tienda",

            igual(
                tienda.getSaldoPuntos(ana),
                1576
            )
        );

        // COMPRAR EN TIENDA RECUPERADA

        var compraTienda = tienda.vender(
            ana,
            Map.of("MED-C", 1),
            null,
            1000
        );

        verificar(
            "Inventario recuperado permite vender",

            tienda.getExistencias("MED-C") == 1 &&

            igual(
                compraTienda.getTotalPagado(),
                22800
            )
        );

        verificar(
            "Puntos compartidos tras compra en tienda: 1032",

            igual(
                cafeteria.getSaldoPuntos(ana),
                1032
            )
        );

        // COMPROBAR CODIGO DE DESCUENTO RECUPERADO

        var compraCodigo = tienda.vender(
            ana,
            Map.of("MED-C", 1),
            "COD-COM",
            0
        );

        verificar(
            "Codigo del empleado recuperado: descuento del 8 por ciento",

            igual(
                compraCodigo.getDescuento(),
                1600
            )
        );

        verificar(
            "Saldo compartido despues de descuento: 1469.92",

            igual(
                tienda.getSaldoPuntos(ana),
                1469.92
            )
        );

        verificar(
            "Historial acumulado tras reabrir",

            tienda.getVentas().size() == 3 &&

            cafeteria.getVentas().size() == 2
        );

        // GUARDAR NUEVAMENTE EL ESTADO RECUPERADO

        PersistenciaComercial.guardar(
            recuperado,
            archivo
        );

        // SIMULAR SEGUNDO REINICIO

        EstadoComercial segundaCarga =
            PersistenciaComercial.cargar(archivo);

        verificar(
            "Segundo reinicio conserva ventas y puntos",

            segundaCarga.getTienda()
                .getVentas().size() == 3 &&

            segundaCarga.getCafeteria()
                .getVentas().size() == 2 &&

            igual(
                segundaCarga.getCafeteria()
                    .getSaldoPuntos(ana),
                1469.92
            )
        );

        verificar(
            "Segundo reinicio conserva existencias",

            segundaCarga.getTienda()
                .getExistencias("MED-C") == 0 &&

            segundaCarga.getTienda()
                .getExistencias("BAL-C") == 2
        );

        // RESULTADOS FINALES

        System.out.println(
            "------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Saldo recuperado: "
            + segundaCarga.getTienda()
                .getSaldoPuntos(ana)
        );

        System.out.println(
            "Archivo: "
            + archivo.toAbsolutePath()
        );
    }
}
