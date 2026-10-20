
package uniandes.dpoo.cds.consola;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.individuales.Modalidad;
import uniandes.dpoo.cds.modelo.individuales.Reserva;
import uniandes.dpoo.cds.modelo.usuarios.Socio;
import uniandes.dpoo.cds.persistencia.PersistenciaClub;

public class PruebaPersistencia {

    public static void main(String[] args) throws Exception {

        ClubDeportivoSeneca original =
            new ClubDeportivoSeneca();

        Socio jugador = new Socio(
            "jugador01",
            "clavePrueba",
            LocalDate.of(2003, 5, 15)
        );

        Deporte futbol = new Deporte("Futbol", 7);

        Categoria mayores =
            new Categoria("Mayores", 18, 100);

        Equipo equipo = new Equipo(
            "Seneca A", futbol, mayores, 20
        );

        original.registrarEquipo(equipo);

        original.inscribirSocioEnEquipo(
            jugador, equipo, "Defensa", 10
        );

        Disciplina tenis = new Disciplina("Tenis", 2);

        Instalacion cancha = new Instalacion(
            "TEN-01", tenis, 4, false
        );

        Reserva reserva = new Reserva(
            jugador,
            cancha,
            LocalDate.now().plusDays(7),
            LocalTime.of(10, 0),
            LocalTime.of(11, 0),
            2,
            Modalidad.SENCILLOS
        );

        original.registrarReserva(reserva);

        // GUARDAR LOS DATOS EN ARCHIVO

        Path archivo = Path.of("datos", "club.dat");

        PersistenciaClub.guardar(original, archivo);

        System.out.println(
            "Datos guardados en: "
            + archivo.toAbsolutePath()
        );

        // CARGAR LOS DATOS GUARDADOS

        ClubDeportivoSeneca recuperado =
            PersistenciaClub.cargar(archivo);

        System.out.println("PRUEBA DE PERSISTENCIA");
        System.out.println("------------------------");

        System.out.println(
            "Equipos recuperados: "
            + recuperado.getEquipos().size()
        );

        System.out.println(
            "Inscripciones recuperadas: "
            + recuperado.getEquipos().get(0)
                       .getInscripciones().size()
        );

        System.out.println(
            "Reservas recuperadas: "
            + recuperado.getReservas().size()
        );

        System.out.println(
            "Socio recuperado: "
            + recuperado.getReservas().get(0)
                       .getSocio().getLogin()
        );

        boolean completo =
            recuperado.getEquipos().size() == 1 &&
            recuperado.getEquipos().get(0)
                      .getInscripciones().size() == 1 &&
            recuperado.getReservas().size() == 1 &&
            "jugador01".equals(
                recuperado.getReservas().get(0)
                          .getSocio().getLogin()
            );

        System.out.println(
            completo
            ? "[OK] Los datos se recuperaron correctamente"
            : "[ERROR] Faltan datos tras la carga"
        );

        // COMPROBAR QUE LAS VALIDACIONES SIGUEN FUNCIONANDO

        Reserva repetida = new Reserva(
            jugador,
            cancha,
            reserva.getFecha(),
            LocalTime.of(10, 30),
            LocalTime.of(11, 30),
            2,
            Modalidad.SENCILLOS
        );

        try {
            recuperado.registrarReserva(repetida);

            System.out.println(
                "[ERROR] Se permitio una reserva cruzada"
            );

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] Conflicto detectado tras cargar:"
            );

            System.out.println("     " + e.getMessage());
        }
    }
}
