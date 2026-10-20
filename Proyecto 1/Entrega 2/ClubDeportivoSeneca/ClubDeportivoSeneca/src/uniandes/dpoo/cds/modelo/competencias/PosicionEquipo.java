
package uniandes.dpoo.cds.modelo.competencias;

import uniandes.dpoo.cds.modelo.conjunto.Equipo;

public class PosicionEquipo {

    private final Equipo equipo;
    private final int jugados;
    private final int ganados;
    private final int empatados;
    private final int perdidos;
    private final int golesFavor;
    private final int golesContra;
    private final int puntos;

    public PosicionEquipo(
            Equipo equipo,
            int ganados,
            int empatados,
            int perdidos,
            int golesFavor,
            int golesContra,
            int puntos) {

        this.equipo = equipo;
        this.ganados = ganados;
        this.empatados = empatados;
        this.perdidos = perdidos;
        this.jugados = ganados + empatados + perdidos;
        this.golesFavor = golesFavor;
        this.golesContra = golesContra;
        this.puntos = puntos;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public int getJugados() {
        return jugados;
    }

    public int getGanados() {
        return ganados;
    }

    public int getEmpatados() {
        return empatados;
    }

    public int getPerdidos() {
        return perdidos;
    }

    public int getGolesFavor() {
        return golesFavor;
    }

    public int getGolesContra() {
        return golesContra;
    }

    public int getDiferenciaGoles() {
        return golesFavor - golesContra;
    }

    public int getPuntos() {
        return puntos;
    }
}
