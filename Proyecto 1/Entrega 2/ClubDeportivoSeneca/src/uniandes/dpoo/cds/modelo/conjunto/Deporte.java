package uniandes.dpoo.cds.modelo.conjunto;

import java.io.Serializable;

public class Deporte implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private int minimoJugadores;

    public Deporte(String nombre, int minimoJugadores) {
        this.nombre = nombre;
        this.minimoJugadores = minimoJugadores;
    }

    public String getNombre() {
        return nombre;
    }

    public int getMinimoJugadores() {
        return minimoJugadores;
    }
}
