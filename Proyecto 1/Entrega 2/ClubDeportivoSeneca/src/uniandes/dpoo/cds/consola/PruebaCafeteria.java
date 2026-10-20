
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.aplicacion.GestorCafeteria;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.*;
import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaCafeteria {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void aceptar(
            String nombre, Accion accion) {

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
            String nombre, Accion accion) {

        try {
            accion.ejecutar();
            System.out.println(
                "[FALLO] " + nombre + " (se permitio)"
            );
            fallidas++;

        } catch (ReglaNegocioException e) {
            System.out.println(
                "[OK] " + nombre + " -> " + e.getMessage()
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
            String nombre, boolean cumple) {

        System.out.println(
            (cumple ? "[OK] " : "[FALLO] ") + nombre
        );

        if (cumple) {
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

        GestorCafeteria cafeteria =
            new GestorCafeteria();

        Socio ana = new Socio(
            "ana01",
            "claveDemo",
            LocalDate.of(2003, 1, 10)
        );

        // CREAR PRODUCTOS

        Bebida fria = new Bebida(
            "B-01",
            "Limonada",
            5000,
            Bebida.Temperatura.FRIA
        );

        Bebida caliente = new Bebida(
            "B-02",
            "Cafe",
            6000,
            Bebida.Temperatura.CALIENTE
        );

        Snack snack = new Snack(
            "S-01",
            "Galletas",
            4000,
            Set.of(
                Alergeno.GLUTEN,
                Alergeno.LACTEOS
            )
        );

        // CREAR INSTALACIONES

        Instalacion padel = new Instalacion(
            "PAD-01",
            new Disciplina("Padel", 1),
            4,
            false
        );

        Instalacion mesaCubierta = new Instalacion(
            "MES-01",
            new Disciplina("Tenis de mesa", 1),
            2,
            true
        );

        Instalacion mesaAbierta = new Instalacion(
            "MES-02",
            new Disciplina("Tenis de mesa", 1),
            2,
            false
        );

        Instalacion tenis = new Instalacion(
            "TEN-01",
            new Disciplina("Tenis", 1),
            2,
            false
        );

        System.out.println("PRUEBAS DE CAFETERIA");
        System.out.println("--------------------------------------");

        // REGISTRAR PRODUCTOS

        aceptar("Registrar bebidas y snack", () -> {

            cafeteria.registrarProducto(fria);
            cafeteria.registrarProducto(caliente);
            cafeteria.registrarProducto(snack);
        });

        rechazar("Rechazar codigo repetido", () ->
            cafeteria.registrarProducto(fria)
        );

        rechazar("Rechazar producto nulo", () ->
            cafeteria.registrarProducto(null)
        );

        // ALERGENOS

        System.out.println(
            "Aviso de alergenos (S-01): "
            + cafeteria.consultarAlergenos("S-01")
        );

        verificar("Alerta de gluten y lacteos",
            cafeteria.consultarAlergenos("S-01")
                .containsAll(
                    Set.of(
                        Alergeno.GLUTEN,
                        Alergeno.LACTEOS
                    )
                )
        );

        rechazar(
            "No consultar alergenos de una bebida",
            () -> cafeteria.consultarAlergenos("B-01")
        );

        rechazar(
            "No vender snack sin informar alergenos",
            () -> cafeteria.vender(
                ana,
                Map.of("S-01", 1),
                null,
                false,
                10,
                0
            )
        );

        // RESTRICCIONES DE BEBIDAS CALIENTES

        rechazar(
            "No vender bebida caliente en padel",
            () -> cafeteria.vender(
                ana,
                Map.of("B-02", 1),
                padel,
                true,
                10,
                0
            )
        );

        rechazar(
            "No vender bebida caliente en mesa techada",
            () -> cafeteria.vender(
                ana,
                Map.of("B-02", 1),
                mesaCubierta,
                true,
                10,
                0
            )
        );

        rechazar(
            "No ingresar con bebida caliente a padel",
            () -> cafeteria.validarIngresoConBebidaCaliente(
                ana, padel
            )
        );

        rechazar(
            "No ingresar con bebida caliente a mesa techada",
            () -> cafeteria.validarIngresoConBebidaCaliente(
                ana, mesaCubierta
            )
        );

        aceptar(
            "Mesa abierta permite bebida caliente",
            () -> cafeteria.validarIngresoConBebidaCaliente(
                ana, mesaAbierta
            )
        );

        aceptar(
            "Cancha de tenis permite bebida caliente",
            () -> cafeteria.validarIngresoConBebidaCaliente(
                ana, tenis
            )
        );

        // VALIDACIONES DE VENTA

        rechazar("Producto inexistente", () ->
            cafeteria.vender(
                ana,
                Map.of("XX", 1),
                null,
                true,
                10,
                0
            )
        );

        rechazar("Cantidad negativa", () ->
            cafeteria.vender(
                ana,
                Map.of("B-01", -1),
                null,
                true,
                10,
                0
            )
        );

        rechazar("Propina negativa", () ->
            cafeteria.vender(
                ana,
                Map.of("B-01", 1),
                null,
                true,
                -1,
                0
            )
        );

        rechazar("Propina superior al 100 por ciento", () ->
            cafeteria.vender(
                ana,
                Map.of("B-01", 1),
                null,
                true,
                150,
                0
            )
        );

        // PRIMERA VENTA

        final VentaCafeteria[] compra1 =
            new VentaCafeteria[1];

        aceptar(
            "Vender bebida fria y snack informado",
            () -> compra1[0] = cafeteria.vender(
                ana,
                Map.of("B-01", 1, "S-01", 1),
                null,
                true,
                10,
                0
            )
        );

        verificar(
            "Impuesto 8 por ciento, propina 10 por ciento y total",

            igual(compra1[0].getSubtotal(), 9000) &&
            igual(compra1[0].getImpuesto(), 720) &&
            igual(compra1[0].getPropina(), 900) &&
            igual(compra1[0].getTotalPagado(), 10620)
        );

        verificar(
            "Dos productos detallados",
            compra1[0].getDetalles().size() == 2
        );

        verificar(
            "Puntos tras primera compra 212.40",
            igual(cafeteria.getSaldoPuntos(ana), 212.40)
        );

        // SEGUNDA VENTA SIN PROPINA

        final VentaCafeteria[] compra2 =
            new VentaCafeteria[1];

        aceptar(
            "Vender cafe fuera de cancha sin propina",
            () -> compra2[0] = cafeteria.vender(
                ana,
                Map.of("B-02", 1),
                null,
                true,
                0,
                0
            )
        );

        verificar(
            "Propina cero y total 6480",

            igual(compra2[0].getPropina(), 0) &&
            igual(compra2[0].getTotalPagado(), 6480)
        );

        verificar(
            "Puntos acumulados 342",
            igual(cafeteria.getSaldoPuntos(ana), 342)
        );

        // REDENCION DE PUNTOS

        rechazar(
            "No redimir mas puntos que los disponibles",
            () -> cafeteria.vender(
                ana,
                Map.of("B-01", 1),
                padel,
                true,
                10,
                400
            )
        );

        final VentaCafeteria[] compra3 =
            new VentaCafeteria[1];

        aceptar(
            "Vender bebida fria en padel y redimir 300 puntos",
            () -> compra3[0] = cafeteria.vender(
                ana,
                Map.of("B-01", 1),
                padel,
                true,
                10,
                300
            )
        );

        verificar(
            "Pago final 5600 y puntos restantes 154",

            igual(compra3[0].getTotalPagado(), 5600) &&
            igual(cafeteria.getSaldoPuntos(ana), 154)
        );

        // VERIFICACION FINAL

        verificar(
            "Tres ventas registradas",
            cafeteria.getVentas().size() == 3
        );

        System.out.println("--------------------------------------");

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Ventas registradas: "
            + cafeteria.getVentas().size()
        );

        System.out.println(
            "Puntos de Ana: "
            + cafeteria.getSaldoPuntos(ana)
        );
    }
}
