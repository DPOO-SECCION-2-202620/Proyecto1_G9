
package uniandes.dpoo.cds.consola;

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

import uniandes.dpoo.cds.modelo.competencias.Competencia;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaIntegracionInicial {

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

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // CREAR SISTEMA COMPLETO

        SistemaClub sistema = new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY
            )
        );

        // CREAR USUARIOS

        Socio socio = new Socio(
            "anaIntegracion",
            "demo",
            LocalDate.of(2002, 1, 1)
        );

        Empleado empleado = new Empleado(
            "empleadoIntegracion",
            "demo",
            "CODINT"
        );

        Administrador admin = new Administrador(
            "adminIntegracion",
            "demo"
        );

        System.out.println(
            "PRUEBAS DE INTEGRACION INICIAL"
        );

        System.out.println(
            "--------------------------------------"
        );

        // INTEGRAR EQUIPOS Y COMPETENCIAS

        Deporte futbol = new Deporte("Futbol", 7);

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        Equipo equipo = new Equipo(
            "Seneca Integrado",
            futbol,
            mayores,
            15
        );

        sistema.getClub().registrarEquipo(equipo);

        Competencia competencia = new Competencia(
            "ASCUN Integrado",
            "ASCUN",
            "2026-2",
            futbol,
            mayores,
            "GRUPOS_Y_ELIMINACION",
            7
        );

        sistema.getCompetencias()
            .registrarCompetencia(competencia);

        verificar(
            "Equipo registrado visible desde competencias",

            sistema.getCompetencias()
                .inscribirEquipo(
                    equipo,
                    competencia
                ) != null
        );

        // PROPONER ARTICULO

        Articulo articulo = new Articulo(
            "BAL-INT",
            "Balon integrado",
            "Balones",
            50000
        );

        int propuestaTienda =
            sistema.getSugerencias()
                .proponerArticulo(
                    empleado,
                    articulo,
                    "Nuevo producto"
                ).getNumero();

        // APROBAR ARTICULO

        sistema.getSugerencias().aprobar(
            admin,
            propuestaTienda,
            "Aprobado"
        );

        verificar(
            "Sugerencia aprobada aparece en tienda",

            sistema.getTienda()
                .getExistencias("BAL-INT") == 0
        );

        // REABASTECER Y VENDER

        sistema.getTienda().reabastecer(
            admin,
            "BAL-INT",
            UbicacionInventario.TIENDA,
            3
        );

        sistema.getTienda().vender(
            socio,
            Map.of("BAL-INT", 1),
            null,
            0
        );

        verificar(
            "Venta comparte inventario de tienda",

            sistema.getTienda()
                .getExistencias("BAL-INT") == 2
        );

        // PROPONER BEBIDA

        Bebida bebida = new Bebida(
            "BEB-INT",
            "Agua",
            5000,
            Bebida.Temperatura.FRIA
        );

        int propuestaCafe =
            sistema.getSugerencias()
                .proponerProductoCafeteria(
                    empleado,
                    bebida,
                    "Ampliar menu"
                ).getNumero();

        // APROBAR Y VENDER BEBIDA

        sistema.getSugerencias().aprobar(
            admin,
            propuestaCafe,
            "Aprobada"
        );

        sistema.getCafeteria().vender(
            socio,
            Map.of("BEB-INT", 1),
            null,
            true,
            0,
            0
        );

        verificar(
            "Sugerencia aprobada aparece en cafeteria",

            sistema.getCafeteria()
                .getVentas().size() == 1
        );

        // CONSULTAR REPORTE FINANCIERO CENTRAL

        ResumenFinanciero reporte =
            sistema.getReportesFinancieros()
                .reporteDiario(LocalDate.now());

        verificar(
            "Reporte consolida la tienda y cafeteria",

            reporte.getVentasTienda() == 1 &&

            reporte.getVentasCafeteria() == 1 &&

            Math.abs(
                reporte.getRecaudoTotal() - 64900.0
            ) < 0.001
        );

        // COMPROBAR DEMAS GESTORES

        verificar(
            "Todos los gestores permanecen disponibles",

            sistema.getEntrenamientos() != null &&

            sistema.getPartidosInternos() != null &&

            sistema.getPartidosOficiales() != null &&

            sistema.getPartidosIndividuales() != null &&

            sistema.getEstadisticas() != null &&

            sistema.getTurnos() != null &&

            sistema.getTablaLiga() != null &&

            sistema.getReportesDeportivos() != null
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
    }
}
