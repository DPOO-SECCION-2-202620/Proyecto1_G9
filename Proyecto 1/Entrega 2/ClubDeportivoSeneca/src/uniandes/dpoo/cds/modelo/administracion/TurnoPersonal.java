
package uniandes.dpoo.cds.modelo.administracion;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Fisioterapeuta;

public class TurnoPersonal implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Cargo {
        ENTRENADOR,
        FISIOTERAPEUTA
    }

    // ATRIBUTOS

    private final Empleado empleado;
    private final DayOfWeek dia;
    private final LocalTime inicio;
    private final LocalTime fin;

    // CONSTRUCTOR

    public TurnoPersonal(
            Empleado empleado,
            DayOfWeek dia,
            LocalTime inicio,
            LocalTime fin) {

        if (empleado == null ||
            empleado.getLogin() == null ||
            empleado.getLogin().isBlank() ||
            !(empleado instanceof Entrenador ||
              empleado instanceof Fisioterapeuta) ||
            dia == null ||
            inicio == null ||
            fin == null ||
            !inicio.isBefore(fin)) {

            throw new IllegalArgumentException(
                "Datos del turno invalidos"
            );
        }

        this.empleado = empleado;
        this.dia = dia;
        this.inicio = inicio;
        this.fin = fin;
    }

    // GETTERS

    public Empleado getEmpleado() {
        return empleado;
    }

    public DayOfWeek getDia() {
        return dia;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public LocalTime getFin() {
        return fin;
    }

    // IDENTIFICAR CARGO

    public Cargo getCargo() {

        return empleado instanceof Entrenador
            ? Cargo.ENTRENADOR
            : Cargo.FISIOTERAPEUTA;
    }

    // COMPROBAR SI EL TURNO CUBRE UNA HORA

    public boolean cubre(LocalTime momento) {

        return !momento.isBefore(inicio)
            && momento.isBefore(fin);
    }

    // COMPROBAR CRUCE DE HORARIOS

    public boolean seCruza(TurnoPersonal otro) {

        return otro != null &&
            dia == otro.dia &&
            inicio.isBefore(otro.fin) &&
            otro.inicio.isBefore(fin);
    }
}
