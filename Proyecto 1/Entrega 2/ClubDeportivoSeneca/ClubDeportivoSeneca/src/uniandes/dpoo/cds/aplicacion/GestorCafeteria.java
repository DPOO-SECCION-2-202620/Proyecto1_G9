
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.*;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class GestorCafeteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double IMPUESTO_CONSUMO = 0.08;

    // GESTOR DE PUNTOS COMPARTIDO

    private final GestorFidelidad fidelidad;

    // PRODUCTOS Y VENTAS

    private final Map<String, ProductoCafeteria> menu =
        new LinkedHashMap<>();

    private final List<VentaCafeteria> ventas =
        new ArrayList<>();

    // CONSTRUCTORES

    public GestorCafeteria() {
        this(new GestorFidelidad());
    }

    public GestorCafeteria(GestorFidelidad fidelidad) {

        if (fidelidad == null) {
            throw new IllegalArgumentException(
                "El gestor de fidelidad es obligatorio"
            );
        }

        this.fidelidad = fidelidad;
    }

    // METODOS AUXILIARES

    private static String clave(String texto) {

        return texto == null
            ? ""
            : texto.trim().toLowerCase(Locale.ROOT);
    }

    private static String sinTildes(String texto) {

        return Normalizer.normalize(
            clave(texto),
            Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    // REGISTRAR PRODUCTO

    public void registrarProducto(
            ProductoCafeteria producto)
            throws ReglaNegocioException {

        if (producto == null) {
            throw new ReglaNegocioException(
                "Producto invalido"
            );
        }

        String codigo = clave(producto.getCodigo());

        if (menu.containsKey(codigo)) {
            throw new ReglaNegocioException(
                "El producto ya existe"
            );
        }

        menu.put(codigo, producto);
    }

    // CONSULTAR ALERGENOS

    public Set<Alergeno> consultarAlergenos(
            String codigo)
            throws ReglaNegocioException {

        ProductoCafeteria producto =
            menu.get(clave(codigo));

        if (!(producto instanceof Snack)) {
            throw new ReglaNegocioException(
                "No es un snack registrado"
            );
        }

        return ((Snack) producto).getAlergenos();
    }

    // IDENTIFICAR INSTALACIONES RESTRINGIDAS

    public static boolean instalacionRestringida(
            Instalacion instalacion) {

        if (instalacion == null ||
            instalacion.getDisciplina() == null) {

            return false;
        }

        String nombre = sinTildes(
            instalacion.getDisciplina().getNombre()
        );

        return nombre.equals("padel") ||
            (
                instalacion.isTechada() &&
                (
                    nombre.equals("tenis de mesa") ||
                    nombre.equals("ping pong")
                )
            );
    }

    // VALIDAR INGRESO CON BEBIDA CALIENTE

    public void validarIngresoConBebidaCaliente(
            Socio socio,
            Instalacion instalacion)
            throws ReglaNegocioException {

        if (socio == null || instalacion == null) {
            throw new ReglaNegocioException(
                "Socio o instalacion invalidos"
            );
        }

        if (instalacionRestringida(instalacion)) {
            throw new ReglaNegocioException(
                "Prohibido ingresar bebidas calientes a esta instalacion"
            );
        }
    }

    // CONSULTAR SALDO COMPARTIDO

    public double getSaldoPuntos(Socio socio) {
        return fidelidad.getSaldo(socio);
    }

    // CONSULTAR HISTORIAL

    public List<VentaCafeteria> getVentas() {
        return Collections.unmodifiableList(ventas);
    }

    // REALIZAR VENTA

    public VentaCafeteria vender(
            Socio comprador,
            Map<String, Integer> carrito,
            Instalacion ubicacion,
            boolean alergenosInformados,
            double porcentajePropina,
            int puntosRedimir)
            throws ReglaNegocioException {

        // VALIDAR COMPRADOR

        if (comprador == null ||
            clave(comprador.getLogin()).isEmpty()) {

            throw new ReglaNegocioException(
                "El comprador debe ser un socio valido"
            );
        }

        // VALIDAR CARRITO

        if (carrito == null || carrito.isEmpty()) {
            throw new ReglaNegocioException(
                "El carrito no puede estar vacio"
            );
        }

        // VALIDAR PROPINA Y PUNTOS

        if (!Double.isFinite(porcentajePropina) ||
            porcentajePropina < 0 ||
            porcentajePropina > 100 ||
            puntosRedimir < 0) {

            throw new ReglaNegocioException(
                "Propina o puntos invalidos"
            );
        }

        List<DetalleVentaCafeteria> detalles =
            new ArrayList<>();

        double subtotal = 0;

        // REVISAR PRODUCTOS

        for (Map.Entry<String, Integer> e :
                carrito.entrySet()) {

            ProductoCafeteria producto =
                menu.get(clave(e.getKey()));

            Integer cantidad = e.getValue();

            if (producto == null ||
                cantidad == null ||
                cantidad <= 0) {

                throw new ReglaNegocioException(
                    "Producto o cantidad invalidos"
                );
            }

            // INFORMAR ALERGENOS

            if (producto instanceof Snack &&
                !alergenosInformados) {

                throw new ReglaNegocioException(
                    "Informe los alergenos antes de vender el snack"
                );
            }

            // VALIDAR BEBIDAS CALIENTES

            if (producto instanceof Bebida &&
                ((Bebida) producto).esCaliente() &&
                instalacionRestringida(ubicacion)) {

                throw new ReglaNegocioException(
                    "No se despachan bebidas calientes en esta instalacion"
                );
            }

            DetalleVentaCafeteria detalle =
                new DetalleVentaCafeteria(
                    producto,
                    cantidad
                );

            detalles.add(detalle);

            subtotal += detalle.getSubtotal();
        }

        // CALCULAR IMPUESTO Y PROPINA

        subtotal = redondear(subtotal);

        double impuesto = redondear(
            subtotal * IMPUESTO_CONSUMO
        );

        double propina = redondear(
            subtotal * porcentajePropina / 100.0
        );

        double antesPuntos = redondear(
            subtotal + impuesto + propina
        );

        // VALIDAR PUNTOS

        fidelidad.validarRedencion(
            comprador,
            puntosRedimir,
            antesPuntos
        );

        double pagado = redondear(
            antesPuntos - puntosRedimir
        );

        // CREAR VENTA

        VentaCafeteria venta = new VentaCafeteria(
            comprador,
            detalles,
            subtotal,
            impuesto,
            propina,
            puntosRedimir,
            pagado
        );

        // ACTUALIZAR PUNTOS COMPARTIDOS

        fidelidad.aplicarCompra(
            comprador,
            puntosRedimir,
            pagado
        );

        ventas.add(venta);

        return venta;
    }
}
