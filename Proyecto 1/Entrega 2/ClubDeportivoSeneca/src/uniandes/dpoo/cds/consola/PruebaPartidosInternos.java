
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.aplicacion.GestorPartidosInternos;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaPartidosInternos {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws ReglaNegocioException;
    }

    private static void aceptar(String nombre, Accion accion) {
        try {
            accion.ejecutar();
            System.out.println("[OK] " + nombre);
            correctas++;
        } catch (Exception e) {
            System.out.println(
                "[FALLO] " + nombre + ": " + e.getMessage()
            );
            fallidas++;
        }
    }

    private static void rechazar(String nombre, Accion accion) {
        try {
            accion.ejecutar();
            System.out.println(
                "[FALLO] " + nombre + ": fue aceptado"
            );
            fallidas++;
        } catch (ReglaNegocioException e) {
            System.out.println(
                "[OK] " + nombre + " -> " + e.getMessage()
            );
            correctas++;
        } catch (Exception e) {
            System.out.println(
                "[FALLO] " + nombre + ": " + e
            );
            fallidas++;
        }
    }

    private static void verificar(
            String nombre, boolean valido) {

        if (valido) {
            System.out.println("[OK] " + nombre);
            correctas++;
        } else {
            System.out.println("[FALLO] " + nombre);
            fallidas++;
        }
    }

    private static void llenarEquipo(
            ClubDeportivoSeneca club,
            Equipo equipo,
            String prefijo,
            int cantidad)
            throws ReglaNegocioException {

        for (int i = 1; i <= cantidad; i++) {

            Socio socio = new Socio(
                prefijo + i,
                "clavePrueba",
                LocalDate.of(2000, 3, 15)
            );

            club.inscribirSocioEnEquipo(
                socio, equipo, "Jugador", i
            );
        }
    }

    public static void main(String[] args) throws Exception {

        ClubDeportivoSeneca club =
            new ClubDeportivoSeneca();

        GestorPartidosInternos gestor =
            new GestorPartidosInternos(club);

        Deporte futbol =
            new Deporte("Futbol", 7);

        Deporte baloncesto =
            new Deporte("Baloncesto", 5);

        Categoria mayores =
            new Categoria("Mayores", 18, 99);

        Categoria sub17 =
            new Categoria("Sub-17", 15, 17);

        Equipo local =
            new Equipo("Seneca A", futbol, mayores, 20);

        Equipo visitante =
            new Equipo("Seneca B", futbol, mayores, 20);

        Equipo incompleto =
            new Equipo("Seneca C", futbol, mayores, 20);

        Equipo juvenil =
            new Equipo("Seneca Sub17", futbol, sub17, 20);

        Equipo basketA =
            new Equipo("Basket A", baloncesto, mayores, 12);

        Equipo basketB =
            new Equipo("Basket B", baloncesto, mayores, 12);

        Equipo externo =
            new Equipo("No registrado", futbol, mayores, 20);

        // REGISTRAR EQUIPOS

        for (Equipo equipo : new Equipo[] {
                local, visitante, incompleto,
                juvenil, basketA, basketB}) {

            club.registrarEquipo(equipo);
        }

        // INSCRIBIR JUGADORES

        llenarEquipo(club, local, "L", 7);
        llenarEquipo(club, visitante, "V", 7);
        llenarEquipo(club, incompleto, "C", 6);
        llenarEquipo(club, basketA, "BA", 5);
        llenarEquipo(club, basketB, "BB", 5);

        LocalDate fecha = LocalDate.of(2026, 11, 1);

        final PartidoInterno[] partidoFutbol =
            new PartidoInterno[1];

        final PartidoInterno[] partidoBasket =
            new PartidoInterno[1];

        System.out.println("PRUEBAS DE PARTIDOS INTERNOS");
        System.out.println("--------------------------------");

        aceptar("Partido de futbol programado", () ->
            partidoFutbol[0] =
                gestor.programarPartido(
                    fecha, local, visitante
                )
        );

        rechazar("Mismo equipo como rival", () ->
            gestor.programarPartido(
                fecha, local, local
            )
        );

        rechazar("Deportes distintos", () ->
            gestor.programarPartido(
                fecha, local, basketA
            )
        );

        rechazar("Categorias distintas", () ->
            gestor.programarPartido(
                fecha, local, juvenil
            )
        );

        rechazar("Equipo sin minimo de jugadores", () ->
            gestor.programarPartido(
                fecha, local, incompleto
            )
        );

        rechazar("Equipo no registrado", () ->
            gestor.programarPartido(
                fecha, local, externo
            )
        );

        rechazar("Marcador negativo", () ->
            partidoFutbol[0].registrarResultado(-1, 2)
        );

        aceptar("Empate de futbol permitido", () ->
            partidoFutbol[0].registrarResultado(2, 2)
        );

        rechazar("No modificar resultado final", () ->
            partidoFutbol[0].registrarResultado(3, 1)
        );

        aceptar("Partido de baloncesto programado", () ->
            partidoBasket[0] =
                gestor.programarPartido(
                    fecha, basketA, basketB
                )
        );

        rechazar("Empate de baloncesto prohibido", () ->
            partidoBasket[0].registrarResultado(60, 60)
        );

        aceptar("Resultado de baloncesto valido", () ->
            partidoBasket[0].registrarResultado(60, 58)
        );

        verificar("Partidos programados: 2",
            gestor.getPartidos().size() == 2
        );

        verificar("Ambos partidos finalizados",
            partidoFutbol[0].estaFinalizado() &&
            partidoBasket[0].estaFinalizado()
        );

        System.out.println("--------------------------------");
        System.out.println(
            "Pruebas correctas: " + correctas
        );
        System.out.println(
            "Pruebas fallidas: " + fallidas
        );
        System.out.println(
            "Partidos registrados: "
            + gestor.getPartidos().size()
        );
    }
}
