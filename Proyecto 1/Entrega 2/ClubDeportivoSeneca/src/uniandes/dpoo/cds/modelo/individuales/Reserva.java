package uniandes.dpoo.cds.modelo.individuales;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    private Socio socio;
    private Instalacion instalacion;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int numeroJugadores;
    private Modalidad modalidad;

    public Reserva(Socio socio,
                   Instalacion instalacion,
                   LocalDate fecha,
                   LocalTime horaInicio,
                   LocalTime horaFin,
                   int numeroJugadores,
                   Modalidad modalidad) {

        this.socio = socio;
        this.instalacion = instalacion;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.numeroJugadores = numeroJugadores;
        this.modalidad = modalidad;
    }

    public Socio getSocio() {
        return socio;
    }

    public Instalacion getInstalacion() {
        return instalacion;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public int getNumeroJugadores() {
        return numeroJugadores;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }
}
