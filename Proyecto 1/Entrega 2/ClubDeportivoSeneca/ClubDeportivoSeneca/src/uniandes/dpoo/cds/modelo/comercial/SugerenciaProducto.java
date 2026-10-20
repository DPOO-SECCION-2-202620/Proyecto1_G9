
package uniandes.dpoo.cds.modelo.comercial;

import java.io.Serializable;
import java.time.LocalDateTime;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;

public class SugerenciaProducto implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Area {
        TIENDA,
        CAFETERIA
    }

    public enum Estado {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }

    private final int numero;
    private final Empleado solicitante;
    private final Articulo articulo;
    private final ProductoCafeteria productoCafeteria;
    private final String justificacion;
    private final LocalDateTime fechaSolicitud;

    private Estado estado = Estado.PENDIENTE;
    private Administrador revisor;
    private String respuesta = "";

    // CONSTRUCTOR PARA ARTICULOS DE TIENDA

    public SugerenciaProducto(
            int numero,
            Empleado solicitante,
            Articulo articulo,
            String justificacion) {

        this(
            numero,
            solicitante,
            articulo,
            null,
            justificacion
        );
    }

    // CONSTRUCTOR PARA PRODUCTOS DE CAFETERIA

    public SugerenciaProducto(
            int numero,
            Empleado solicitante,
            ProductoCafeteria producto,
            String justificacion) {

        this(
            numero,
            solicitante,
            null,
            producto,
            justificacion
        );
    }

    private SugerenciaProducto(
            int numero,
            Empleado solicitante,
            Articulo articulo,
            ProductoCafeteria producto,
            String justificacion) {

        if (numero <= 0 ||
            solicitante == null ||
            solicitante.getLogin() == null ||
            solicitante.getLogin().isBlank() ||
            (articulo == null) == (producto == null) ||
            justificacion == null ||
            justificacion.isBlank()) {

            throw new IllegalArgumentException(
                "Datos de sugerencia invalidos"
            );
        }

        this.numero = numero;
        this.solicitante = solicitante;
        this.articulo = articulo;
        this.productoCafeteria = producto;
        this.justificacion = justificacion.trim();
        this.fechaSolicitud = LocalDateTime.now();
    }

    // GETTERS

    public int getNumero() {
        return numero;
    }

    public Empleado getSolicitante() {
        return solicitante;
    }

    public Area getArea() {
        return articulo != null
            ? Area.TIENDA
            : Area.CAFETERIA;
    }

    public String getCodigoProducto() {

        return articulo != null
            ? articulo.getCodigo()
            : productoCafeteria.getCodigo();
    }

    public Articulo getArticulo() {
        return articulo;
    }

    public ProductoCafeteria getProductoCafeteria() {
        return productoCafeteria;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public Estado getEstado() {
        return estado;
    }

    public Administrador getRevisor() {
        return revisor;
    }

    public String getRespuesta() {
        return respuesta;
    }

    // APROBAR O RECHAZAR UNA SUGERENCIA

    public void resolver(
            Administrador administrador,
            Estado decision,
            String observacion)
            throws ReglaNegocioException {

        if (administrador == null ||
            administrador.getLogin() == null ||
            administrador.getLogin().isBlank()) {

            throw new ReglaNegocioException(
                "Se requiere un administrador"
            );
        }

        if (estado != Estado.PENDIENTE) {

            throw new ReglaNegocioException(
                "La sugerencia ya fue resuelta"
            );
        }

        if (decision == null ||
            decision == Estado.PENDIENTE) {

            throw new ReglaNegocioException(
                "La decision debe ser aprobar o rechazar"
            );
        }

        if (decision == Estado.RECHAZADA &&
            (observacion == null ||
             observacion.isBlank())) {

            throw new ReglaNegocioException(
                "Indique el motivo del rechazo"
            );
        }

        estado = decision;
        revisor = administrador;

        respuesta = observacion == null
            ? ""
            : observacion.trim();
    }
}
