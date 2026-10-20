
package uniandes.dpoo.cds.persistencia;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.time.DayOfWeek;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uniandes.dpoo.cds.aplicacion.GestorCafeteria;
import uniandes.dpoo.cds.aplicacion.GestorFidelidad;
import uniandes.dpoo.cds.aplicacion.GestorTienda;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.RegistroMensualidad;
import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto;

public final class PersistenciaComercialIntegrada {

    private PersistenciaComercialIntegrada() {
    }

    // ESTADO COMERCIAL QUE SE GUARDA

    private static class Estado implements Serializable {

        private static final long serialVersionUID = 1L;

        private final GestorFidelidad fidelidad;
        private final GestorTienda tienda;
        private final GestorCafeteria cafeteria;

        private final List<RegistroMensualidad> mensualidades;

        // NUEVO: HISTORIAL DE SUGERENCIAS

        private final List<SugerenciaProducto> sugerencias;

        private Estado(SistemaClub sistema) {

            fidelidad = sistema.getFidelidad();

            tienda = sistema.getTienda();

            cafeteria = sistema.getCafeteria();

            mensualidades = new ArrayList<>(
                sistema.getReportesFinancieros()
                    .getMensualidades()
            );

            // GUARDAR PROPUESTAS Y DECISIONES

            sugerencias = new ArrayList<>(
                sistema.getSugerencias()
                    .getSugerencias()
            );
        }
    }

    // GUARDAR SISTEMA COMERCIAL

    public static void guardar(
            SistemaClub sistema,
            Path archivo)
            throws IOException {

        if (sistema == null || archivo == null) {

            throw new IllegalArgumentException(
                "El sistema y la ruta son obligatorios"
            );
        }

        Path destino =
            archivo.toAbsolutePath().normalize();

        Path carpeta = destino.getParent();

        Files.createDirectories(carpeta);

        // CREAR ARCHIVO TEMPORAL

        Path temporal = Files.createTempFile(
            carpeta,
            "comercial-integrado-",
            ".tmp"
        );

        try {

            // GUARDAR TODOS LOS DATOS COMERCIALES

            try (ObjectOutputStream salida =
                    new ObjectOutputStream(
                        Files.newOutputStream(temporal))) {

                salida.writeObject(
                    new Estado(sistema)
                );
            }

            // GUARDAR EN LA UBICACION FINAL

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

    // RECUPERAR SISTEMA COMERCIAL

    public static SistemaClub cargarSistema(
            Path archivo,
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion)
            throws IOException, ClassNotFoundException {

        if (archivo == null) {

            throw new IllegalArgumentException(
                "La ruta es obligatoria"
            );
        }

        Estado estado;

        // LEER DATOS GUARDADOS

        try (ObjectInputStream entrada =
                new ObjectInputStream(
                    Files.newInputStream(archivo))) {

            Object objeto =
                entrada.readObject();

            if (!(objeto instanceof Estado)) {

                throw new IOException(
                    "Formato de estado comercial incorrecto"
                );
            }

            estado = (Estado) objeto;
        }

        // COMPROBAR INFORMACION OBLIGATORIA

        if (estado.fidelidad == null ||
            estado.tienda == null ||
            estado.cafeteria == null) {

            throw new IOException(
                "El estado comercial esta incompleto"
            );
        }

        // RECONSTRUIR SISTEMA CENTRAL

        SistemaClub sistema = new SistemaClub(
            apertura,
            cierre,
            diasAtencion,
            estado.fidelidad,
            estado.tienda,
            estado.cafeteria
        );

        try {

            // RESTAURAR MENSUALIDADES

            if (estado.mensualidades != null) {

                for (RegistroMensualidad pago :
                        estado.mensualidades) {

                    if (pago == null) {

                        throw new IOException(
                            "Mensualidad nula en el archivo"
                        );
                    }

                    sistema.getReportesFinancieros()
                        .registrarMensualidad(
                            pago.getSocio(),
                            pago.getPeriodoCorrespondiente(),
                            pago.getFechaPago(),
                            pago.getValorPagado()
                        );
                }
            }

            // NUEVO: RESTAURAR SUGERENCIAS

            if (estado.sugerencias != null) {

                sistema.getSugerencias()
                    .restaurarHistorial(
                        estado.sugerencias
                    );
            }

        } catch (ReglaNegocioException e) {

            throw new IOException(
                "No se pudo restaurar el historial comercial",
                e
            );
        }

        return sistema;
    }
}
