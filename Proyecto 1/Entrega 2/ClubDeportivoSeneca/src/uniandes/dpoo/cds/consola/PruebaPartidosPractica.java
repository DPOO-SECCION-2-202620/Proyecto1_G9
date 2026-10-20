
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.GestorPartidosPractica;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.individuales.*;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaPartidosPractica {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void aceptar(
            String caso,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + caso);
            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + caso + ": " + e.getMessage()
            );

            fallidas++;
        }
    }

    private static void rechazar(
            String caso,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + caso + ": fue aceptado"
            );

            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + caso + " -> " + e.getMessage()
            );

            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + caso + ": " + e
            );

            fallidas++;
        }
    }

    private static void verificar(
            String caso,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ") + caso
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    public static void main(String[] args)
            throws Exception {

        GestorPartidosPractica gestor =
            new GestorPartidosPractica();

        // CREAR DISCIPLINAS

        Disciplina tenis =
            new Disciplina("Tenis", 2);

        Disciplina padel =
            new Disciplina("Padel", 1);

        Disciplina mesa =
            new Disciplina("Tenis de mesa", 1);

        // CREAR SOCIOS

        Socio ana = new Socio(
            "ana01",
            "demo",
            LocalDate.of(2003, 1, 1)
        );

        Socio juan = new Socio(
            "juan01",
            "demo",
            LocalDate.of(2002, 1, 1)
        );

        Socio lucia = new Socio(
            "lucia01",
            "demo",
            LocalDate.of(2001, 1, 1)
        );

        Socio profeJugador = new Socio(
            "profe01",
            "demo",
            LocalDate.of(1998, 1, 1)
        );

        Socio desconocido = new Socio(
            "noRegistrado",
            "demo",
            LocalDate.of(2001, 1, 1)
        );

        LocalDate fecha =
            LocalDate.of(2026, 11, 12);

        System.out.println(
            "PRUEBAS DE PARTIDOS INDIVIDUALES Y RANKING"
        );

        System.out.println(
            "------------------------------------------"
        );

        // INSCRIPCIONES

        rechazar("Inscripcion nula", () ->
            gestor.registrarInscripcion(null)
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                ana, tenis, Nivel.PRINCIPIANTE
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                juan, tenis, Nivel.INTERMEDIO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                lucia, tenis, Nivel.AVANZADO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                profeJugador, tenis, Nivel.AVANZADO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                ana, padel, Nivel.INTERMEDIO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                juan, padel, Nivel.INTERMEDIO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                lucia, padel, Nivel.AVANZADO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                profeJugador, padel, Nivel.AVANZADO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                ana, mesa, Nivel.INTERMEDIO
            )
        );

        gestor.registrarInscripcion(
            new InscripcionDisciplina(
                juan, mesa, Nivel.INTERMEDIO
            )
        );

        verificar(
            "Ranking inicial de tenis = 1000",
            gestor.getRanking(ana, tenis) == 1000.0
        );

        rechazar("Inscripcion duplicada", () ->
            gestor.registrarInscripcion(
                new InscripcionDisciplina(
                    ana, tenis, Nivel.PRINCIPIANTE
                )
            )
        );

        // VALIDAR NIVELES Y PARTICIPANTES

        rechazar("Jugador no inscrito", () ->
            gestor.programarPartido(
                fecha,
                tenis,
                Modalidad.SENCILLOS,
                ana,
                desconocido
            )
        );

        rechazar("Niveles no adyacentes", () ->
            gestor.programarPartido(
                fecha,
                tenis,
                Modalidad.SENCILLOS,
                ana,
                lucia
            )
        );

        rechazar("Participante repetido", () ->
            gestor.programarPartido(
                fecha,
                tenis,
                Modalidad.SENCILLOS,
                ana,
                ana
            )
        );

        rechazar("Sencillos con tres participantes", () ->
            gestor.programarPartido(
                fecha,
                tenis,
                Modalidad.SENCILLOS,
                ana,
                juan,
                lucia
            )
        );

        // EXCEPCION DE ENTRENADOR

        gestor.registrarEntrenador(
            new Entrenador(
                "profe01",
                "demo",
                "DESC1"
            )
        );

        aceptar(
            "Excepcion por socio que tambien es entrenador",
            () -> gestor.programarPartido(
                fecha,
                tenis,
                Modalidad.SENCILLOS,
                ana,
                profeJugador
            )
        );

        rechazar("Padel no permite sencillos", () ->
            gestor.programarPartido(
                fecha,
                padel,
                Modalidad.SENCILLOS,
                ana,
                juan
            )
        );

        // PARTIDO DE TENIS

        PartidoPractica pTenis =
            gestor.programarPartido(
                fecha,
                tenis,
                Modalidad.SENCILLOS,
                ana,
                juan
            );

        verificar(
            "Partido de tenis creado",
            pTenis.getParticipantes().size() == 2
        );

        rechazar("No finalizar sin estadisticas", () ->
            gestor.finalizarPartido(pTenis, ana)
        );

        rechazar("Estadistica de jugador ajeno", () ->
            gestor.registrarEstadistica(
                pTenis,
                new EstadisticaRaqueta(
                    desconocido, 0, 0, 0, 0
                )
            )
        );

        rechazar("Tipo de estadistica equivocado", () ->
            gestor.registrarEstadistica(
                pTenis,
                new EstadisticaTenisMesa(
                    ana, 1, 11
                )
            )
        );

        aceptar("Registrar estadistica de Ana", () ->
            gestor.registrarEstadistica(
                pTenis,
                new EstadisticaRaqueta(
                    ana, 2, 12, 3, 1
                )
            )
        );

        rechazar("No duplicar estadistica", () ->
            gestor.registrarEstadistica(
                pTenis,
                new EstadisticaRaqueta(
                    ana, 2, 12, 3, 1
                )
            )
        );

        aceptar("Registrar estadistica de Juan", () ->
            gestor.registrarEstadistica(
                pTenis,
                new EstadisticaRaqueta(
                    juan, 1, 7, 1, 4
                )
            )
        );

        // FINALIZAR PARTIDO Y COMPROBAR ELO

        aceptar("Finalizar tenis con Ana ganadora", () ->
            gestor.finalizarPartido(pTenis, ana)
        );

        verificar(
            "Ranking ELO Ana +16",
            gestor.getRanking(ana, tenis) == 1016.0
        );

        verificar(
            "Ranking ELO Juan -16",
            gestor.getRanking(juan, tenis) == 984.0
        );

        rechazar("No finalizar dos veces", () ->
            gestor.finalizarPartido(pTenis, ana)
        );

        rechazar(
            "No agregar estadisticas tras finalizar",
            () -> gestor.registrarEstadistica(
                pTenis,
                new EstadisticaRaqueta(
                    ana, 0, 0, 0, 0
                )
            )
        );

        // PARTIDO DE PADEL DOBLES

        PartidoPractica pPadel =
            gestor.programarPartido(
                fecha,
                padel,
                Modalidad.DOBLES,
                ana,
                juan,
                lucia,
                profeJugador
            );

        verificar(
            "Padel requiere cuatro jugadores",
            pPadel.getParticipantes().size() == 4
        );

        for (Socio socio : pPadel.getParticipantes()) {

            gestor.registrarEstadistica(
                pPadel,
                new EstadisticaRaqueta(
                    socio, 1, 6, 2, 3
                )
            );
        }

        rechazar("Ganadores de equipos mezclados", () ->
            gestor.finalizarPartido(
                pPadel, ana, lucia
            )
        );

        aceptar("Finalizar padel dobles", () ->
            gestor.finalizarPartido(
                pPadel, ana, juan
            )
        );

        verificar(
            "Ranking de padel actualizado para ambos equipos",

            gestor.getRanking(ana, padel) == 1016.0 &&
            gestor.getRanking(juan, padel) == 1016.0 &&
            gestor.getRanking(lucia, padel) == 984.0
        );

        verificar(
            "Rankings por disciplina independientes",

            gestor.getRanking(ana, tenis) == 1016.0 &&
            gestor.getRanking(juan, tenis) == 984.0
        );

        // PARTIDO DE TENIS DE MESA

        PartidoPractica pMesa =
            gestor.programarPartido(
                fecha,
                mesa,
                Modalidad.SENCILLOS,
                ana,
                juan
            );

        gestor.registrarEstadistica(
            pMesa,
            new EstadisticaTenisMesa(
                ana, 3, 33
            )
        );

        gestor.registrarEstadistica(
            pMesa,
            new EstadisticaTenisMesa(
                juan, 1, 27
            )
        );

        aceptar("Finalizar tenis de mesa", () ->
            gestor.finalizarPartido(
                pMesa, juan
            )
        );

        verificar(
            "Ranking ELO tenis de mesa",

            gestor.getRanking(ana, mesa) == 984.0 &&
            gestor.getRanking(juan, mesa) == 1016.0
        );

        // VERIFICACIONES FINALES

        verificar(
            "Tres partidos finalizados",

            pTenis.isFinalizado() &&
            pPadel.isFinalizado() &&
            pMesa.isFinalizado()
        );

        verificar(
            "Estadisticas de padel = 4",
            pPadel.getEstadisticas().size() == 4
        );

        verificar(
            "Total de partidos = 4",
            gestor.getPartidos().size() == 4
        );

        System.out.println(
            "------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Partidos programados: "
            + gestor.getPartidos().size()
        );

        System.out.println(
            "Ranking Ana tenis: "
            + gestor.getRanking(ana, tenis)
        );
    }
}
