package uniandes.dpoo.cds.consola;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.modelo.conjunto.*;
import uniandes.dpoo.cds.modelo.usuarios.Socio;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class PruebaInscripciones {

    private static int correctas = 0;
    private static int fallidas = 0;

    public static void main(String[] args) {

        ClubDeportivoSeneca club = new ClubDeportivoSeneca();

        Deporte futbol = new Deporte("Futbol", 7);
        Deporte baloncesto = new Deporte("Baloncesto", 5);

        // Categoria ficticia para probar edades
        Categoria categoria =
            new Categoria("Categoria prueba", 18, 30);

        Equipo equipoA = new Equipo(
            "Seneca A", futbol, categoria, 2);

        Equipo equipoB = new Equipo(
            "Seneca B", futbol, categoria, 2);

        Equipo equipoBasket = new Equipo(
            "Seneca Basket", baloncesto, categoria, 2);

        club.registrarEquipo(equipoA);
        club.registrarEquipo(equipoB);
        club.registrarEquipo(equipoBasket);

        LocalDate hoy = LocalDate.now();

        Socio juan = new Socio(
            "juan", "clave1", hoy.minusYears(23));

        Socio ana = new Socio(
            "ana", "clave2", hoy.minusYears(21));

        Socio pedro = new Socio(
            "pedro", "clave3", hoy.minusYears(20));

        Socio menor = new Socio(
            "menor", "clave4", hoy.minusYears(12));

        Socio mayor = new Socio(
            "mayor", "clave5", hoy.minusYears(50));

        System.out.println("PRUEBAS DE INSCRIPCION");
        System.out.println("-----------------------------");

        probar(club, "Inscripcion correcta",
               juan, equipoA, 10, true);

        probar(club, "Jugador duplicado",
               juan, equipoA, 11, false);

        probar(club, "Camiseta repetida",
               ana, equipoA, 10, false);

        probar(club, "Jugador menor de edad",
               menor, equipoA, 12, false);

        probar(club, "Jugador supera edad maxima",
               mayor, equipoA, 12, false);

        probar(club, "Dos equipos del mismo deporte",
               juan, equipoB, 11, false);

        probar(club, "Otro deporte permitido",
               juan, equipoBasket, 5, true);

        probar(club, "Completar cupos",
               ana, equipoA, 11, true);

        probar(club, "Equipo lleno",
               pedro, equipoA, 12, false);

        probar(club, "Numero de camiseta invalido",
               pedro, equipoB, 0, false);

        System.out.println("-----------------------------");
        System.out.println("Pruebas correctas: " + correctas);
        System.out.println("Pruebas fallidas: " + fallidas);

        System.out.println("Inscritos Seneca A: "
                + equipoA.getInscripciones().size());
        System.out.println("Inscritos Seneca B: "
                + equipoB.getInscripciones().size());
        System.out.println("Inscritos Basket: "
                + equipoBasket.getInscripciones().size());
    }

    private static void probar(
            ClubDeportivoSeneca club,
            String nombre,
            Socio socio,
            Equipo equipo,
            int camiseta,
            boolean debeAceptar) {

        int antes = equipo.getInscripciones().size();

        try {
            club.inscribirSocioEnEquipo(
                socio, equipo, "Jugador", camiseta);

            int despues = equipo.getInscripciones().size();

            boolean correcto =
                debeAceptar && despues == antes + 1;

            mostrar(nombre, correcto);

        } catch (ReglaNegocioException e) {

            boolean correcto =
                !debeAceptar &&
                equipo.getInscripciones().size() == antes;

            mostrar(nombre, correcto);
        }
    }

    private static void mostrar(String nombre, boolean correcto) {

        if (correcto) {
            correctas++;
            System.out.println("[OK] " + nombre);
        } else {
            fallidas++;
            System.out.println("[FALLO] " + nombre);
        }
    }
}
