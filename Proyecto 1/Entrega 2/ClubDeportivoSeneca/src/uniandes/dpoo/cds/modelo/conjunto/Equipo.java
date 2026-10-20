package uniandes.dpoo.cds.modelo.conjunto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import uniandes.dpoo.cds.modelo.usuarios.Socio;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class Equipo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String nombre;
    private Deporte deporte;
    private Categoria categoria;
    private int cupoMaximo;

    private List<InscripcionEquipo> inscripciones;

    public Equipo(String nombre, Deporte deporte,
                  Categoria categoria, int cupoMaximo) {

        this.nombre = nombre;
        this.deporte = deporte;
        this.categoria = categoria;
        this.cupoMaximo = cupoMaximo;
        this.inscripciones = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public List<InscripcionEquipo> getInscripciones() {
        return Collections.unmodifiableList(inscripciones);
    }

    public boolean tieneCupo() {
        return inscripciones.size() < cupoMaximo;
    }

    public boolean numeroCamisetaDisponible(int numero) {
        for (InscripcionEquipo inscripcion : inscripciones) {
            if (inscripcion.getNumeroCamiseta() == numero) {
                return false;
            }
        }
        return true;
    }

    public boolean contieneSocio(Socio socio) {
        for (InscripcionEquipo inscripcion : inscripciones) {
            if (inscripcion.getSocio().getLogin().equals(socio.getLogin())) {
                return true;
            }
        }
        return false;
    }


public void agregarInscripcion(InscripcionEquipo inscripcion)
        throws ReglaNegocioException {

    // Verificar que la inscripcion sea valida
    if (inscripcion == null || inscripcion.getSocio() == null) {
        throw new ReglaNegocioException(
            "La inscripcion no es valida"
        );
    }

    // Verificar que pertenezca a este equipo
    if (inscripcion.getEquipo() != this) {
        throw new ReglaNegocioException(
            "La inscripcion corresponde a otro equipo"
        );
    }

    // Verificar que existan cupos
    if (!tieneCupo()) {
        throw new ReglaNegocioException(
            "El equipo no tiene cupos disponibles"
        );
    }

    // Verificar que el socio no este inscrito
    if (contieneSocio(inscripcion.getSocio())) {
        throw new ReglaNegocioException(
            "El socio ya pertenece a este equipo"
        );
    }

    // Verificar el numero de camiseta
    if (inscripcion.getNumeroCamiseta() <= 0 ||
        !numeroCamisetaDisponible(
            inscripcion.getNumeroCamiseta())) {

        throw new ReglaNegocioException(
            "El numero de camiseta no esta disponible"
        );
    }

    // Registrar la inscripcion
    inscripciones.add(inscripcion);
}



}
