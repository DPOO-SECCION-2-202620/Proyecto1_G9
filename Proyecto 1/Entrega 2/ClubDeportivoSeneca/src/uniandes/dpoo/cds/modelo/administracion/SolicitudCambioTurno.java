
package uniandes.dpoo.cds.modelo.administracion;

import java.io.Serializable;
import java.time.LocalDateTime;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.usuarios.Administrador;

public class SolicitudCambioTurno implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Estado {
        PENDIENTE,
        APROBADA,
        RECHAZADA
    }

    // ATRIBUTOS

    private final int numero;
    private final TurnoPersonal original;
    private final TurnoPersonal propuesto;
    private final String motivo;
    private final LocalDateTime fechaSolicitud;

    private Estado estado = Estado.PENDIENTE;
    private String respuesta = "";
    private Administrador revisor;

    // CONSTRUCTOR

    public SolicitudCambioTurno(
            int numero,
            TurnoPersonal original,
            TurnoPersonal propuesto,
            String motivo) {

        if (numero <= 0 ||
            original == null ||
            propuesto == null ||
            original.getEmpleado() !=
                propuesto.getEmpleado() ||
            motivo == null ||
            motivo.isBlank()) {

            throw new IllegalArgumentException(
                "Solicitud invalida"
            );
        }

        this.numero = numero;
        this.original = original;
        this.propuesto = propuesto;
        this.motivo = motivo.trim();
        this.fechaSolicitud = LocalDateTime.now();
    }

    // GETTERS

    public int getNumero() {
        return numero;
    }

    public TurnoPersonal getOriginal() {
        return original;
    }

    public TurnoPersonal getPropuesto() {
        return propuesto;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public Estado getEstado() {
        return estado;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public Administrador getRevisor() {
        return revisor;
    }

    // RESOLVER SOLICITUD

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
                "La solicitud ya fue resuelta"
            );
        }

        if (decision == null ||
            decision == Estado.PENDIENTE) {

            throw new ReglaNegocioException(
                "Decision invalida"
            );
        }

        if (decision == Estado.RECHAZADA &&
            (observacion == null ||
             observacion.isBlank())) {

            throw new ReglaNegocioException(
                "El rechazo requiere un motivo"
            );
        }

        revisor = administrador;
        estado = decision;

        respuesta = observacion == null
            ? ""
            : observacion.trim();
    }
}
