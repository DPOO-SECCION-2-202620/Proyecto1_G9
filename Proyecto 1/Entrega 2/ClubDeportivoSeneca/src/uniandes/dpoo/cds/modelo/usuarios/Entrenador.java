package uniandes.dpoo.cds.modelo.usuarios;

public class Entrenador extends Empleado {

    private static final long serialVersionUID = 1L;

    public Entrenador(String login, String contrasena,
                      String codigoDescuento) {
        super(login, contrasena, codigoDescuento);
    }
}
