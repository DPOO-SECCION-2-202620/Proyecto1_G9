
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.aplicacion.GestorCafeteria;
import uniandes.dpoo.cds.aplicacion.GestorSugerenciasProducto;
import uniandes.dpoo.cds.aplicacion.GestorTienda;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Alergeno;
import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.Bebida;
import uniandes.dpoo.cds.modelo.comercial.Snack;
import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto;
import uniandes.dpoo.cds.modelo.comercial.UbicacionInventario;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaSugerenciasProducto {

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
                "[FALLO] " + titulo + " (fue aceptado)"
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

    // VERIFICAR RESULTADO

    private static void verificar(
            String titulo,
            boolean cumple) {

        System.out.println(
            (cumple ? "[OK] " : "[FALLO] ") + titulo
        );

        if (cumple) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    public static void main(String[] args)
            throws Exception {

        // CREAR GESTORES

        GestorTienda tienda = new GestorTienda();

        GestorCafeteria cafeteria =
            new GestorCafeteria();

        GestorSugerenciasProducto gestor =
            new GestorSugerenciasProducto(
                tienda,
                cafeteria
            );

        // CREAR USUARIOS

        Empleado empleado = new Empleado(
            "empleado01",
            "demo",
            "EMP01"
        );

        Administrador admin = new Administrador(
            "admin01",
            "demo"
        );

        Socio socio = new Socio(
            "socio01",
            "demo",
            LocalDate.of(2002, 1, 1)
        );

        // CREAR PRODUCTOS PROPUESTOS

        Articulo balon = new Articulo(
            "BAL-N",
            "Balon nuevo",
            "Balones",
            30000
        );

        Snack snack = new Snack(
            "GAL-N",
            "Galletas",
            4000,
            Set.of(Alergeno.GLUTEN)
        );

        Bebida bebida = new Bebida(
            "CAF-N",
            "Cafe",
            6000,
            Bebida.Temperatura.CALIENTE
        );

        System.out.println(
            "PRUEBAS DE SUGERENCIAS DE PRODUCTOS"
        );

        System.out.println(
            "----------------------------------------"
        );

        // VALIDAR PROPUESTAS

        rechazar(
            "Articulo nulo rechazado",
            () -> gestor.proponerArticulo(
                empleado, null, "Necesario"
            )
        );

        rechazar(
            "Empleado nulo rechazado",
            () -> gestor.proponerArticulo(
                null, balon, "Necesario"
            )
        );

        rechazar(
            "Justificacion vacia rechazada",
            () -> gestor.proponerArticulo(
                empleado, balon, " "
            )
        );

        // PROPONER ARTICULO

        SugerenciaProducto[] propuestaBalon =
            new SugerenciaProducto[1];

        aceptar(
            "Empleado propone balon",
            () -> propuestaBalon[0] =
                gestor.proponerArticulo(
                    empleado,
                    balon,
                    "Mayor demanda"
                )
        );

        verificar(
            "Propuesta pendiente de tienda",

            propuestaBalon[0].getEstado()
                == SugerenciaProducto.Estado.PENDIENTE &&

            propuestaBalon[0].getArea()
                == SugerenciaProducto.Area.TIENDA &&

            gestor.getPendientes().size() == 1
        );

        rechazar(
            "No repetir propuesta pendiente",
            () -> gestor.proponerArticulo(
                empleado, balon, "Duplicado"
            )
        );

        // VALIDAR APROBACIONES

        rechazar(
            "No aprobar sin administrador",
            () -> gestor.aprobar(
                null,
                propuestaBalon[0].getNumero(),
                "Bien"
            )
        );

        rechazar(
            "No aprobar propuesta inexistente",
            () -> gestor.aprobar(
                admin,
                999,
                "Bien"
            )
        );

        // APROBAR ARTICULO

        aceptar(
            "Administrador aprueba balon",
            () -> gestor.aprobar(
                admin,
                propuestaBalon[0].getNumero(),
                "Aprobado"
            )
        );

        verificar(
            "Articulo aprobado inicia sin existencias",

            propuestaBalon[0].getEstado()
                == SugerenciaProducto.Estado.APROBADA &&

            tienda.getExistencias("BAL-N") == 0
        );

        rechazar(
            "No aprobar dos veces",
            () -> gestor.aprobar(
                admin,
                propuestaBalon[0].getNumero(),
                "Otra vez"
            )
        );

        // ABASTECER ARTICULO APROBADO

        aceptar(
            "Administrador abastece nuevo articulo",
            () -> tienda.reabastecer(
                admin,
                "BAL-N",
                UbicacionInventario.TIENDA,
                3
            )
        );

        // VENDER ARTICULO

        aceptar(
            "Socio compra articulo aprobado",
            () -> tienda.vender(
                socio,
                Map.of("BAL-N", 2),
                null,
                0
            )
        );

        verificar(
            "La compra descuenta existencias",

            tienda.getExistencias("BAL-N") == 1 &&

            tienda.getVentas().size() == 1
        );

        // PROPONER SNACK

        SugerenciaProducto[] propuestaSnack =
            new SugerenciaProducto[1];

        aceptar(
            "Empleado propone snack",
            () -> propuestaSnack[0] =
                gestor.proponerProductoCafeteria(
                    empleado,
                    snack,
                    "Opcion con alergenos identificados"
                )
        );

        rechazar(
            "No repetir propuesta de snack",
            () -> gestor.proponerProductoCafeteria(
                empleado,
                snack,
                "Duplicado"
            )
        );

        // VALIDAR RECHAZOS

        rechazar(
            "Rechazo requiere motivo",
            () -> gestor.rechazar(
                admin,
                propuestaSnack[0].getNumero(),
                " "
            )
        );

        aceptar(
            "Administrador rechaza snack con motivo",
            () -> gestor.rechazar(
                admin,
                propuestaSnack[0].getNumero(),
                "Revisar proveedor"
            )
        );

        verificar(
            "Rechazo conserva motivo y responsable",

            propuestaSnack[0].getEstado()
                == SugerenciaProducto.Estado.RECHAZADA &&

            propuestaSnack[0].getRespuesta()
                .equals("Revisar proveedor") &&

            propuestaSnack[0].getRevisor() == admin
        );

        rechazar(
            "Rechazo no registro el snack",
            () -> cafeteria.consultarAlergenos(
                "GAL-N"
            )
        );

        rechazar(
            "No cambiar decision ya resuelta",
            () -> gestor.aprobar(
                admin,
                propuestaSnack[0].getNumero(),
                "Cambio de opinion"
            )
        );

        // NUEVA PROPUESTA TRAS RECHAZO

        SugerenciaProducto[] segundaPropuesta =
            new SugerenciaProducto[1];

        aceptar(
            "Nuevo intento tras rechazo",
            () -> segundaPropuesta[0] =
                gestor.proponerProductoCafeteria(
                    empleado,
                    snack,
                    "Ahora con proveedor verificado"
                )
        );

        aceptar(
            "Aprobar nueva propuesta de snack",
            () -> gestor.aprobar(
                admin,
                segundaPropuesta[0].getNumero(),
                "Proveedor validado"
            )
        );

        verificar(
            "Snack disponible con alergenos",

            cafeteria.consultarAlergenos("GAL-N")
                .contains(Alergeno.GLUTEN)
        );

        // PROPONER BEBIDA

        SugerenciaProducto[] propuestaBebida =
            new SugerenciaProducto[1];

        aceptar(
            "Empleado propone bebida",
            () -> propuestaBebida[0] =
                gestor.proponerProductoCafeteria(
                    empleado,
                    bebida,
                    "Ampliar menu"
                )
        );

        aceptar(
            "Administrador aprueba bebida",
            () -> gestor.aprobar(
                admin,
                propuestaBebida[0].getNumero(),
                "Bien"
            )
        );

        // COMPROBAR VENTA EN CAFETERIA

        aceptar(
            "Cafeteria vende la bebida aprobada",
            () -> cafeteria.vender(
                socio,
                Map.of("CAF-N", 1),
                null,
                true,
                0,
                0
            )
        );

        // VERIFICACION FINAL

        verificar(
            "Tres productos aprobados y uno rechazado",

            gestor.getSugerencias().size() == 4 &&

            gestor.getPendientes().isEmpty() &&

            cafeteria.getVentas().size() == 1
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
            "Sugerencias registradas: "
            + gestor.getSugerencias().size()
        );

        System.out.println(
            "Sugerencias pendientes: "
            + gestor.getPendientes().size()
        );
    }
}
