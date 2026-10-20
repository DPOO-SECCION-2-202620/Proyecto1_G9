
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.*;
import uniandes.dpoo.cds.modelo.usuarios.*;

public class GestorTienda implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final double IVA = 0.19;

    private final GestorFidelidad fidelidad;

    private final Map<String, Inventario> inventarios =
        new LinkedHashMap<>();

    private final Map<String, Inventario> bodega =
        new LinkedHashMap<>();

    private final Map<String, Integer> minimosTienda =
        new LinkedHashMap<>();

    private final Map<String, Empleado> codigosEmpleados =
        new LinkedHashMap<>();

    private final List<MovimientoInventario> movimientos =
        new ArrayList<>();

    private final List<VentaTienda> ventas =
        new ArrayList<>();

    // CONSTRUCTORES

    public GestorTienda() {
        this(new GestorFidelidad());
    }

    public GestorTienda(GestorFidelidad fidelidad) {

        if (fidelidad == null) {
            throw new IllegalArgumentException(
                "El gestor de fidelidad es obligatorio"
            );
        }

        this.fidelidad = fidelidad;
    }

    // METODOS AUXILIARES

    private static String clave(String valor) {

        return valor == null
            ? ""
            : valor.trim().toLowerCase(Locale.ROOT);
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private static void validarAdmin(Administrador admin)
            throws ReglaNegocioException {

        if (admin == null ||
            clave(admin.getLogin()).isEmpty()) {

            throw new ReglaNegocioException(
                "La operacion requiere un administrador"
            );
        }
    }

    // BUSCAR INVENTARIO

    private Inventario buscar(
            String codigo,
            UbicacionInventario ubicacion)
            throws ReglaNegocioException {

        if (ubicacion == null) {
            throw new ReglaNegocioException(
                "La ubicacion es obligatoria"
            );
        }

        Inventario inventario =
            (ubicacion == UbicacionInventario.TIENDA
                ? inventarios
                : bodega).get(clave(codigo));

        if (inventario == null) {
            throw new ReglaNegocioException(
                "Articulo no registrado"
            );
        }

        return inventario;
    }

    // REGISTRAR ARTICULO

    public void registrarArticulo(
            Articulo articulo,
            int cantidadInicial)
            throws ReglaNegocioException {

        if (articulo == null || cantidadInicial < 0) {
            throw new ReglaNegocioException(
                "Articulo o cantidad invalidos"
            );
        }

        String codigo = clave(articulo.getCodigo());

        if (inventarios.containsKey(codigo)) {
            throw new ReglaNegocioException(
                "El articulo ya existe"
            );
        }

        inventarios.put(
            codigo,
            new Inventario(articulo, cantidadInicial)
        );

        bodega.put(
            codigo,
            new Inventario(articulo, 0)
        );

        minimosTienda.put(codigo, 0);
    }

    // REGISTRAR EMPLEADO

    public void registrarEmpleado(Empleado empleado)
            throws ReglaNegocioException {

        if (empleado == null ||
            clave(empleado.getLogin()).isEmpty() ||
            clave(empleado.getCodigoDescuento()).isEmpty()) {

            throw new ReglaNegocioException(
                "Empleado o codigo invalido"
            );
        }

        String codigo =
            clave(empleado.getCodigoDescuento());

        if (codigosEmpleados.containsKey(codigo)) {
            throw new ReglaNegocioException(
                "El codigo de descuento ya existe"
            );
        }

        codigosEmpleados.put(codigo, empleado);
    }

    // CONSULTAR EXISTENCIAS

    public int getExistencias(String codigo)
            throws ReglaNegocioException {

        return getExistencias(
            codigo,
            UbicacionInventario.TIENDA
        );
    }

    public int getExistencias(
            String codigo,
            UbicacionInventario ubicacion)
            throws ReglaNegocioException {

        return buscar(codigo, ubicacion).getCantidad();
    }

    // REABASTECER

    public void reabastecer(
            Administrador admin,
            String codigo,
            UbicacionInventario ubicacion,
            int cantidad)
            throws ReglaNegocioException {

        validarAdmin(admin);

        if (cantidad <= 0) {
            throw new ReglaNegocioException(
                "La cantidad debe ser positiva"
            );
        }

        Inventario inventario =
            buscar(codigo, ubicacion);

        inventario.reabastecer(cantidad);

        movimientos.add(
            new MovimientoInventario(
                MovimientoInventario.Tipo.REABASTECIMIENTO,
                inventario.getArticulo().getCodigo(),
                null,
                ubicacion,
                cantidad,
                admin.getLogin()
            )
        );
    }

    // TRASLADAR INVENTARIO

    public void trasladar(
            Administrador admin,
            String codigo,
            UbicacionInventario origen,
            UbicacionInventario destino,
            int cantidad)
            throws ReglaNegocioException {

        validarAdmin(admin);

        if (origen == null ||
            destino == null ||
            origen == destino ||
            cantidad <= 0) {

            throw new ReglaNegocioException(
                "Traslado invalido"
            );
        }

        Inventario desde = buscar(codigo, origen);
        Inventario hacia = buscar(codigo, destino);

        if (cantidad > desde.getCantidad()) {
            throw new ReglaNegocioException(
                "No hay existencias suficientes para trasladar"
            );
        }

        if (cantidad >
            Integer.MAX_VALUE - hacia.getCantidad()) {

            throw new ReglaNegocioException(
                "El inventario de destino excederia el limite"
            );
        }

        desde.descontar(cantidad);
        hacia.reabastecer(cantidad);

        movimientos.add(
            new MovimientoInventario(
                MovimientoInventario.Tipo.TRASLADO,
                desde.getArticulo().getCodigo(),
                origen,
                destino,
                cantidad,
                admin.getLogin()
            )
        );
    }

    // CONFIGURAR STOCK MINIMO

    public void configurarMinimo(
            Administrador admin,
            String codigo,
            int minimo)
            throws ReglaNegocioException {

        validarAdmin(admin);

        buscar(
            codigo,
            UbicacionInventario.TIENDA
        );

        if (minimo < 0) {
            throw new ReglaNegocioException(
                "El minimo no puede ser negativo"
            );
        }

        minimosTienda.put(clave(codigo), minimo);
    }

    // CONSULTAR ALERTAS DE STOCK

    public List<Articulo> getArticulosStockBajo() {

        List<Articulo> resultado =
            new ArrayList<>();

        for (Map.Entry<String, Inventario> e :
                inventarios.entrySet()) {

            if (minimosTienda.get(e.getKey()) > 0 &&
                e.getValue().getCantidad() <
                minimosTienda.get(e.getKey())) {

                resultado.add(
                    e.getValue().getArticulo()
                );
            }
        }

        return Collections.unmodifiableList(resultado);
    }

    // SUGERENCIAS DE REPOSICION

    public Map<String, Integer> getSugerenciasReposicion() {

        Map<String, Integer> resultado =
            new LinkedHashMap<>();

        for (Map.Entry<String, Inventario> e :
                inventarios.entrySet()) {

            int minimo =
                minimosTienda.get(e.getKey());

            int actual =
                e.getValue().getCantidad();

            if (minimo > 0 && actual < minimo) {

                long propuesto =
                    2L * minimo - actual;

                resultado.put(
                    e.getValue().getArticulo().getCodigo(),
                    (int) Math.min(
                        Integer.MAX_VALUE,
                        propuesto
                    )
                );
            }
        }

        return Collections.unmodifiableMap(resultado);
    }

    // CONSULTAR HISTORIALES

    public List<MovimientoInventario> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    public List<VentaTienda> getVentas() {
        return Collections.unmodifiableList(ventas);
    }

    // CONSULTAR FIDELIDAD COMPARTIDA

    public double getSaldoPuntos(Usuario comprador) {
        return fidelidad.getSaldo(comprador);
    }

    // REALIZAR VENTA

    public VentaTienda vender(
            Usuario comprador,
            Map<String, Integer> carrito,
            String codigoDescuento,
            int puntosRedimir)
            throws ReglaNegocioException {

        if (!(comprador instanceof Socio ||
              comprador instanceof Empleado) ||
            clave(comprador.getLogin()).isEmpty()) {

            throw new ReglaNegocioException(
                "El comprador debe ser socio o empleado"
            );
        }

        if (carrito == null || carrito.isEmpty()) {
            throw new ReglaNegocioException(
                "El carrito no puede estar vacio"
            );
        }

        if (puntosRedimir < 0) {
            throw new ReglaNegocioException(
                "Los puntos a redimir son invalidos"
            );
        }

        boolean conCodigo =
            !clave(codigoDescuento).isEmpty();

        if (conCodigo && puntosRedimir > 0) {
            throw new ReglaNegocioException(
                "No se acumulan codigos de descuento con otras promociones"
            );
        }

        double tasa = 0;

        if (conCodigo) {

            Empleado propietario =
                codigosEmpleados.get(
                    clave(codigoDescuento)
                );

            if (propietario == null) {
                throw new ReglaNegocioException(
                    "Codigo de descuento no registrado"
                );
            }

            if (comprador instanceof Empleado &&
                clave(propietario.getLogin())
                    .equals(clave(comprador.getLogin()))) {

                tasa = 0.15;

            } else if (comprador instanceof Socio) {

                tasa = 0.08;

            } else {

                throw new ReglaNegocioException(
                    "Codigo ajeno no valido para empleados"
                );
            }
        }

        // AGRUPAR PRODUCTOS POR CODIGO

        Map<String, Integer> cantidades =
            new LinkedHashMap<>();

        for (Map.Entry<String, Integer> e :
                carrito.entrySet()) {

            String codigo = clave(e.getKey());
            Integer cantidad = e.getValue();

            if (!inventarios.containsKey(codigo) ||
                cantidad == null ||
                cantidad <= 0) {

                throw new ReglaNegocioException(
                    "Articulo o cantidad invalida"
                );
            }

            int anterior =
                cantidades.getOrDefault(codigo, 0);

            if (cantidad >
                Integer.MAX_VALUE - anterior) {

                throw new ReglaNegocioException(
                    "Cantidad demasiado grande"
                );
            }

            cantidades.put(
                codigo,
                anterior + cantidad
            );
        }

        // COMPROBAR EXISTENCIAS Y CALCULAR SUBTOTAL

        List<DetalleVenta> detalles =
            new ArrayList<>();

        Map<Inventario, Integer> descontar =
            new LinkedHashMap<>();

        double subtotal = 0;

        for (Map.Entry<String, Integer> e :
                cantidades.entrySet()) {

            Inventario inventario =
                inventarios.get(e.getKey());

            int cantidad = e.getValue();

            if (cantidad > inventario.getCantidad()) {
                throw new ReglaNegocioException(
                    "Inventario insuficiente"
                );
            }

            DetalleVenta detalle =
                new DetalleVenta(
                    inventario.getArticulo(),
                    cantidad
                );

            detalles.add(detalle);
            descontar.put(inventario, cantidad);
            subtotal += detalle.getSubtotal();
        }

        // CALCULAR VALORES

        subtotal = redondear(subtotal);

        double descuento = redondear(
            subtotal * tasa
        );

        double impuesto = redondear(
            (subtotal - descuento) * IVA
        );

        double antesPuntos = redondear(
            subtotal - descuento + impuesto
        );

        fidelidad.validarRedencion(
            comprador,
            puntosRedimir,
            antesPuntos
        );

        double pagado = redondear(
            antesPuntos - puntosRedimir
        );

        VentaTienda venta = new VentaTienda(
            comprador,
            detalles,
            subtotal,
            descuento,
            impuesto,
            puntosRedimir,
            pagado
        );

        // DESCONTAR INVENTARIO

        for (Map.Entry<Inventario, Integer> e :
                descontar.entrySet()) {

            e.getKey().descontar(
                e.getValue()
            );

            movimientos.add(
                new MovimientoInventario(
                    MovimientoInventario.Tipo.VENTA,
                    e.getKey().getArticulo().getCodigo(),
                    UbicacionInventario.TIENDA,
                    null,
                    e.getValue(),
                    comprador.getLogin()
                )
            );
        }

        // ACTUALIZAR PUNTOS

        fidelidad.aplicarCompra(
            comprador,
            puntosRedimir,
            pagado
        );

        ventas.add(venta);

        return venta;
    }
}
