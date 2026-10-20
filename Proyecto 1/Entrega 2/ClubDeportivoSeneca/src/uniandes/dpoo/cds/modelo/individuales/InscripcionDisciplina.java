package uniandes.dpoo.cds.modelo.individuales;

import java.io.Serializable;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class InscripcionDisciplina implements Serializable {

    private static final long serialVersionUID = 1L;

    private Socio socio;
    private Disciplina disciplina;
    private Nivel nivel;

    public InscripcionDisciplina(Socio socio,
                                 Disciplina disciplina,
                                 Nivel nivel) {

        this.socio = socio;
        this.disciplina = disciplina;
        this.nivel = nivel;
    }

    public Socio getSocio() {
        return socio;
    }

    public Disciplina getDisciplina() {
        return disciplina;
    }

    public Nivel getNivel() {
        return nivel;
    }
}
