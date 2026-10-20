
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.SistemaClub;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;
import uniandes.dpoo.cds.modelo.comercial.ResumenFinanciero;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaComercialIntegrada;

public class PruebaPersistenciaMensualidades {

    private static int correctas = 0;
    private static int fallidas = 0;

    // COMPROBAR RESULTADOS

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

    // PRUEBA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // NUEVO ARCHIVO DE PRUEBA

        Path archivo = Path.of(
            "datos",
            "comercial-mensualidades-prueba.dat"
        );

        LocalTime apertura = LocalTime.of(8, 0);
        LocalTime cierre = LocalTime.of(18, 0);

        EnumSet<DayOfWeek> dias =
            EnumSet.of(DayOfWeek.MONDAY);

        LocalDate hoy = LocalDate.now();

        YearMonth mesActual = YearMonth.from(hoy);
        YearMonth mesAnterior = mesActual.minusMonths(1);

        LocalDate diaMesAnterior =
            mesAnterior.atDay(15);

        // CREAR SOCIOS

        Socio ana = new Socio(
            "anaMensualidad",
            "demo",
            LocalDate.of(2001, 1, 1)
        );

        Socio juan = new Socio(
            "juanMensualidad",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        // CREAR SISTEMA

        SistemaClub original = new SistemaClub(
            apertura,
            cierre,
            dias
        );

        // REGISTRAR PRODUCTOS

        original.getTienda().registrarArticulo(
            new Articulo(
                "BAL-M",
                "Balon",
                "Deportes",
                20000
            ),
            3
        );

        original.getCafeteria().registrarProducto(
            new Bebida(
                "AGU-M",
                "Agua",
                5000,
                Bebida.Temperatura.FRIA
            )
        );

        // REGISTRAR VENTAS

        original.getTienda().vender(
            ana,
            Map.of("BAL-M", 1),
            null,
            0
        );

        original.getCafeteria().vender(
            ana,
            Map.of("AGU-M", 1),
            null,
            true,
            0,
            0
        );

        // REGISTRAR MENSUALIDADES

        original.getReportesFinancieros()
            .registrarMensualidad(
                ana,
                mesActual,
                hoy,
                100000
            );

        original.getReportesFinancieros()
            .registrarMensualidad(
                juan,
                mesAnterior,
                diaMesAnterior,
                80000
            );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA DE MENSUALIDADES"
        );

        System.out.println(
            "--------------------------------------------"
        );

        // VALIDAR ESTADO ORIGINAL

        verificar(
            "Dos mensualidades antes de guardar",

            original.getReportesFinancieros()
                .getMensualidades().size() == 2
        );

        verificar(
            "Recaudo actual antes de guardar: 129200",

            igual(
                original.getReportesFinancieros()
                    .reporteDiario(hoy)
                    .getRecaudoTotal(),
                129200
            )
        );

        // GUARDAR SISTEMA

        PersistenciaComercialIntegrada.guardar(
            original,
            archivo
        );

        verificar(
            "Archivo de prueba creado",
            Files.isRegularFile(archivo)
        );

        // RECUPERAR SISTEMA

        SistemaClub recuperado =
            PersistenciaComercialIntegrada.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        verificar(
            "Mensualidades recuperadas: 2",

            recuperado.getReportesFinancieros()
                .getMensualidades().size() == 2
        );

        verificar(
            "Ventas recuperadas: tienda y cafeteria",

            recuperado.getTienda().getVentas().size() == 1 &&

            recuperado.getCafeteria().getVentas().size() == 1
        );

        // CONSULTAR REPORTE RESTAURADO

        ResumenFinanciero diario =
            recuperado.getReportesFinancieros()
                .reporteDiario(hoy);

        verificar(
            "Reporte diario incluye mensualidad y ventas",

            diario.getPagosMensualidad() == 1 &&

            diario.getVentasTienda() == 1 &&

            diario.getVentasCafeteria() == 1 &&

            igual(
                diario.getRecaudoTotal(),
                129200
            )
        );

        // CONSULTAR MES ANTERIOR

        verificar(
            "Mensualidad del mes anterior recuperada",

            igual(
                recuperado.getReportesFinancieros()
                    .reporteMensual(mesAnterior)
                    .getRecaudoTotal(),
                80000
            )
        );

        // CONSULTAR FIDELIDAD RESTAURADA

        verificar(
            "Saldo de puntos recuperado: 584",

            igual(
                recuperado.getTienda()
                    .getSaldoPuntos(ana),
                584
            ) &&

            igual(
                recuperado.getCafeteria()
                    .getSaldoPuntos(ana),
                584
            )
        );

        // VERIFICAR QUE NO DUPLIQUE PAGOS

        try {

            recuperado.getReportesFinancieros()
                .registrarMensualidad(
                    ana,
                    mesActual,
                    hoy,
                    100000
                );

            verificar(
                "Rechazar mensualidad duplicada tras cargar",
                false
            );

        } catch (ReglaNegocioException e) {

            verificar(
                "Rechazar mensualidad duplicada tras cargar",
                true
            );
        }

        // AGREGAR NUEVA MENSUALIDAD

        recuperado.getReportesFinancieros()
            .registrarMensualidad(
                juan,
                mesActual,
                hoy,
                70000
            );

        verificar(
            "Agregar nueva mensualidad tras recuperar",

            recuperado.getReportesFinancieros()
                .getMensualidades().size() == 3
        );

        verificar(
            "Nuevo recaudo diario: 199200",

            igual(
                recuperado.getReportesFinancieros()
                    .reporteDiario(hoy)
                    .getRecaudoTotal(),
                199200
            )
        );

        // GUARDAR POR SEGUNDA VEZ

        PersistenciaComercialIntegrada.guardar(
            recuperado,
            archivo
        );

        // SEGUNDO REINICIO

        SistemaClub segundoReinicio =
            PersistenciaComercialIntegrada.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        verificar(
            "Segundo reinicio conserva las 3 mensualidades",

            segundoReinicio.getReportesFinancieros()
                .getMensualidades().size() == 3
        );

        verificar(
            "Segundo reinicio conserva recaudo total",

            igual(
                segundoReinicio.getReportesFinancieros()
                    .reporteDiario(hoy)
                    .getRecaudoTotal(),
                199200
            )
        );

        verificar(
            "No se perdieron puntos ni inventario",

            igual(
                segundoReinicio.getTienda()
                    .getSaldoPuntos(ana),
                584
            ) &&

            segundoReinicio.getTienda()
                .getExistencias("BAL-M") == 2
        );

        // RESULTADOS FINALES

        System.out.println(
            "--------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Mensualidades recuperadas: "
            + segundoReinicio.getReportesFinancieros()
                .getMensualidades().size()
        );

        System.out.println(
            "Recaudo diario recuperado: "
            + segundoReinicio.getReportesFinancieros()
                .reporteDiario(hoy)
                .getRecaudoTotal()
        );
    }
}
