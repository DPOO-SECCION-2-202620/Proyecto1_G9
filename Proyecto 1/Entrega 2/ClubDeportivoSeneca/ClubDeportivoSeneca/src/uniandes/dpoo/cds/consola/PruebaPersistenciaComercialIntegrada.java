
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;
import uniandes.dpoo.cds.modelo.comercial.ResumenFinanciero;
import uniandes.dpoo.cds.modelo.comercial.UbicacionInventario;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaComercialIntegrada;

public class PruebaPersistenciaComercialIntegrada {

    private static int correctas = 0;
    private static int fallidas = 0;

    // VERIFICAR RESULTADOS

    private static void verificar(
            String nombre,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ") + nombre
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

    public static void main(String[] args)
            throws Exception {

        // CONFIGURAR HORARIOS

        LocalTime apertura = LocalTime.of(8, 0);
        LocalTime cierre = LocalTime.of(18, 0);

        EnumSet<DayOfWeek> dias =
            EnumSet.of(DayOfWeek.MONDAY);

        // ARCHIVO DE PRUEBA NUEVO

        Path archivo = Path.of(
            "datos",
            "comercial-integrado-prueba.dat"
        );

        // CREAR USUARIOS

        Socio ana = new Socio(
            "anaIntegrada",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Empleado empleado = new Empleado(
            "empleadoIntegrado",
            "demo",
            "COD-INT"
        );

        Administrador admin = new Administrador(
            "adminIntegrado",
            "demo"
        );

        // CREAR SISTEMA ORIGINAL

        SistemaClub primero =
            new SistemaClub(
                apertura,
                cierre,
                dias
            );

        // REGISTRAR ARTICULO

        primero.getTienda().registrarArticulo(
            new Articulo(
                "BAL-I",
                "Balon",
                "Deportes",
                100000
            ),
            3
        );

        // REGISTRAR BEBIDA

        primero.getCafeteria().registrarProducto(
            new Bebida(
                "AGU-I",
                "Agua",
                5000,
                Bebida.Temperatura.FRIA
            )
        );

        // ADMINISTRAR INVENTARIO

        primero.getTienda().reabastecer(
            admin,
            "BAL-I",
            UbicacionInventario.BODEGA,
            4
        );

        primero.getTienda().trasladar(
            admin,
            "BAL-I",
            UbicacionInventario.BODEGA,
            UbicacionInventario.TIENDA,
            2
        );

        // REALIZAR VENTAS

        primero.getTienda().vender(
            ana,
            Map.of("BAL-I", 1),
            null,
            0
        );

        primero.getCafeteria().vender(
            ana,
            Map.of("AGU-I", 1),
            null,
            true,
            0,
            0
        );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA COMERCIAL INTEGRADA"
        );

        System.out.println(
            "-----------------------------------------"
        );

        // VERIFICAR SALDO ORIGINAL

        verificar(
            "Saldo original compartido: 2488",

            igual(
                primero.getFidelidad().getSaldo(ana),
                2488
            ) &&

            igual(
                primero.getTienda().getSaldoPuntos(ana),
                2488
            ) &&

            igual(
                primero.getCafeteria().getSaldoPuntos(ana),
                2488
            )
        );

        // GUARDAR SISTEMA

        PersistenciaComercialIntegrada.guardar(
            primero,
            archivo
        );

        verificar(
            "Archivo nuevo creado sin sobrescribir los anteriores",
            Files.isRegularFile(archivo)
        );

        // RECUPERAR SISTEMA COMPLETO COMERCIAL

        SistemaClub recuperado =
            PersistenciaComercialIntegrada.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        // COMPROBAR INVENTARIOS

        verificar(
            "Se recupera inventario de tienda y bodega",

            recuperado.getTienda()
                .getExistencias("BAL-I") == 4 &&

            recuperado.getTienda().getExistencias(
                "BAL-I",
                UbicacionInventario.BODEGA
            ) == 2
        );

        // COMPROBAR HISTORIALES

        verificar(
            "Se recuperan historiales de compras",

            recuperado.getTienda()
                .getVentas().size() == 1 &&

            recuperado.getCafeteria()
                .getVentas().size() == 1
        );

        // COMPROBAR FIDELIDAD

        verificar(
            "Fidelidad sigue compartida al reiniciar",

            igual(
                recuperado.getFidelidad().getSaldo(ana),
                2488
            ) &&

            igual(
                recuperado.getTienda().getSaldoPuntos(ana),
                2488
            ) &&

            igual(
                recuperado.getCafeteria().getSaldoPuntos(ana),
                2488
            )
        );

        // COMPROBAR REPORTES DESPUES DE CARGAR

        ResumenFinanciero reporte =
            recuperado.getReportesFinancieros()
                .reporteDiario(LocalDate.now());

        verificar(
            "Reporte central encuentra ambas ventas recuperadas",

            reporte.getVentasTienda() == 1 &&

            reporte.getVentasCafeteria() == 1 &&

            igual(
                reporte.getRecaudoTotal(),
                124400
            )
        );

        // PROPONER NUEVO ARTICULO

        int numero =
            recuperado.getSugerencias().proponerArticulo(
                empleado,

                new Articulo(
                    "MED-I",
                    "Medias",
                    "Ropa",
                    20000
                ),

                "Ampliar catalogo"
            ).getNumero();

        // APROBAR SUGERENCIA

        recuperado.getSugerencias().aprobar(
            admin,
            numero,
            "Aprobado"
        );

        verificar(
            "Sugerencia aprobada alimenta tienda restaurada",

            recuperado.getTienda()
                .getExistencias("MED-I") == 0
        );

        // REABASTECER NUEVO ARTICULO

        recuperado.getTienda().reabastecer(
            admin,
            "MED-I",
            UbicacionInventario.TIENDA,
            2
        );

        // REDIMIR PUNTOS EN CAFETERIA

        double cafePagado =
            recuperado.getCafeteria().vender(
                ana,
                Map.of("AGU-I", 1),
                null,
                true,
                0,
                1000
            ).getTotalPagado();

        verificar(
            "Puntos originales pueden redimirse en cafeteria",

            igual(cafePagado, 4400) &&

            igual(
                recuperado.getTienda().getSaldoPuntos(ana),
                1576
            )
        );

        // REDIMIR PUNTOS EN TIENDA

        double tiendaPagado =
            recuperado.getTienda().vender(
                ana,
                Map.of("MED-I", 1),
                null,
                1000
            ).getTotalPagado();

        verificar(
            "Puntos actualizados pueden redimirse en tienda",

            igual(tiendaPagado, 22800) &&

            igual(
                recuperado.getCafeteria().getSaldoPuntos(ana),
                1032
            )
        );

        // COMPROBAR REPORTES ACTUALIZADOS

        verificar(
            "Reporte refleja compras posteriores a la carga",

            recuperado.getReportesFinancieros()
                .reporteDiario(LocalDate.now())
                .getVentasTienda() == 2 &&

            recuperado.getReportesFinancieros()
                .reporteDiario(LocalDate.now())
                .getVentasCafeteria() == 2
        );

        // VOLVER A GUARDAR

        PersistenciaComercialIntegrada.guardar(
            recuperado,
            archivo
        );

        // SIMULAR SEGUNDO REINICIO

        SistemaClub otraVez =
            PersistenciaComercialIntegrada.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        verificar(
            "Segundo reinicio conserva saldo 1032",

            igual(
                otraVez.getFidelidad().getSaldo(ana),
                1032
            ) &&

            igual(
                otraVez.getCafeteria().getSaldoPuntos(ana),
                1032
            )
        );

        verificar(
            "Segundo reinicio conserva inventario y ventas",

            otraVez.getTienda()
                .getExistencias("MED-I") == 1 &&

            otraVez.getTienda()
                .getVentas().size() == 2 &&

            otraVez.getCafeteria()
                .getVentas().size() == 2
        );

        // COMPROBAR GESTORES DEPORTIVOS

        verificar(
            "Los gestores deportivos siguen disponibles",

            otraVez.getClub() != null &&

            otraVez.getEntrenamientos() != null &&

            otraVez.getPartidosIndividuales() != null
        );

        // RESULTADOS FINALES

        System.out.println(
            "-----------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Saldo final: "
            + otraVez.getFidelidad().getSaldo(ana)
        );

        System.out.println(
            "Archivo: "
            + archivo.toAbsolutePath()
        );
    }
}
