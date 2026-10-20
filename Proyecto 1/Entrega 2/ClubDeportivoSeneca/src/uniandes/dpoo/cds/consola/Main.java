package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import uniandes.dpoo.cds.modelo.usuarios.*;
import uniandes.dpoo.cds.modelo.conjunto.*;
import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

public class Main {

    public static void main(String[] args) {

        System.out.println("Club Deportivo Seneca");
        System.out.println("-------------------------");

        Usuario[] usuarios = {
            new Socio("juan123", "clave123",
                      LocalDate.of(2003, 5, 15)),

            new Empleado("tienda01", "clave456", "DESC01"),

            new Entrenador("profe01", "clave789", "DESC02"),

            new Fisioterapeuta("fisio01", "clave321", "DESC03"),

            new Administrador("admin01", "clave999")
        };

        for (Usuario usuario : usuarios) {
            System.out.println(
                usuario.getClass().getSimpleName()
                + ": " + usuario.getLogin()
            );
        }

        System.out.println("-------------------------");
        System.out.println("Total de usuarios: "
                           + usuarios.length);
        
        System.out.println();
        System.out.println("PRUEBA DE DEPORTES DE CONJUNTO");

        // Crear deporte
        Deporte futbol = new Deporte("Futbol", 7);

        // Categoria de prueba: rangos provisionales
        Categoria categoria = new Categoria("Prueba", 0, 99);

    // Crear equipo
    Equipo equipo = new Equipo(
        "Seneca A", futbol, categoria, 20
    );

    	// Tomar el primer socio del arreglo anterior
    	Socio jugador = (Socio) usuarios[0];

    	// Crear objeto de inscripcion
    	InscripcionEquipo registro = new InscripcionEquipo(
        jugador, equipo, "Defensa", 10
    			);

    	System.out.println("Equipo: " + equipo.getNombre());
    	System.out.println("Deporte: " + equipo.getDeporte().getNombre());
    	System.out.println("Categoria: " + equipo.getCategoria().getNombre());
    	System.out.println("Cupo maximo: " + equipo.getCupoMaximo());
    	System.out.println("Jugador: " + registro.getSocio().getLogin());
    	System.out.println("Posicion: " + registro.getPosicion());
    	System.out.println("Camiseta: " + registro.getNumeroCamiseta());
    	
    	
    	

    	System.out.println();
    	System.out.println("PRUEBA DE VALIDACIONES");

    	System.out.println("Tiene cupo: "
        + equipo.tieneCupo());

    	System.out.println("Camiseta 10 disponible: "
        + equipo.numeroCamisetaDisponible(10));

    	System.out.println("Socio inscrito: "
        + equipo.contieneSocio(jugador));

    	System.out.println("Edad 23 admitida: "
        + categoria.admiteEdad(23));


    

    System.out.println();
    System.out.println("PRUEBA DE INSCRIPCION");

    ClubDeportivoSeneca club = new ClubDeportivoSeneca();
    club.registrarEquipo(equipo);

    try {
    	club.inscribirSocioEnEquipo(
    			jugador, equipo, "Defensa", 10
    	);

    	System.out.println("Primera inscripcion exitosa");
    	System.out.println("Total inscritos: "
    			+ equipo.getInscripciones().size());

    	club.inscribirSocioEnEquipo(
    			jugador, equipo, "Delantero", 11
    	);

    	System.out.println("ERROR: se permitio duplicar al socio");

    } catch (ReglaNegocioException e) {
    System.out.println("Inscripcion rechazada: "
            + e.getMessage());
    }
    System.out.println("Total final de inscritos: "
            + equipo.getInscripciones().size());
    }

}
