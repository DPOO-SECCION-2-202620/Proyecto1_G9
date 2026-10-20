
package uniandes.dpoo.cds.modelo.entrenamientos;

import java.io.Serializable;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class Asistencia implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Socio socio;
    private final EstadoAsistencia estado;
    private Evaluacion evaluacion;

    public Asistencia(
            Socio socio,
            EstadoAsistencia estado) {

        if (socio == null || estado == null) {
            throw new IllegalArgumentException(
                "Socio y estado son obligatorios"
            );
        }

        this.socio = socio;
        this.estado = estado;
    }

    public Socio getSocio() {
        return socio;
    }

    public EstadoAsistencia getEstado() {
        return estado;
    }

    public Evaluacion getEvaluacion() {
        return evaluacion;
    }

    void asignarEvaluacion(Evaluacion evaluacion) {
        this.evaluacion = evaluacion;
    }
}
