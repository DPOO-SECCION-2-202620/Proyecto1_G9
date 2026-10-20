
package uniandes.dpoo.cds.modelo.entrenamientos;

import java.io.Serializable;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class Evaluacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int nota;
    private final String comentario;

    public Evaluacion(int nota, String comentario)
            throws ReglaNegocioException {

        if (nota < 1 || nota > 5) {
            throw new ReglaNegocioException(
                "La nota debe estar entre 1 y 5"
            );
        }

        this.nota = nota;

        this.comentario = comentario == null
            ? "" : comentario.trim();
    }

    public int getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }
}
