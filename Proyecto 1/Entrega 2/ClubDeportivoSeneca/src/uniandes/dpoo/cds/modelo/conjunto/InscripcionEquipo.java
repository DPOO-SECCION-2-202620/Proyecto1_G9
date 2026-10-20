package uniandes.dpoo.cds.modelo.conjunto;

import java.io.Serializable;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class InscripcionEquipo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Socio socio;
    private Equipo equipo;
    private String posicion;
    private int numeroCamiseta;

    public InscripcionEquipo(Socio socio, Equipo equipo,
                             String posicion,
                             int numeroCamiseta) {

        this.socio = socio;
        this.equipo = equipo;
        this.posicion = posicion;
        this.numeroCamiseta = numeroCamiseta;
    }

    public Socio getSocio() {
        return socio;
    }

    public Equipo getEquipo() {
        return equipo;
    }

    public String getPosicion() {
        return posicion;
    }

    public int getNumeroCamiseta() {
        return numeroCamiseta;
    }
}
