
package uniandes.dpoo.cds.modelo.individuales;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PartidoPractica implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final Disciplina disciplina;
    private final Modalidad modalidad;
    private final List<Socio> participantes;

    private final List<EstadisticaPractica> estadisticas =
        new ArrayList<>();

    private List<Socio> ganadores = new ArrayList<>();
    private boolean finalizado;

    public PartidoPractica(
            LocalDate fecha,
            Disciplina disciplina,
            Modalidad modalidad,
            List<Socio> participantes) {

        this.fecha = fecha;
        this.disciplina = disciplina;
        this.modalidad = modalidad;
        this.participantes =
            new ArrayList<>(participantes);
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public boolean isFinalizado() {
        return finalizado;
    }

    public List<Socio> getParticipantes() {
        return Collections.unmodifiableList(participantes);
    }

    public List<EstadisticaPractica> getEstadisticas() {
        return Collections.unmodifiableList(estadisticas);
    }

    public List<Socio> getGanadores() {
        return Collections.unmodifiableList(ganadores);
    }

    // El gestor valida antes de agregar estadisticas.

    public void agregarEstadistica(
            EstadisticaPractica dato) {

        estadisticas.add(dato);
    }

    // Registrar finalizacion del partido.

    public void marcarFinalizado(
            List<Socio> ganadores) {

        this.ganadores = new ArrayList<>(ganadores);
        this.finalizado = true;
    }
}
