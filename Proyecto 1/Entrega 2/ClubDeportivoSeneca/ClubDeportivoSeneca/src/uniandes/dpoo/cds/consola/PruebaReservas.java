
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.time.LocalTime;

import uniandes.dpoo.cds.modelo.individuales.*;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaReservas {

    public static void main(String[] args) {

        Disciplina tenis = new Disciplina("Tenis", 2);

        Instalacion cancha = new Instalacion(
            "TEN-01", tenis, 4, false
        );

        Socio socio = new Socio(
            "jugador01",
            "clave123",
            LocalDate.of(2003, 5, 15)
        );

        InscripcionDisciplina inscripcion =
            new InscripcionDisciplina(
                socio, tenis, Nivel.INTERMEDIO
            );

        Reserva reserva = new Reserva(
            socio,
            cancha,
            LocalDate.of(2026, 10, 25),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            2,
            Modalidad.SENCILLOS
        );

        System.out.println("PRUEBA DE RESERVAS");
        System.out.println("----------------------");
        System.out.println("Socio: " + socio.getLogin());
        System.out.println("Disciplina: "
                + tenis.getNombre());
        System.out.println("Nivel: "
                + inscripcion.getNivel());
        System.out.println("Instalacion: "
                + cancha.getCodigo());
        System.out.println("Capacidad: "
                + cancha.getCapacidadMaxima());
        System.out.println("Fecha: "
                + reserva.getFecha());
        System.out.println("Inicio: "
                + reserva.getHoraInicio());
        System.out.println("Fin: "
                + reserva.getHoraFin());
        System.out.println("Modalidad: "
                + reserva.getModalidad());
    }
}
