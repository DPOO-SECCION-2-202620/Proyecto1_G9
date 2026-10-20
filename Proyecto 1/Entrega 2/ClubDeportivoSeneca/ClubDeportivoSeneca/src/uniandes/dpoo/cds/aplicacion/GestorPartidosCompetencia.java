
package uniandes.dpoo.cds.aplicacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.competencias.Escenario;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.competencias.InstitucionRival;
import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;

public class GestorPartidosCompetencia {

    private final GestorCompetencias gestorCompetencias;

    private final List<PartidoCompetencia> partidos =
        new ArrayList<>();

    public GestorPartidosCompetencia(
            GestorCompetencias gestorCompetencias) {

        if (gestorCompetencias == null) {
            throw new IllegalArgumentException(
                "El gestor de competencias es obligatorio"
            );
        }

        this.gestorCompetencias = gestorCompetencias;
    }

    public List<PartidoCompetencia> getPartidos() {
        return Collections.unmodifiableList(partidos);
    }

    // PROGRAMAR PARTIDO OFICIAL

    public PartidoCompetencia programarPartido(
            InscripcionCompetencia inscripcion,
            InstitucionRival rival,
            Escenario escenario,
            LocalDate fecha,
            String fase)
            throws ReglaNegocioException {

        // VALIDAR INSCRIPCION

        if (inscripcion == null ||
            !gestorCompetencias.getInscripciones()
                .contains(inscripcion)) {

            throw new ReglaNegocioException(
                "La inscripcion no pertenece al club"
            );
        }

        // VALIDAR NOMINA OFICIAL

        if (!inscripcion.isOficial()) {
            throw new ReglaNegocioException(
                "La nomina debe estar oficializada"
            );
        }

        // VALIDAR DATOS

        if (rival == null ||
            escenario == null ||
            fecha == null ||
            fase == null ||
            fase.isBlank()) {

            throw new ReglaNegocioException(
                "Faltan datos del partido oficial"
            );
        }

        // EVITAR PARTIDOS DUPLICADOS

        for (PartidoCompetencia existente : partidos) {

            if (existente.getInscripcion() == inscripcion &&
                existente.getFecha().equals(fecha) &&
                existente.getRival().getNombre()
                    .equalsIgnoreCase(rival.getNombre())) {

                throw new ReglaNegocioException(
                    "Este partido oficial ya esta programado"
                );
            }
        }

        // CREAR PARTIDO

        PartidoCompetencia nuevo =
            new PartidoCompetencia(
                inscripcion,
                rival,
                escenario,
                fecha,
                fase
            );

        partidos.add(nuevo);

        return nuevo;
    }
}
