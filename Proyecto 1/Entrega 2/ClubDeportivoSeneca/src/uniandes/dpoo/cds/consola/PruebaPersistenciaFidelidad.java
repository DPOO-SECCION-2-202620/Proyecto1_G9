
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.GestorFidelidad;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaFidelidad;

public class PruebaPersistenciaFidelidad {

    private static int correctas = 0;
    private static int fallidas = 0;

    // VERIFICAR RESULTADOS

    private static void verificar(
            String nombre,
            boolean resultado) {

        System.out.println(
            (resultado ? "[OK] " : "[FALLO] ") + nombre
        );

        if (resultado) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // COMPARAR VALORES DECIMALES

    private static boolean igual(double a, double b) {
        return Math.abs(a - b) < 0.001;
    }

    // CREAR SISTEMA CON FIDELIDAD RECUPERADA

    private static SistemaClub nuevoSistema(
            GestorFidelidad recuperado) {

        return new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY),
            recuperado
        );
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // ARCHIVO DE PRUEBA

        Path archivo = Path.of(
            "datos",
            "fidelidad-prueba.dat"
        );

        // CREAR SOCIOS

        Socio ana = new Socio(
            "anaPersistencia",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Socio bob = new Socio(
            "bobPersistencia",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        // CREAR PRIMER SISTEMA

        SistemaClub sistema1 =
            nuevoSistema(new GestorFidelidad());

        sistema1.getTienda().registrarArticulo(
            new Articulo(
                "BAL-P",
                "Balon",
                "Deportes",
                100000
            ),
            2
        );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA DE FIDELIDAD"
        );

        System.out.println(
            "---------------------------------------"
        );

        // COMPROBAR SALDO INICIAL

        verificar(
            "Saldo inicial en cero",

            igual(
                sistema1.getFidelidad().getSaldo(ana),
                0
            )
        );

        // PRIMERA COMPRA

        sistema1.getTienda().vender(
            ana,
            Map.of("BAL-P", 1),
            null,
            0
        );

        verificar(
            "Compra inicial genera 2380 puntos",

            igual(
                sistema1.getTienda().getSaldoPuntos(ana),
                2380
            )
        );

        // GUARDAR PUNTOS EN DISCO

        PersistenciaFidelidad.guardar(
            sistema1.getFidelidad(),
            archivo
        );

        verificar(
            "Archivo de fidelidad creado fuera de src",

            Files.isRegularFile(archivo)
        );

        // SIMULAR CIERRE Y REINICIO DEL SISTEMA

        GestorFidelidad restaurada =
            PersistenciaFidelidad.cargar(archivo);

        SistemaClub sistema2 =
            nuevoSistema(restaurada);

        verificar(
            "Saldo recuperado de 2380 puntos",

            igual(
                sistema2.getFidelidad().getSaldo(ana),
                2380
            )
        );

        verificar(
            "Tienda y cafeteria reciben el mismo saldo restaurado",

            igual(
                sistema2.getTienda().getSaldoPuntos(ana),
                2380
            ) &&

            igual(
                sistema2.getCafeteria().getSaldoPuntos(ana),
                2380
            )
        );

        // REGISTRAR BEBIDA EN SISTEMA RECUPERADO

        sistema2.getCafeteria().registrarProducto(
            new Bebida(
                "AGU-P",
                "Agua",
                5000,
                Bebida.Temperatura.FRIA
            )
        );

        // REDIMIR PUNTOS RECUPERADOS EN CAFETERIA

        var compraCafe =
            sistema2.getCafeteria().vender(
                ana,
                Map.of("AGU-P", 1),
                null,
                true,
                10,
                1000
            );

        verificar(
            "Puntos restaurados permiten comprar en cafeteria",

            igual(
                compraCafe.getTotalPagado(),
                4900
            )
        );

        verificar(
            "Nuevo saldo tras cafeteria = 1478",

            igual(
                sistema2.getTienda().getSaldoPuntos(ana),
                1478
            )
        );

        // GUARDAR POR SEGUNDA VEZ

        PersistenciaFidelidad.guardar(
            sistema2.getFidelidad(),
            archivo
        );

        // SIMULAR SEGUNDO REINICIO

        SistemaClub sistema3 = nuevoSistema(
            PersistenciaFidelidad.cargar(archivo)
        );

        verificar(
            "Segundo reinicio conserva 1478 puntos",

            igual(
                sistema3.getCafeteria().getSaldoPuntos(ana),
                1478
            )
        );

        // REGISTRAR PRODUCTO EN TERCER SISTEMA

        sistema3.getTienda().registrarArticulo(
            new Articulo(
                "MED-P",
                "Media",
                "Deportes",
                20000
            ),
            2
        );

        // REDIMIR PUNTOS EN TIENDA

        var compraTienda =
            sistema3.getTienda().vender(
                ana,
                Map.of("MED-P", 1),
                null,
                1000
            );

        verificar(
            "Puntos restaurados se redimen tambien en tienda",

            igual(
                compraTienda.getTotalPagado(),
                22800
            )
        );

        verificar(
            "Saldo despues de tienda = 934",

            igual(
                sistema3.getFidelidad().getSaldo(ana),
                934
            )
        );

        // COMPROBAR CUENTAS INDEPENDIENTES

        verificar(
            "Los puntos de otro socio permanecen separados",

            igual(
                sistema3.getFidelidad().getSaldo(bob),
                0
            )
        );

        // ULTIMO GUARDADO

        PersistenciaFidelidad.guardar(
            sistema3.getFidelidad(),
            archivo
        );

        GestorFidelidad finalRecuperada =
            PersistenciaFidelidad.cargar(archivo);

        verificar(
            "Tercer guardado y carga conservan 934 puntos",

            igual(
                finalRecuperada.getSaldo(ana),
                934
            )
        );

        // RESULTADOS FINALES

        System.out.println(
            "---------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Saldo recuperado de Ana: "
            + finalRecuperada.getSaldo(ana)
        );

        System.out.println(
            "Archivo utilizado: "
            + archivo.toAbsolutePath()
        );
    }
}
