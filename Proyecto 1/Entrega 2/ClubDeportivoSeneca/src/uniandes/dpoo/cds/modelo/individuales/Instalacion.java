package uniandes.dpoo.cds.modelo.individuales;

import java.io.Serializable;

public class Instalacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private String codigo;
    private Disciplina disciplina;
    private int capacidadMaxima;
    private boolean techada;

    public Instalacion(String codigo,
                       Disciplina disciplina,
                       int capacidadMaxima,
                       boolean techada) {

        this.codigo = codigo;
        this.disciplina = disciplina;
        this.capacidadMaxima = capacidadMaxima;
        this.techada = techada;
    }

    public String getCodigo() {
        return codigo;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public boolean isTechada() {
        return techada;
    }
}
