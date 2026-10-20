package uniandes.dpoo.cds.modelo.usuarios;

public class Empleado extends Usuario {

    private static final long serialVersionUID = 1L;

    private String codigoDescuento;

    public Empleado(String login, String contrasena,
                    String codigoDescuento) {
        super(login, contrasena);
        this.codigoDescuento = codigoDescuento;
    }

    public String getCodigoDescuento() {
        return codigoDescuento;
    }

    public void setCodigoDescuento(String codigoDescuento) {
        this.codigoDescuento = codigoDescuento;
    }
}
