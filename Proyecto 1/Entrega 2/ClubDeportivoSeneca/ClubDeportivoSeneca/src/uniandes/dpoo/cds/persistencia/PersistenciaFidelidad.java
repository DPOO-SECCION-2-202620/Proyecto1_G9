
package uniandes.dpoo.cds.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import uniandes.dpoo.cds.aplicacion.GestorFidelidad;

public final class PersistenciaFidelidad {

    // EVITAR CREAR OBJETOS DE ESTA CLASE

    private PersistenciaFidelidad() {
    }

    // GUARDAR PUNTOS DE FIDELIDAD

    public static void guardar(
            GestorFidelidad fidelidad,
            Path archivo)
            throws IOException {

        if (fidelidad == null || archivo == null) {

            throw new IllegalArgumentException(
                "Fidelidad o ruta no validos"
            );
        }

        // OBTENER UBICACION DEL ARCHIVO

        Path destino =
            archivo.toAbsolutePath().normalize();

        Path carpeta =
            destino.getParent();

        // CREAR CARPETA SI NO EXISTE

        Files.createDirectories(carpeta);

        // CREAR ARCHIVO TEMPORAL

        Path temporal = Files.createTempFile(
            carpeta,
            "fidelidad-",
            ".tmp"
        );

        try {

            // ESCRIBIR LOS DATOS

            try (ObjectOutputStream salida =
                    new ObjectOutputStream(
                        Files.newOutputStream(temporal)
                    )) {

                salida.writeObject(fidelidad);
            }

            // REEMPLAZAR ARCHIVO ANTERIOR

            try {

                Files.move(
                    temporal,
                    destino,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                );

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                    temporal,
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
                );
            }

        } finally {

            Files.deleteIfExists(temporal);
        }
    }

    // CARGAR PUNTOS DE FIDELIDAD

    public static GestorFidelidad cargar(
            Path archivo)
            throws IOException, ClassNotFoundException {

        if (archivo == null) {

            throw new IllegalArgumentException(
                "Ruta no valida"
            );
        }

        // LEER ARCHIVO GUARDADO

        try (ObjectInputStream entrada =
                new ObjectInputStream(
                    Files.newInputStream(archivo)
                )) {

            Object objeto =
                entrada.readObject();

            // VALIDAR EL TIPO DE ARCHIVO

            if (!(objeto instanceof GestorFidelidad)) {

                throw new IOException(
                    "El archivo no contiene un gestor de fidelidad"
                );
            }

            return (GestorFidelidad) objeto;
        }
    }
}
