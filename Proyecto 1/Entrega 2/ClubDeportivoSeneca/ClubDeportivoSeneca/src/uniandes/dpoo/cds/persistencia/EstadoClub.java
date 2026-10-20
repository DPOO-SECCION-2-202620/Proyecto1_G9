
package uniandes.dpoo.cds.persistencia;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.individuales.Reserva;

public class EstadoClub implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Equipo> equipos;
    private List<Reserva> reservas;

    public EstadoClub(List<Equipo> equipos, List<Reserva> reservas) {
        this.equipos = new ArrayList<>(equipos);
        this.reservas = new ArrayList<>(reservas);
    }

    public List<Equipo> getEquipos() {
        return Collections.unmodifiableList(equipos);
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}
