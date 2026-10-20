
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Alergeno;
import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;
import uniandes.dpoo.cds.modelo.comercial.Snack;
import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto;

import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto.Estado;

import uniandes.dpoo.cds.modelo.comercial.UbicacionInventario;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaComercialIntegrada;

public class PruebaPersistenciaSugerencias {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // VERIFICAR RESULTADOS

    private static void verificar(
            String descripcion,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ")
            + descripcion
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // COMPROBAR OPERACIONES RECHAZADAS

    private static void rechazar(
            String descripcion,
            Accion accion) {

        try {

            accion.ejecutar();
            verificar(descripcion, false);

        } catch (ReglaNegocioException e) {

            verificar(descripcion, true);

        } catch (Exception e) {

            System.out.println(
                "Error inesperado: " + e
            );

            verificar(descripcion, false);
        }
    }

    private static boolean igual(double a, double b) {

        return Math.abs(a - b) < 0.001;
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // CONFIGURAR HORARIO

        LocalTime apertura =
            LocalTime.of(8, 0);

        LocalTime cierre =
            LocalTime.of(18, 0);

        EnumSet<DayOfWeek> dias =
            EnumSet.of(DayOfWeek.MONDAY);

        // ARCHIVO DE PRUEBA INDEPENDIENTE

        Path archivo = Path.of(
            "datos",
            "comercial-sugerencias-prueba.dat"
        );

        // CREAR USUARIOS

        Empleado empleado = new Empleado(
            "empleadoSug",
            "demo",
            "SUG-01"
        );

        Administrador admin = new Administrador(
            "adminSug",
            "demo"
        );

        Socio ana = new Socio(
            "anaSug",
            "demo",
            LocalDate.of(2002, 1, 1)
        );

        LocalDate hoy = LocalDate.now();

        // CREAR PRIMER SISTEMA

        SistemaClub primero = new SistemaClub(
            apertura,
            cierre,
            dias
        );

        // CREAR PRODUCTOS

        Articulo balon = new Articulo(
            "BAL-S",
            "Balon sugerido",
            "Balones",
            20000
        );

        Articulo medias = new Articulo(
            "MED-S",
            "Medias sugeridas",
            "Ropa",
            20000
        );

        Snack galleta = new Snack(
            "GAL-S",
            "Galletas",
            4000,
            Set.of(Alergeno.GLUTEN)
        );

        Bebida agua = new Bebida(
            "AGU-S",
            "Agua",
            5000,
            Bebida.Temperatura.FRIA
        );

        // PRIMERA SUGERENCIA: APROBADA

        SugerenciaProducto aprobada =
            primero.getSugerencias()
                .proponerArticulo(
                    empleado,
                    balon,
                    "Alta demanda"
                );

        primero.getSugerencias().aprobar(
            admin,
            aprobada.getNumero(),
            "Aprobado"
        );

        // SEGUNDA SUGERENCIA: RECHAZADA

        SugerenciaProducto rechazada =
            primero.getSugerencias()
                .proponerProductoCafeteria(
                    empleado,
                    galleta,
                    "Ampliar menu"
                );

        primero.getSugerencias().rechazar(
            admin,
            rechazada.getNumero(),
            "Falta validar proveedor"
        );

        // TERCERA SUGERENCIA: PENDIENTE CAFETERIA

        SugerenciaProducto pendienteCafe =
            primero.getSugerencias()
                .proponerProductoCafeteria(
                    empleado,
                    agua,
                    "Bebida nueva"
                );

        // CUARTA SUGERENCIA: PENDIENTE TIENDA

        SugerenciaProducto pendienteTienda =
            primero.getSugerencias()
                .proponerArticulo(
                    empleado,
                    medias,
                    "Nueva ropa"
                );

        // ABASTECER EL PRODUCTO APROBADO

        primero.getTienda().reabastecer(
            admin,
            "BAL-S",
            UbicacionInventario.TIENDA,
            2
        );

        // REALIZAR VENTA

        primero.getTienda().vender(
            ana,
            Map.of("BAL-S", 1),
            null,
            0
        );

        // REGISTRAR MENSUALIDAD

        primero.getReportesFinancieros()
            .registrarMensualidad(
                ana,
                YearMonth.from(hoy),
                hoy,
                100000
            );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA DE SUGERENCIAS"
        );

        System.out.println(
            "------------------------------------------"
        );

        // VALIDACIONES INICIALES

        verificar(
            "Cuatro sugerencias originales",

            primero.getSugerencias()
                .getSugerencias().size() == 4
        );

        verificar(
            "Dos sugerencias pendientes antes de guardar",

            primero.getSugerencias()
                .getPendientes().size() == 2
        );

        verificar(
            "El articulo aprobado ya tiene inventario y venta",

            primero.getTienda()
                .getExistencias("BAL-S") == 1 &&

            primero.getTienda()
                .getVentas().size() == 1
        );

        verificar(
            "Puntos y mensualidad iniciales",

            igual(
                primero.getFidelidad().getSaldo(ana),
                476
            ) &&

            primero.getReportesFinancieros()
                .getMensualidades().size() == 1
        );

        // GUARDAR SISTEMA

        PersistenciaComercialIntegrada.guardar(
            primero,
            archivo
        );

        verificar(
            "Archivo de sugerencias creado",

            Files.isRegularFile(archivo)
        );

        // PRIMER REINICIO

        SistemaClub segundo =
            PersistenciaComercialIntegrada.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        // VERIFICAR SUGERENCIAS RECUPERADAS

        verificar(
            "Se recuperan cuatro sugerencias y dos pendientes",

            segundo.getSugerencias()
                .getSugerencias().size() == 4 &&

            segundo.getSugerencias()
                .getPendientes().size() == 2
        );

        SugerenciaProducto rAprobada =
            segundo.getSugerencias()
                .getSugerencias().get(0);

        SugerenciaProducto rRechazada =
            segundo.getSugerencias()
                .getSugerencias().get(1);

        verificar(
            "Aprobacion y revisor se recuperaron",

            rAprobada.getEstado() == Estado.APROBADA &&

            rAprobada.getRevisor()
                .getLogin().equals("adminSug") &&

            rAprobada.getRespuesta()
                .equals("Aprobado") &&

            rAprobada.getFechaSolicitud()
                .equals(aprobada.getFechaSolicitud())
        );

        verificar(
            "Rechazo y motivo se recuperaron",

            rRechazada.getEstado() == Estado.RECHAZADA &&

            rRechazada.getRespuesta()
                .equals("Falta validar proveedor")
        );

        verificar(
            "La recuperacion no duplica el inventario ni las ventas",

            segundo.getTienda()
                .getExistencias("BAL-S") == 1 &&

            segundo.getTienda()
                .getVentas().size() == 1
        );

        verificar(
            "Mensualidad y puntos siguen recuperandose",

            segundo.getReportesFinancieros()
                .getMensualidades().size() == 1 &&

            igual(
                segundo.getFidelidad().getSaldo(ana),
                476
            )
        );

        // NO REPETIR DECISIONES ANTERIORES

        rechazar(
            "No volver a aprobar una sugerencia resuelta",

            () -> segundo.getSugerencias().aprobar(
                admin,
                rAprobada.getNumero(),
                "Repetir"
            )
        );

        rechazar(
            "No duplicar una sugerencia que sigue pendiente",

            () -> segundo.getSugerencias().proponerArticulo(
                empleado,
                medias,
                "Duplicada"
            )
        );

        // APROBAR PROPUESTAS QUE QUEDARON PENDIENTES

        segundo.getSugerencias().aprobar(
            admin,
            pendienteCafe.getNumero(),
            "Aprobada despues de cargar"
        );

        segundo.getSugerencias().aprobar(
            admin,
            pendienteTienda.getNumero(),
            "Aprobada despues de cargar"
        );

        verificar(
            "Aprobar pendientes conecta ambos catalogos restaurados",

            segundo.getTienda()
                .getExistencias("MED-S") == 0 &&

            segundo.getSugerencias()
                .getPendientes().isEmpty()
        );

        // COMPRAR PRODUCTO RECIEN APROBADO

        var compraAgua =
            segundo.getCafeteria().vender(
                ana,
                Map.of("AGU-S", 1),
                null,
                true,
                0,
                0
            );

        verificar(
            "La cafeteria restaurada vende el producto aprobado",

            igual(
                compraAgua.getTotalPagado(),
                5400
            ) &&

            igual(
                segundo.getTienda().getSaldoPuntos(ana),
                584
            )
        );

        // VOLVER A PROPONER PRODUCTO RECHAZADO

        SugerenciaProducto intento =
            segundo.getSugerencias()
                .proponerProductoCafeteria(
                    empleado,
                    galleta,
                    "Proveedor corregido"
                );

        verificar(
            "Nueva propuesta tras rechazo usa consecutivo 5",

            intento.getNumero() == 5
        );

        // APROBAR NUEVO INTENTO

        segundo.getSugerencias().aprobar(
            admin,
            intento.getNumero(),
            "Proveedor confirmado"
        );

        verificar(
            "El snack corregido aparece en cafeteria",

            segundo.getCafeteria()
                .consultarAlergenos("GAL-S")
                .contains(Alergeno.GLUTEN)
        );

        // COMPROBAR REPORTES ANTERIORES

        verificar(
            "Reporte financiero conserva ventas y mensualidad",

            segundo.getReportesFinancieros()
                .reporteDiario(hoy)
                .getNumeroOperaciones() == 3 &&

            igual(
                segundo.getReportesFinancieros()
                    .reporteDiario(hoy)
                    .getRecaudoTotal(),
                129200
            )
        );

        // GUARDAR POR SEGUNDA VEZ

        PersistenciaComercialIntegrada.guardar(
            segundo,
            archivo
        );

        // SEGUNDO REINICIO

        SistemaClub tercero =
            PersistenciaComercialIntegrada.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        verificar(
            "Segundo reinicio conserva cinco decisiones y sin pendientes",

            tercero.getSugerencias()
                .getSugerencias().size() == 5 &&

            tercero.getSugerencias()
                .getPendientes().isEmpty()
        );

        verificar(
            "Segundo reinicio conserva ventas, puntos y mensualidades",

            tercero.getCafeteria()
                .getVentas().size() == 1 &&

            igual(
                tercero.getFidelidad().getSaldo(ana),
                584
            ) &&

            tercero.getReportesFinancieros()
                .getMensualidades().size() == 1
        );

        // VERIFICAR QUE CONTINUA LA NUMERACION

        SugerenciaProducto ultima =
            tercero.getSugerencias()
                .proponerArticulo(
                    empleado,
                    new Articulo(
                        "RAQ-S",
                        "Raqueta",
                        "Raquetas",
                        30000
                    ),
                    "Otra idea"
                );

        verificar(
            "Consecutivo continua en 6 tras segundo reinicio",

            ultima.getNumero() == 6
        );

        // RESULTADOS FINALES

        System.out.println(
            "------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Sugerencias restauradas: "
            + tercero.getSugerencias()
                .getSugerencias().size()
        );

        System.out.println(
            "Saldo compartido restaurado: "
            + tercero.getFidelidad().getSaldo(ana)
        );
    }
}
