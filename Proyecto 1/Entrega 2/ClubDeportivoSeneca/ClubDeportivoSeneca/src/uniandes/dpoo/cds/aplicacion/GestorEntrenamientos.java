
package uniandes.dpoo.cds.aplicacion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.entrenamientos.SesionEntrenamiento;

public class GestorEntrenamientos {

    private final List<SesionEntrenamiento> sesiones =
        new ArrayList<>();

    public List<SesionEntrenamiento> getSesiones() {
        return Collections.unmodifiableList(sesiones);
    }

    // PROGRAMAR SESION

    public void programarSesion(
            SesionEntrenamiento nueva)
            throws ReglaNegocioException {

        if (nueva == null) {
            throw new ReglaNegocioException(
                "La sesion no puede ser nula"
            );
        }

        LocalDateTime inicioNuevo =
            nueva.getFecha().atTime(nueva.getHora());

        LocalDateTime finNuevo =
            inicioNuevo.plusMinutes(
                nueva.getDuracionMinutos()
            );

        // COMPROBAR CONFLICTOS

        for (SesionEntrenamiento otra : sesiones) {

            LocalDateTime inicioOtro =
                otra.getFecha().atTime(otra.getHora());

            LocalDateTime finOtro =
                inicioOtro.plusMinutes(
                    otra.getDuracionMinutos()
                );

            boolean cruce =
                inicioNuevo.isBefore(finOtro) &&
                inicioOtro.isBefore(finNuevo);

            if (!cruce) {
                continue;
            }

            boolean mismaInstalacion =
                nueva.getInstalacion().getCodigo()
                .equalsIgnoreCase(
                    otra.getInstalacion().getCodigo()
                );

            boolean mismoEntrenador =
                nueva.getEntrenador().getLogin()
                .equalsIgnoreCase(
                    otra.getEntrenador().getLogin()
                );

            if (mismaInstalacion) {
                throw new ReglaNegocioException(
                    "La instalacion ya esta ocupada"
                );
            }

            if (mismoEntrenador) {
                throw new ReglaNegocioException(
                    "El entrenador ya tiene una sesion"
                );
            }
        }

        sesiones.add(nueva);
    }
}
