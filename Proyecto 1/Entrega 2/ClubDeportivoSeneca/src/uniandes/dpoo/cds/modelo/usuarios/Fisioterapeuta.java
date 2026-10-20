package uniandes.dpoo.cds.modelo.usuarios;

public class Fisioterapeuta extends Empleado {

    private static final long serialVersionUID = 1L;

    public Fisioterapeuta(String login, String contrasena,
                         String codigoDescuento) {
        super(login, contrasena, codigoDescuento);
    }
}
