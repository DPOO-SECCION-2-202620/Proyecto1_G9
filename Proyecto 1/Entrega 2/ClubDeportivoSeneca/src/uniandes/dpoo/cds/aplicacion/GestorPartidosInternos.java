
package uniandes.dpoo.cds.aplicacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;

public class GestorPartidosInternos {

    private final ClubDeportivoSeneca club;
    private final List<PartidoInterno> partidos;

    public GestorPartidosInternos(ClubDeportivoSeneca club) {
        if (club == null) {
            throw new IllegalArgumentException(
                "El club no puede ser nulo"
            );
        }
        this.club = club;
        this.partidos = new ArrayList<>();
    }

    public List<PartidoInterno> getPartidos() {
        return Collections.unmodifiableList(partidos);
    }

    public PartidoInterno programarPartido(
            LocalDate fecha,
            Equipo local,
            Equipo visitante)
            throws ReglaNegocioException {

        // VALIDAR LA FECHA

        if (fecha == null) {
            throw new ReglaNegocioException(
                "La fecha es obligatoria"
            );
        }

        // VALIDAR LOS EQUIPOS

        if (local == null || visitante == null) {
            throw new ReglaNegocioException(
                "Los dos equipos son obligatorios"
            );
        }

        if (local == visitante) {
            throw new ReglaNegocioException(
                "Un equipo no puede jugar contra si mismo"
            );
        }

        // VERIFICAR QUE ESTEN REGISTRADOS EN EL CLUB

        if (!club.getEquipos().contains(local) ||
            !club.getEquipos().contains(visitante)) {

            throw new ReglaNegocioException(
                "Ambos equipos deben estar registrados en el club"
            );
        }

        // VALIDAR DEPORTE Y CATEGORIA

        if (local.getDeporte() == null ||
            visitante.getDeporte() == null ||
            local.getCategoria() == null ||
            visitante.getCategoria() == null) {

            throw new ReglaNegocioException(
                "Los equipos deben tener deporte y categoria"
            );
        }

        if (!local.getDeporte().getNombre().trim()
                .equalsIgnoreCase(
                    visitante.getDeporte().getNombre().trim()
                )) {

            throw new ReglaNegocioException(
                "Los equipos deben practicar el mismo deporte"
            );
        }

        if (!local.getCategoria().getNombre().trim()
                .equalsIgnoreCase(
                    visitante.getCategoria().getNombre().trim()
                )) {

            throw new ReglaNegocioException(
                "Los equipos deben ser de la misma categoria"
            );
        }

        // VALIDAR MINIMO DE JUGADORES

        int minimoLocal =
            local.getDeporte().getMinimoJugadores();

        int minimoVisitante =
            visitante.getDeporte().getMinimoJugadores();

        if (minimoLocal <= 0 || minimoVisitante <= 0) {
            throw new ReglaNegocioException(
                "El minimo de jugadores debe ser positivo"
            );
        }

        if (local.getInscripciones().size() < minimoLocal ||
            visitante.getInscripciones().size() < minimoVisitante) {

            throw new ReglaNegocioException(
                "Un equipo no tiene suficientes jugadores inscritos"
            );
        }

        // CREAR Y REGISTRAR EL PARTIDO

        PartidoInterno partido =
            new PartidoInterno(fecha, local, visitante);

        partidos.add(partido);

        return partido;
    }
}
