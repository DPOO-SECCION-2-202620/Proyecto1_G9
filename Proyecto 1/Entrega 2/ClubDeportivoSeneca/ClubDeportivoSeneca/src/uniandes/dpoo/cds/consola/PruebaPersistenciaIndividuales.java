
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.SistemaClub;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.individuales.*;
import uniandes.dpoo.cds.modelo.usuarios.*;
import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class PruebaPersistenciaIndividuales {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void verificar(String nombre, boolean cumple) {
        System.out.println((cumple ? "[OK] " : "[FALLO] ") + nombre);
        if (cumple) correctas++; else fallidas++;
    }

    private static void rechazar(String nombre, Accion accion) {
        try {
            accion.ejecutar();
            verificar(nombre, false);
        } catch (ReglaNegocioException esperado) {
            verificar(nombre, true);
        } catch (Exception inesperado) {
            System.out.println("Error inesperado: " + inesperado);
            verificar(nombre, false);
        }
    }

    private static SistemaClub nuevo() {
        return new SistemaClub(LocalTime.of(8, 0), LocalTime.of(18, 0),
                EnumSet.of(DayOfWeek.MONDAY));
    }

    private static SistemaClub cargar(Path ruta) throws Exception {
        return PersistenciaSistema.cargarSistema(ruta, LocalTime.of(8, 0),
                LocalTime.of(18, 0), EnumSet.of(DayOfWeek.MONDAY));
    }

    public static void main(String[] args) throws Exception {
        Path archivo = Path.of("datos", "sistema-individuales-prueba.dat");
        LocalDate fecha = LocalDate.of(2026, 11, 15);
        Disciplina tenis = new Disciplina("Tenis", 2);
        Disciplina mesa = new Disciplina("Tenis de mesa", 1);

        Socio ana = new Socio("anaPI", "demo", LocalDate.of(2000, 1, 1));
        Socio juan = new Socio("juanPI", "demo", LocalDate.of(2000, 1, 1));
        Socio lucia = new Socio("luciaPI", "demo", LocalDate.of(2000, 1, 1));
        Socio profesorSocio = new Socio("profePI", "demo", LocalDate.of(2000, 1, 1));
        Entrenador entrenador = new Entrenador("profePI", "demo", "PROF-PI");
        Administrador admin = new Administrador("adminPI", "demo");

        SistemaClub inicial = nuevo();

        // Registrar jugadores y sus niveles.
        inicial.getPartidosIndividuales().registrarInscripcion(
            new InscripcionDisciplina(ana, tenis, Nivel.PRINCIPIANTE));
        inicial.getPartidosIndividuales().registrarInscripcion(
            new InscripcionDisciplina(juan, tenis, Nivel.INTERMEDIO));
        inicial.getPartidosIndividuales().registrarInscripcion(
            new InscripcionDisciplina(lucia, tenis, Nivel.AVANZADO));
        inicial.getPartidosIndividuales().registrarInscripcion(
            new InscripcionDisciplina(profesorSocio, tenis, Nivel.AVANZADO));
        inicial.getPartidosIndividuales().registrarInscripcion(
            new InscripcionDisciplina(ana, mesa, Nivel.INTERMEDIO));
        inicial.getPartidosIndividuales().registrarInscripcion(
            new InscripcionDisciplina(juan, mesa, Nivel.INTERMEDIO));

        inicial.getPartidosIndividuales().registrarEntrenador(entrenador);

        // Primer partido de tenis: gana Ana.
        PartidoPractica terminado = inicial.getPartidosIndividuales().programarPartido(
            fecha, tenis, Modalidad.SENCILLOS, ana, juan);

        inicial.getPartidosIndividuales().registrarEstadistica(
            terminado, new EstadisticaRaqueta(ana, 2, 12, 3, 1));
        inicial.getPartidosIndividuales().registrarEstadistica(
            terminado, new EstadisticaRaqueta(juan, 1, 7, 1, 4));
        inicial.getPartidosIndividuales().finalizarPartido(terminado, ana);

        // Segundo partido: queda pendiente con una estadistica.
        PartidoPractica pendiente = inicial.getPartidosIndividuales().programarPartido(
            fecha.plusDays(1), tenis, Modalidad.SENCILLOS, ana, juan);

        inicial.getPartidosIndividuales().registrarEstadistica(
            pendiente, new EstadisticaRaqueta(ana, 1, 5, 0, 2));

        // Partido de tenis de mesa: gana Juan.
        PartidoPractica juegoMesa = inicial.getPartidosIndividuales().programarPartido(
            fecha, mesa, Modalidad.SENCILLOS, ana, juan);

        inicial.getPartidosIndividuales().registrarEstadistica(
            juegoMesa, new EstadisticaTenisMesa(ana, 1, 22));
        inicial.getPartidosIndividuales().registrarEstadistica(
            juegoMesa, new EstadisticaTenisMesa(juan, 3, 33));
        inicial.getPartidosIndividuales().finalizarPartido(juegoMesa, juan);

        // Verificar que los datos comerciales siguen guardandose.
        inicial.getTienda().registrarArticulo(
            new Articulo("BAL-PI", "Balon de prueba", "Balones", 10000), 2);
        inicial.getTienda().vender(ana, Map.of("BAL-PI", 1), null, 0);

        System.out.println("PRUEBAS DE PERSISTENCIA DE INDIVIDUALES Y ELO");
        System.out.println("----------------------------------------------");

        verificar("Tres partidos individuales originales",
            inicial.getPartidosIndividuales().getPartidos().size() == 3);

        verificar("ELO inicial de tenis actualizado (1016 y 984)",
            inicial.getPartidosIndividuales().getRanking(ana, tenis) == 1016.0 &&
            inicial.getPartidosIndividuales().getRanking(juan, tenis) == 984.0);

        verificar("ELO de tenis de mesa independiente",
            inicial.getPartidosIndividuales().getRanking(ana, mesa) == 984.0 &&
            inicial.getPartidosIndividuales().getRanking(juan, mesa) == 1016.0);

        verificar("Partido pendiente conserva una estadistica",
            !pendiente.isFinalizado() && pendiente.getEstadisticas().size() == 1);

        // Guardar y reiniciar.
        PersistenciaSistema.guardar(inicial, archivo);

        verificar("Archivo nuevo de persistencia creado",
            Files.isRegularFile(archivo));

        SistemaClub restaurado = cargar(archivo);

        verificar("Tres partidos recuperados",
            restaurado.getPartidosIndividuales().getPartidos().size() == 3);

        verificar("Ranking ELO de tenis recuperado sin recalcular",
            restaurado.getPartidosIndividuales().getRanking(ana, tenis) == 1016.0 &&
            restaurado.getPartidosIndividuales().getRanking(juan, tenis) == 984.0);

        verificar("Ranking ELO de mesa recuperado por separado",
            restaurado.getPartidosIndividuales().getRanking(ana, mesa) == 984.0 &&
            restaurado.getPartidosIndividuales().getRanking(juan, mesa) == 1016.0);

        PartidoPractica terminadoRestaurado =
            restaurado.getPartidosIndividuales().getPartidos().get(0);

        PartidoPractica pendienteRestaurado =
            restaurado.getPartidosIndividuales().getPartidos().get(1);

        verificar("Ganador y estadisticas del partido finalizado recuperados",
            terminadoRestaurado.isFinalizado() &&
            terminadoRestaurado.getGanadores().size() == 1 &&
            terminadoRestaurado.getGanadores().get(0).getLogin().equals("anaPI") &&
            terminadoRestaurado.getEstadisticas().size() == 2);

        verificar("Partido incompleto conserva estadistica y sigue pendiente",
            !pendienteRestaurado.isFinalizado() &&
            pendienteRestaurado.getEstadisticas().size() == 1);

        rechazar("No repetir estadistica restaurada", () ->
            restaurado.getPartidosIndividuales().registrarEstadistica(
                pendienteRestaurado, new EstadisticaRaqueta(ana, 1, 5, 0, 2)));

        rechazar("Niveles no adyacentes siguen rechazandose", () ->
            restaurado.getPartidosIndividuales().programarPartido(
                fecha.plusDays(3), tenis, Modalidad.SENCILLOS, ana, lucia));

        verificar("Reporte deportivo cuenta dos partidos individuales finalizados",
            restaurado.getReportesDeportivos().generarResumen(admin,
                fecha, fecha.plusDays(3)).getPartidosPracticaFinalizados() == 2);

        verificar("Venta e inventario comercial se conservan",
            restaurado.getTienda().getVentas().size() == 1 &&
            restaurado.getTienda().getExistencias("BAL-PI") == 1);

        // Continuar un partido que estaba pendiente.
        restaurado.getPartidosIndividuales().registrarEstadistica(
            pendienteRestaurado, new EstadisticaRaqueta(juan, 2, 10, 1, 0));
        restaurado.getPartidosIndividuales().finalizarPartido(pendienteRestaurado, juan);

        verificar("Finalizar pendiente recuperado actualiza ELO desde 1016/984",
            pendienteRestaurado.isFinalizado() &&
            restaurado.getPartidosIndividuales().getRanking(ana, tenis) < 1016.0 &&
            restaurado.getPartidosIndividuales().getRanking(juan, tenis) > 984.0);

        // La excepcion del entrenador sigue funcionando.
        PartidoPractica conEntrenador = restaurado.getPartidosIndividuales()
            .programarPartido(fecha.plusDays(4), tenis, Modalidad.SENCILLOS,
                ana, profesorSocio);

        verificar("Excepcion de niveles del entrenador conservada",
            conEntrenador != null && !conEntrenador.isFinalizado());

        // Segundo guardado y reinicio.
        PersistenciaSistema.guardar(restaurado, archivo);
        SistemaClub segundaCarga = cargar(archivo);

        verificar("Segundo reinicio conserva cuatro partidos",
            segundaCarga.getPartidosIndividuales().getPartidos().size() == 4);

        verificar("Segundo reinicio conserva ELO actualizado",
            segundaCarga.getPartidosIndividuales().getRanking(ana, tenis) ==
                restaurado.getPartidosIndividuales().getRanking(ana, tenis));

        verificar("Segundo reinicio conserva encuentro pendiente",
            !segundaCarga.getPartidosIndividuales().getPartidos().get(3).isFinalizado());

        verificar("Segundo reinicio conserva estadisticas de tenis y mesa",
            segundaCarga.getPartidosIndividuales().getPartidos().get(0)
                .getEstadisticas().size() == 2 &&
            segundaCarga.getPartidosIndividuales().getPartidos().get(2)
                .getEstadisticas().size() == 2);

        System.out.println("----------------------------------------------");
        System.out.println("Pruebas correctas: " + correctas);
        System.out.println("Pruebas fallidas: " + fallidas);
        System.out.println("Partidos individuales recuperados: " +
            segundaCarga.getPartidosIndividuales().getPartidos().size());
        System.out.println("ELO tenis Ana recuperado: " +
            segundaCarga.getPartidosIndividuales().getRanking(ana, tenis));
    }
}
