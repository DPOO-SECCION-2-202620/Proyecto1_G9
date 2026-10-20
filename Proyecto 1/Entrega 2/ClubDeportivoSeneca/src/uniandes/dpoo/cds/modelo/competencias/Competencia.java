
package uniandes.dpoo.cds.modelo.competencias;

import java.io.Serializable;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;

public class Competencia implements Serializable {

    private static final long serialVersionUID = 1L;

    // ATRIBUTOS

    private final String nombre;
    private final String entidadOrganizadora;
    private final String temporada;
    private final Deporte deporte;
    private final Categoria categoria;
    private final String formato;
    private final int minimoNomina;

    // CONSTRUCTOR

    public Competencia(
            String nombre,
            String entidadOrganizadora,
            String temporada,
            Deporte deporte,
            Categoria categoria,
            String formato,
            int minimoNomina) {

        if (nombre == null || nombre.isBlank() ||
            entidadOrganizadora == null ||
            entidadOrganizadora.isBlank() ||
            temporada == null || temporada.isBlank() ||
            deporte == null || categoria == null ||
            formato == null || formato.isBlank() ||
            minimoNomina <= 0 ||
            minimoNomina < deporte.getMinimoJugadores()) {

            throw new IllegalArgumentException(
                "Datos de competencia invalidos"
            );
        }

        this.nombre = nombre;
        this.entidadOrganizadora = entidadOrganizadora;
        this.temporada = temporada;
        this.deporte = deporte;
        this.categoria = categoria;
        this.formato = formato;
        this.minimoNomina = minimoNomina;
    }

    // GETTERS

    public String getNombre() {
        return nombre;
    }

    public String getEntidadOrganizadora() {
        return entidadOrganizadora;
    }

    public String getTemporada() {
        return temporada;
    }

    public Deporte getDeporte() {
        return deporte;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public String getFormato() {
        return formato;
    }

    public int getMinimoNomina() {
        return minimoNomina;
    }
}
