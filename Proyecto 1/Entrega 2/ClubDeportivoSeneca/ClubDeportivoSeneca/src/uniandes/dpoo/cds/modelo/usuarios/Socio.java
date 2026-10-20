package uniandes.dpoo.cds.modelo.usuarios;

import java.time.LocalDate;

public class Socio extends Usuario {
	
	private static final long serialVersionUID = 1L;

    private LocalDate fechaNacimiento;

    public Socio(String login, String contrasena,
                 LocalDate fechaNacimiento) {

        super(login, contrasena);
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
}
