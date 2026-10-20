package uniandes.dpoo.cds.excepciones;

public class ReglaNegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
