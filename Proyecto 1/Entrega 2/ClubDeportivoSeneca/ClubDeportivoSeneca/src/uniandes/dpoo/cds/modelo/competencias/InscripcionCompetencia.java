
package uniandes.dpoo.cds.modelo.competencias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class InscripcionCompetencia implements Serializable {

    private static final long serialVersionUID = 1L;

    // ATRIBUTOS

    private final Equipo equipo;
    private final Competencia competencia;
    private final List<Socio> nomina = new ArrayList<>();
    private boolean oficial;

    // CONSTRUCTOR

    public InscripcionCompetencia(
            Equipo equipo,
            Competencia competencia) {

        this.equipo = equipo;
        this.competencia = competencia;
    }

    // GETTERS

    public Equipo getEquipo() {
        return equipo;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    public List<Socio> getNomina() {
        return Collections.unmodifiableList(nomina);
    }

    public boolean isOficial() {
        return oficial;
    }

    // AGREGAR JUGADORES A LA NOMINA

    public void agregarJugador(Socio socio)
            throws ReglaNegocioException {

        if (oficial) {
            throw new ReglaNegocioException(
                "La nomina oficial ya esta cerrada"
            );
        }

        if (socio == null ||
            socio.getLogin() == null ||
            socio.getLogin().isBlank() ||
            !equipo.contieneSocio(socio)) {

            throw new ReglaNegocioException(
                "El jugador no pertenece al equipo"
            );
        }

        // EVITAR JUGADORES DUPLICADOS

        for (Socio inscrito : nomina) {

            if (inscrito.getLogin().equals(socio.getLogin())) {

                throw new ReglaNegocioException(
                    "El jugador ya esta en la nomina"
                );
            }
        }

        nomina.add(socio);
    }

    // OFICIALIZAR LA NOMINA

    public void oficializarNomina()
            throws ReglaNegocioException {

        if (oficial) {
            throw new ReglaNegocioException(
                "La nomina ya fue oficializada"
            );
        }

        if (nomina.size() < competencia.getMinimoNomina()) {

            throw new ReglaNegocioException(
                "Nomina insuficiente para la competencia"
            );
        }

        oficial = true;
    }

    // COMPROBAR SI UN JUGADOR ESTA HABILITADO

    public boolean jugadorHabilitado(Socio socio) {

        if (!oficial ||
            socio == null ||
            socio.getLogin() == null) {

            return false;
        }

        for (Socio inscrito : nomina) {

            if (inscrito.getLogin().equals(socio.getLogin())) {
                return true;
            }
        }

        return false;
    }
}
