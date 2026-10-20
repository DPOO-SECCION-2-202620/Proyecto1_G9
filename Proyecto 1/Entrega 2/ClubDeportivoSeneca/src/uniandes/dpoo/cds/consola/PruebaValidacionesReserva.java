
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.time.LocalTime;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.individuales.Modalidad;
import uniandes.dpoo.cds.modelo.individuales.Reserva;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaValidacionesReserva {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Operacion {
        void ejecutar() throws ReglaNegocioException;
    }

    private static void comprobar(
            String nombre,
            ClubDeportivoSeneca club,
            boolean debeAceptar,
            int totalEsperado,
            Operacion operacion) {

        boolean aceptada = false;

        try {
            operacion.ejecutar();
            aceptada = true;

        } catch (ReglaNegocioException e) {
            System.out.println(
                "     Motivo: " + e.getMessage()
            );

        } catch (RuntimeException e) {
            System.out.println(
                "[FALLO] " + nombre
                + " (error inesperado: " + e + ")"
            );
            fallidas++;
            return;
        }

        if (aceptada == debeAceptar &&
            club.getReservas().size() == totalEsperado) {

            System.out.println("[OK] " + nombre);
            correctas++;

        } else {
            System.out.println("[FALLO] " + nombre);
            fallidas++;
        }
    }

    private static Reserva crear(
            Socio socio,
            Instalacion instalacion,
            LocalDate fecha,
            int inicioHora,
            int inicioMinuto,
            int finHora,
            int finMinuto,
            int jugadores,
            Modalidad modalidad) {

        return new Reserva(
            socio,
            instalacion,
            fecha,
            LocalTime.of(inicioHora, inicioMinuto),
            LocalTime.of(finHora, finMinuto),
            jugadores,
            modalidad
        );
    }

    public static void main(String[] args) {

        ClubDeportivoSeneca club =
            new ClubDeportivoSeneca();

        Disciplina tenis = new Disciplina("Tenis", 2);
        Disciplina padel = new Disciplina("Pádel", 1);
        Disciplina mesa =
            new Disciplina("Tenis de mesa", 1);

        Instalacion ten1 =
            new Instalacion("TEN-01", tenis, 4, false);

        Instalacion ten1Duplicada =
            new Instalacion("TEN-01", tenis, 4, false);

        Instalacion ten2 =
            new Instalacion("TEN-02", tenis, 4, false);

        Instalacion ten3 =
            new Instalacion("TEN-03", tenis, 4, false);

        Instalacion pad1 =
            new Instalacion("PAD-01", padel, 4, true);

        Instalacion mes1 =
            new Instalacion("MES-01", mesa, 4, true);

        Socio socio = new Socio(
            "jugador01",
            "clave123",
            LocalDate.of(2003, 5, 15)
        );

        LocalDate fecha = LocalDate.now().plusDays(1);

        System.out.println("PRUEBAS COMPLETAS DE RESERVAS");
        System.out.println("------------------------------");

        comprobar("Reserva nula rechazada", club, false, 0,
            () -> club.registrarReserva(null));

        comprobar("Horario invertido rechazado", club, false, 0,
            () -> club.registrarReserva(
                crear(socio, ten1, fecha,
                    12, 0, 10, 0, 2, Modalidad.SENCILLOS)));

        comprobar("Reserva de tenis correcta", club, true, 1,
            () -> club.registrarReserva(
                crear(socio, ten1, fecha,
                    10, 0, 11, 0, 2, Modalidad.SENCILLOS)));

        comprobar("Choque de horario en la misma cancha",
            club, false, 1,
            () -> club.registrarReserva(
                crear(socio, ten1Duplicada, fecha,
                    10, 30, 11, 30, 2, Modalidad.SENCILLOS)));

        comprobar("Reservas consecutivas permitidas",
            club, true, 2,
            () -> club.registrarReserva(
                crear(socio, ten1, fecha,
                    11, 0, 12, 0, 2, Modalidad.SENCILLOS)));

        comprobar("Otra cancha en el mismo horario",
            club, true, 3,
            () -> club.registrarReserva(
                crear(socio, ten2, fecha,
                    10, 0, 11, 0, 4, Modalidad.DOBLES)));

        comprobar("Limite de instalaciones de tenis",
            club, false, 3,
            () -> club.registrarReserva(
                crear(socio, ten3, fecha,
                    10, 15, 10, 45, 2, Modalidad.SENCILLOS)));

        comprobar("Padel sencillos rechazado",
            club, false, 3,
            () -> club.registrarReserva(
                crear(socio, pad1, fecha,
                    10, 0, 11, 0, 2, Modalidad.SENCILLOS)));

        comprobar("Padel dobles aceptado",
            club, true, 4,
            () -> club.registrarReserva(
                crear(socio, pad1, fecha,
                    10, 0, 11, 0, 4, Modalidad.DOBLES)));

        comprobar("Tenis sencillos con cuatro rechazado",
            club, false, 4,
            () -> club.registrarReserva(
                crear(socio, ten1, fecha,
                    13, 0, 14, 0, 4, Modalidad.SENCILLOS)));

        comprobar("Misma cancha en otro dia",
            club, true, 5,
            () -> club.registrarReserva(
                crear(socio, ten1, fecha.plusDays(1),
                    10, 0, 11, 0, 2, Modalidad.SENCILLOS)));

        comprobar("Tenis de mesa en paralelo",
            club, true, 6,
            () -> club.registrarReserva(
                crear(socio, mes1, fecha,
                    10, 0, 11, 0, 2, Modalidad.SENCILLOS)));

        comprobar("Demasiados jugadores en tenis de mesa",
            club, false, 6,
            () -> club.registrarReserva(
                crear(socio, mes1, fecha,
                    12, 0, 13, 0, 6, Modalidad.DOBLES)));

        System.out.println("------------------------------");
        System.out.println(
            "Pruebas correctas: " + correctas
        );
        System.out.println(
            "Pruebas fallidas: " + fallidas
        );
        System.out.println(
            "Reservas guardadas: "
            + club.getReservas().size()
        );
    }
}
