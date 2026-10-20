package uniandes.dpoo.cds.modelo.individuales;

import java.io.Serializable;

public class Disciplina implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private int maximoInstalaciones;

    public Disciplina(String nombre, int maximoInstalaciones) {
        this.nombre = nombre;
        this.maximoInstalaciones = maximoInstalaciones;
    }

    public String getNombre() {
        return nombre;
    }

    public int getMaximoInstalaciones() {
        return maximoInstalaciones;
    }
}
