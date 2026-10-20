
package uniandes.dpoo.cds.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import uniandes.dpoo.cds.aplicacion.GestorTienda;
import uniandes.dpoo.cds.aplicacion.GestorCafeteria;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

public final class PersistenciaComercial {

    private PersistenciaComercial() {
    }

    // ESTADO DE LOS MODULOS COMERCIALES

    public static class EstadoComercial
            implements Serializable {

        private static final long serialVersionUID = 1L;

        private final GestorTienda tienda;
        private final GestorCafeteria cafeteria;

        public EstadoComercial(
                GestorTienda tienda,
                GestorCafeteria cafeteria) {

            if (tienda == null || cafeteria == null) {
                throw new IllegalArgumentException(
                    "Los dos gestores son obligatorios"
                );
            }

            this.tienda = tienda;
            this.cafeteria = cafeteria;
        }

        public GestorTienda getTienda() {
            return tienda;
        }

        public GestorCafeteria getCafeteria() {
            return cafeteria;
        }
    }

    // GUARDAR DESDE EL SISTEMA INTEGRADO

    public static void guardar(
            SistemaClub sistema,
            Path archivo)
            throws IOException {

        if (sistema == null) {
            throw new IllegalArgumentException(
                "Sistema nulo"
            );
        }

        EstadoComercial estado =
            new EstadoComercial(
                sistema.getTienda(),
                sistema.getCafeteria()
            );

        guardar(estado, archivo);
    }

    // GUARDAR UN ESTADO RECUPERADO

    public static void guardar(
            EstadoComercial estado,
            Path archivo)
            throws IOException {

        if (estado == null || archivo == null) {
            throw new IllegalArgumentException(
                "Datos o ruta invalidos"
            );
        }

        Path destino =
            archivo.toAbsolutePath().normalize();

        Path carpeta =
            destino.getParent();

        Files.createDirectories(carpeta);

        // CREAR ARCHIVO TEMPORAL

        Path temporal = Files.createTempFile(
            carpeta,
            "comercial-",
            ".tmp"
        );

        try {

            // GUARDAR TODAS LAS REFERENCIAS JUNTAS

            try (ObjectOutputStream salida =
                    new ObjectOutputStream(
                        Files.newOutputStream(temporal)
                    )) {

                salida.writeObject(estado);
            }

            // REEMPLAZAR EL ARCHIVO ANTERIOR

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

    // CARGAR INFORMACION COMERCIAL

    public static EstadoComercial cargar(Path archivo)
            throws IOException, ClassNotFoundException {

        if (archivo == null) {
            throw new IllegalArgumentException(
                "Ruta invalida"
            );
        }

        try (ObjectInputStream entrada =
                new ObjectInputStream(
                    Files.newInputStream(archivo)
                )) {

            Object leido =
                entrada.readObject();

            if (!(leido instanceof EstadoComercial)) {
                throw new IOException(
                    "Formato comercial incorrecto"
                );
            }

            return (EstadoComercial) leido;
        }
    }
}
