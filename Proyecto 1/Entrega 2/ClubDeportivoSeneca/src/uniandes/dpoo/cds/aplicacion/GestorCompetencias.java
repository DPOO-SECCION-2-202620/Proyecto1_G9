
package uniandes.dpoo.cds.aplicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;

public class GestorCompetencias {

    // ATRIBUTOS

    private final ClubDeportivoSeneca club;

    private final List<Competencia> competencias =
        new ArrayList<>();

    private final List<InscripcionCompetencia> inscripciones =
        new ArrayList<>();

    // CONSTRUCTOR

    public GestorCompetencias(ClubDeportivoSeneca club) {

        if (club == null) {
            throw new IllegalArgumentException(
                "El club no puede ser nulo"
            );
        }

        this.club = club;
    }

    // GETTERS

    public List<Competencia> getCompetencias() {
        return Collections.unmodifiableList(competencias);
    }

    public List<InscripcionCompetencia> getInscripciones() {
        return Collections.unmodifiableList(inscripciones);
    }

    // REGISTRAR COMPETENCIA

    public void registrarCompetencia(
            Competencia competencia)
            throws ReglaNegocioException {

        if (competencia == null) {
            throw new ReglaNegocioException(
                "La competencia no puede ser nula"
            );
        }

        // EVITAR COMPETENCIAS DUPLICADAS

        for (Competencia existente : competencias) {

            if (existente.getNombre().equalsIgnoreCase(
                    competencia.getNombre()) &&

                existente.getTemporada().equalsIgnoreCase(
                    competencia.getTemporada()) &&

                existente.getEntidadOrganizadora()
                    .equalsIgnoreCase(
                        competencia.getEntidadOrganizadora())) {

                throw new ReglaNegocioException(
                    "La competencia ya esta registrada"
                );
            }
        }

        competencias.add(competencia);
    }

    // INSCRIBIR EQUIPO EN COMPETENCIA

    public InscripcionCompetencia inscribirEquipo(
            Equipo equipo,
            Competencia competencia)
            throws ReglaNegocioException {

        // VALIDAR REGISTROS

        if (equipo == null ||
            competencia == null ||
            !club.getEquipos().contains(equipo) ||
            !competencias.contains(competencia)) {

            throw new ReglaNegocioException(
                "Equipo o competencia no registrados"
            );
        }

        // VALIDAR DEPORTE Y CATEGORIA

        if (equipo.getDeporte() == null ||
            equipo.getCategoria() == null) {

            throw new ReglaNegocioException(
                "El equipo no tiene deporte o categoria"
            );
        }

        if (!equipo.getDeporte().getNombre()
                .equalsIgnoreCase(
                    competencia.getDeporte().getNombre())) {

            throw new ReglaNegocioException(
                "El deporte no corresponde a la competencia"
            );
        }

        if (!equipo.getCategoria().getNombre()
                .equalsIgnoreCase(
                    competencia.getCategoria().getNombre())) {

            throw new ReglaNegocioException(
                "La categoria no corresponde a la competencia"
            );
        }

        // EVITAR INSCRIPCIONES DUPLICADAS

        for (InscripcionCompetencia inscripcion : inscripciones) {

            if (inscripcion.getEquipo() == equipo &&
                inscripcion.getCompetencia() == competencia) {

                throw new ReglaNegocioException(
                    "El equipo ya esta inscrito"
                );
            }
        }

        // CREAR INSCRIPCION

        InscripcionCompetencia nueva =
            new InscripcionCompetencia(equipo, competencia);

        inscripciones.add(nueva);

        return nueva;
    }
}
