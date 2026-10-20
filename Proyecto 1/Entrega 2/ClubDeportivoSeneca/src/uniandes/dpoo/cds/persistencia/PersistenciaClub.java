
package uniandes.dpoo.cds.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import uniandes.dpoo.cds.aplicacion.ClubDeportivoSeneca;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.individuales.Reserva;

public class PersistenciaClub {

    public static void guardar(ClubDeportivoSeneca club, Path archivo)
            throws IOException {

        if (club == null || archivo == null) {
            throw new IllegalArgumentException(
                "Club o ruta no validos"
            );
        }

        Path ruta = archivo.toAbsolutePath();
        Files.createDirectories(ruta.getParent());

        EstadoClub datos = new EstadoClub(
            club.getEquipos(), club.getReservas()
        );

        try (ObjectOutputStream salida = new ObjectOutputStream(
                Files.newOutputStream(ruta))) {

            salida.writeObject(datos);
        }
    }

    public static ClubDeportivoSeneca cargar(Path archivo)
            throws IOException, ClassNotFoundException,
                   ReglaNegocioException {

        if (archivo == null) {
            throw new IllegalArgumentException(
                "Ruta no valida"
            );
        }

        EstadoClub datos;

        try (ObjectInputStream entrada = new ObjectInputStream(
                Files.newInputStream(archivo))) {

            Object objeto = entrada.readObject();

            if (!(objeto instanceof EstadoClub)) {
                throw new IOException(
                    "Formato de archivo incorrecto"
                );
            }

            datos = (EstadoClub) objeto;
        }

        ClubDeportivoSeneca club = new ClubDeportivoSeneca();

        for (Equipo equipo : datos.getEquipos()) {
            club.registrarEquipo(equipo);
        }

        for (Reserva reserva : datos.getReservas()) {
            club.registrarReserva(reserva);
        }

        return club;
    }
}
