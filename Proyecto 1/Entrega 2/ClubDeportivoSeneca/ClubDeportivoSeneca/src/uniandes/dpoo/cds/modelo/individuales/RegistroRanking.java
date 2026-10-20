
package uniandes.dpoo.cds.modelo.individuales;

import java.io.Serializable;
import java.time.LocalDate;

public class RegistroRanking implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final String loginSocio;
    private final String nombreDisciplina;
    private final double puntos;

    public RegistroRanking(
            LocalDate fecha,
            String loginSocio,
            String nombreDisciplina,
            double puntos) {

        if (fecha == null ||
            loginSocio == null ||
            loginSocio.isBlank() ||
            nombreDisciplina == null ||
            nombreDisciplina.isBlank() ||
            !Double.isFinite(puntos) ||
            puntos < 0) {

            throw new IllegalArgumentException(
                "Registro de ranking invalido"
            );
        }

        this.fecha = fecha;
        this.loginSocio = loginSocio;
        this.nombreDisciplina = nombreDisciplina;
        this.puntos = puntos;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getLoginSocio() {
        return loginSocio;
    }

    public String getNombreDisciplina() {
        return nombreDisciplina;
    }

    public double getPuntos() {
        return puntos;
    }
}
