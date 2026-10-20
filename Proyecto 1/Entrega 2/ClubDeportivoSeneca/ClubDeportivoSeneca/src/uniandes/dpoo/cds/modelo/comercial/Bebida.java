
package uniandes.dpoo.cds.modelo.comercial;

public class Bebida extends ProductoCafeteria {

    private static final long serialVersionUID = 1L;

    public enum Temperatura {
        FRIA,
        CALIENTE
    }

    private final Temperatura temperatura;

    public Bebida(
            String codigo,
            String nombre,
            double precio,
            Temperatura temperatura) {

        super(codigo, nombre, precio);

        if (temperatura == null) {
            throw new IllegalArgumentException(
                "La temperatura es obligatoria"
            );
        }

        this.temperatura = temperatura;
    }

    public Temperatura getTemperatura() {
        return temperatura;
    }

    public boolean esCaliente() {
        return temperatura == Temperatura.CALIENTE;
    }
}
