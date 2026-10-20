
package uniandes.dpoo.cds.consola;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.GestorUsuarios.Rol;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaArranqueSistema {

    private static int correctas;
    private static int fallidas;

    // VERIFICAR RESULTADOS

    private static void verificar(
            String texto,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ")
            + texto
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // COMPROBAR QUE UNA SESION YA NO ES VALIDA

    private static boolean tokenNoValido(
            SistemaClub sistema,
            String token) {

        try {

            sistema.getUsuarios()
                .usuarioAutenticado(token);

            return false;

        } catch (ReglaNegocioException esperado) {

            return true;
        }
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // CARPETA TEMPORAL INDEPENDIENTE

        Path carpeta = Files.createTempDirectory(
            "seneca-arranque-"
        );

        Path archivo = carpeta.resolve(
            "sistema-prueba.dat"
        );

        Path danado = carpeta.resolve(
            "corrupto.dat"
        );

        try {

            System.out.println(
                "PRUEBAS DE ARRANQUE Y GUARDADO GENERAL"
            );

            System.out.println(
                "----------------------------------------"
            );

            // PRIMERA APERTURA

            SistemaClub primero =
                Main.cargarOCrear(archivo);

            verificar(
                "Primera apertura crea sistema cuando no hay archivo",

                primero != null &&
                !Files.exists(archivo)
            );

            verificar(
                "Sistema nuevo tiene usuarios y gestores deportivos",

                primero.getUsuarios() != null &&
                primero.getTurnos() != null &&
                primero.getPartidosIndividuales() != null
            );

            // CREAR ADMINISTRADOR

            primero.getUsuarios()
                .registrarAdministradorInicial(
                    new Administrador(
                        "adminFinal",
                        "ClaveFinal2026!"
                    )
                );

            String tokenViejo =
                primero.getUsuarios().iniciarSesion(
                    "adminFinal",
                    "ClaveFinal2026!"
                );

            verificar(
                "Administrador inicial puede iniciar sesion",

                primero.getUsuarios()
                    .rolAutenticado(tokenViejo)
                    == Rol.ADMINISTRADOR
            );

            // REGISTRAR SOCIO

            primero.getUsuarios()
                .registrarUsuario(
                    tokenViejo,

                    new Socio(
                        "socioFinal",
                        "ClaveSocio2026!",
                        LocalDate.of(2000, 1, 1)
                    )
                );

            // REGISTRAR EQUIPO

            primero.getClub()
                .registrarEquipo(
                    new Equipo(
                        "Seneca final",
                        new Deporte(
                            "Futbol",
                            7
                        ),
                        new Categoria(
                            "Mayores",
                            18,
                            99
                        ),
                        15
                    )
                );

            verificar(
                "Cambios permanecen en memoria antes de guardar",

                primero.getClub()
                    .getEquipos().size() == 1 &&

                primero.getUsuarios()
                    .getUsuarios(tokenViejo).size() == 2
            );

            // PRIMER GUARDADO

            Main.guardar(
                primero,
                archivo
            );

            verificar(
                "Se crea el archivo general de datos",

                Files.isRegularFile(archivo)
            );

            long tamano =
                Files.size(archivo);

            verificar(
                "El archivo general no esta vacio",

                tamano > 0
            );

            // PRIMER REINICIO

            SistemaClub segundo =
                Main.cargarOCrear(archivo);

            verificar(
                "Reapertura recupera equipo",

                segundo.getClub()
                    .getEquipos().size() == 1
            );

            verificar(
                "Reapertura recupera nombre del equipo",

                segundo.getClub()
                    .getEquipos().get(0)
                    .getNombre()
                    .equals("Seneca final")
            );

            verificar(
                "No restaura una sesion antigua",

                tokenNoValido(
                    segundo,
                    tokenViejo
                )
            );

            // NUEVO INICIO DE SESION

            String nuevoToken =
                segundo.getUsuarios().iniciarSesion(
                    "adminFinal",
                    "ClaveFinal2026!"
                );

            verificar(
                "El administrador puede volver a iniciar sesion",

                segundo.getUsuarios()
                    .rolAutenticado(nuevoToken)
                    == Rol.ADMINISTRADOR
            );

            verificar(
                "El usuario socio conserva su rol",

                segundo.getUsuarios()
                    .rolAutenticado(
                        segundo.getUsuarios()
                            .iniciarSesion(
                                "socioFinal",
                                "ClaveSocio2026!"
                            )
                    ) == Rol.SOCIO
            );

            verificar(
                "El archivo cargado incluye ambos usuarios",

                segundo.getUsuarios()
                    .getUsuarios(nuevoToken).size() == 2
            );

            // REGISTRAR OTRO EQUIPO

            segundo.getClub()
                .registrarEquipo(
                    new Equipo(
                        "Seneca nuevo",
                        new Deporte(
                            "Baloncesto",
                            5
                        ),
                        new Categoria(
                            "Mayores",
                            18,
                            99
                        ),
                        10
                    )
                );

            // SEGUNDO GUARDADO

            Main.guardar(
                segundo,
                archivo
            );

            // SEGUNDO REINICIO

            SistemaClub tercero =
                Main.cargarOCrear(archivo);

            verificar(
                "Segundo reinicio conserva los dos equipos",

                tercero.getClub()
                    .getEquipos().size() == 2
            );

            verificar(
                "Segundo reinicio conserva usuarios",

                tercero.getUsuarios()
                    .rolAutenticado(
                        tercero.getUsuarios()
                            .iniciarSesion(
                                "adminFinal",
                                "ClaveFinal2026!"
                            )
                    ) == Rol.ADMINISTRADOR
            );

            // COMPROBAR ARCHIVO CORRUPTO

            Files.writeString(
                danado,
                "ESTO NO ES UN ESTADO JAVA VALIDO"
            );

            byte[] antes =
                Files.readAllBytes(danado);

            boolean falloDetectado = false;

            try {

                Main.cargarOCrear(danado);

            } catch (IOException |
                     ClassNotFoundException esperado) {

                falloDetectado = true;
            }

            verificar(
                "Archivo corrupto produce error en lugar de crear sistema vacio",

                falloDetectado
            );

            verificar(
                "Archivo corrupto no se modifica al intentar cargar",

                java.util.Arrays.equals(
                    antes,
                    Files.readAllBytes(danado)
                )
            );

            verificar(
                "El archivo original valido continua legible",

                Main.cargarOCrear(archivo)
                    .getClub()
                    .getEquipos().size() == 2
            );

            // RESULTADOS FINALES

            System.out.println(
                "----------------------------------------"
            );

            System.out.println(
                "Pruebas correctas: "
                + correctas
            );

            System.out.println(
                "Pruebas fallidas: "
                + fallidas
            );

            System.out.println(
                "Equipos recuperados: "
                + tercero.getClub()
                    .getEquipos().size()
            );

        } finally {

            // LIMPIAR SOLO LOS ARCHIVOS TEMPORALES
            // CREADOS POR ESTA PRUEBA

            Files.deleteIfExists(archivo);

            Files.deleteIfExists(danado);

            Files.deleteIfExists(carpeta);
        }
    }
}
