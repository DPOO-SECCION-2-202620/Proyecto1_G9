
package uniandes.dpoo.cds.consola;

import java.io.Console;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.EnumSet;
import java.util.Scanner;
import java.util.Set;

import uniandes.dpoo.cds.aplicacion.GestorUsuarios.Rol;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Fisioterapeuta;
import uniandes.dpoo.cds.modelo.usuarios.Socio;
import uniandes.dpoo.cds.modelo.usuarios.Usuario;

import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class Main {

    // ARCHIVO REAL DE LA APLICACION
    // NO UTILIZAR LOS ARCHIVOS DE PRUEBAS

    private static final Path ARCHIVO =
        Path.of("datos", "sistema-seneca.dat");

    // HORARIO DE EJEMPLO DEL CLUB

    private static final LocalTime APERTURA =
        LocalTime.of(8, 0);

    private static final LocalTime CIERRE =
        LocalTime.of(18, 0);

    private static final Set<DayOfWeek> DIAS =
        EnumSet.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY
        );

    // CARGAR SISTEMA O CREAR UNO NUEVO
    // UN ARCHIVO DANADO NO SE REEMPLAZA

    public static SistemaClub cargarOCrear(
            Path ruta)
            throws IOException, ClassNotFoundException {

        if (ruta == null) {

            throw new IllegalArgumentException(
                "La ruta es obligatoria"
            );
        }

        if (Files.exists(ruta)) {

            return PersistenciaSistema.cargarSistema(
                ruta,
                APERTURA,
                CIERRE,
                DIAS
            );
        }

        return new SistemaClub(
            APERTURA,
            CIERRE,
            DIAS
        );
    }

    // GUARDAR TODOS LOS MODULOS

    public static void guardar(
            SistemaClub sistema,
            Path ruta)
            throws IOException {

        PersistenciaSistema.guardar(
            sistema,
            ruta
        );
    }

    // LEER TEXTO DESDE LA CONSOLA

    private static String leer(
            Scanner sc,
            String pregunta) {

        System.out.print(pregunta);

        return sc.nextLine().trim();
    }

    // LEER CONTRASENA

    private static String leerClave(
            Scanner sc,
            String pregunta) {

        Console consola =
            System.console();

        if (consola != null) {

            char[] clave =
                consola.readPassword(
                    "%s",
                    pregunta
                );

            if (clave == null) {
                return "";
            }

            String resultado =
                new String(clave);

            java.util.Arrays.fill(
                clave,
                '\0'
            );

            return resultado;
        }

        // EN ECLIPSE NORMALMENTE NO EXISTE
        // SYSTEM.CONSOLE()

        return leer(sc, pregunta);
    }

    // CREAR ADMINISTRADOR EN LA PRIMERA EJECUCION

    private static void crearAdministradorInicial(
            Scanner sc,
            SistemaClub sistema,
            Path ruta)
            throws IOException {

        System.out.println(
            "Primera ejecucion. "
            + "Configure el administrador inicial."
        );

        while (true) {

            String login = leer(
                sc,
                "Login del administrador: "
            );

            String clave = leerClave(
                sc,
                "Contrasena (minimo 8 caracteres): "
            );

            String confirmacion = leerClave(
                sc,
                "Repetir contrasena: "
            );

            if (!clave.equals(confirmacion)) {

                System.out.println(
                    "Las contrasenas no coinciden. "
                    + "Intente otra vez."
                );

                continue;
            }

            try {

                sistema.getUsuarios()
                    .registrarAdministradorInicial(
                        new Administrador(
                            login,
                            clave
                        )
                    );

                guardar(
                    sistema,
                    ruta
                );

                System.out.println(
                    "Administrador creado y datos guardados."
                );

                return;

            } catch (ReglaNegocioException e) {

                System.out.println(
                    "No se pudo crear: "
                    + e.getMessage()
                );
            }
        }
    }

    // MOSTRAR RESUMEN DEL CLUB

    private static void resumen(
            SistemaClub sistema) {

        System.out.println(
            "\n--- RESUMEN DEL CLUB ---"
        );

        System.out.println(
            "Equipos: "
            + sistema.getClub()
                .getEquipos().size()
        );

        System.out.println(
            "Reservas: "
            + sistema.getClub()
                .getReservas().size()
        );

        System.out.println(
            "Competencias: "
            + sistema.getCompetencias()
                .getCompetencias().size()
        );

        System.out.println(
            "Fases ASCUN: "
            + sistema.getFasesASCUN().size()
        );

        System.out.println(
            "Partidos internos: "
            + sistema.getPartidosInternos()
                .getPartidos().size()
        );

        System.out.println(
            "Partidos oficiales: "
            + sistema.getPartidosOficiales()
                .getPartidos().size()
        );

        System.out.println(
            "Partidos individuales: "
            + sistema.getPartidosIndividuales()
                .getPartidos().size()
        );

        System.out.println(
            "Entrenamientos: "
            + sistema.getEntrenamientos()
                .getSesiones().size()
        );

        System.out.println(
            "Turnos: "
            + sistema.getTurnos()
                .getTurnos().size()
        );

        System.out.println(
            "Ventas de tienda: "
            + sistema.getTienda()
                .getVentas().size()
        );

        System.out.println(
            "Mensualidades: "
            + sistema.getReportesFinancieros()
                .getMensualidades().size()
        );
    }

    // REGISTRAR USUARIO DESDE EL MENU
    // REQUIERE ADMINISTRADOR AUTENTICADO

    private static void registrarUsuario(
            Scanner sc,
            SistemaClub sistema,
            String token,
            Path ruta)
            throws ReglaNegocioException, IOException {

        sistema.getUsuarios().exigirRol(
            token,
            Rol.ADMINISTRADOR
        );

        System.out.println(
            "Roles: 1-Socio 2-Empleado "
            + "3-Entrenador 4-Fisioterapeuta "
            + "5-Administrador"
        );

        String rol = leer(
            sc,
            "Opcion: "
        );

        String login = leer(
            sc,
            "Login nuevo: "
        );

        String clave = leerClave(
            sc,
            "Contrasena nueva (minimo 8 caracteres): "
        );

        Usuario nuevo;

        switch (rol) {

            case "1":

                nuevo = new Socio(
                    login,
                    clave,
                    LocalDate.parse(
                        leer(
                            sc,
                            "Nacimiento (AAAA-MM-DD): "
                        )
                    )
                );

                break;

            case "2":

                nuevo = new Empleado(
                    login,
                    clave,
                    leer(
                        sc,
                        "Codigo de descuento: "
                    )
                );

                break;

            case "3":

                nuevo = new Entrenador(
                    login,
                    clave,
                    leer(
                        sc,
                        "Codigo de descuento: "
                    )
                );

                break;

            case "4":

                nuevo = new Fisioterapeuta(
                    login,
                    clave,
                    leer(
                        sc,
                        "Codigo de descuento: "
                    )
                );

                break;

            case "5":

                nuevo = new Administrador(
                    login,
                    clave
                );

                break;

            default:

                System.out.println(
                    "Rol no reconocido."
                );

                return;
        }

        sistema.getUsuarios().registrarUsuario(
            token,
            nuevo
        );

        guardar(
            sistema,
            ruta
        );

        System.out.println(
            "Usuario registrado y guardado."
        );
    }

    // REGISTRAR EQUIPO DESDE EL MENU
    // SOLO PARA ADMINISTRADORES

    private static void registrarEquipo(
            Scanner sc,
            SistemaClub sistema,
            String token,
            Path ruta)
            throws ReglaNegocioException, IOException {

        sistema.getUsuarios().exigirRol(
            token,
            Rol.ADMINISTRADOR
        );

        String nombre = leer(
            sc,
            "Nombre del equipo: "
        );

        String deporte = leer(
            sc,
            "Deporte: "
        );

        int minimo = Integer.parseInt(
            leer(
                sc,
                "Minimo jugadores: "
            )
        );

        String categoria = leer(
            sc,
            "Categoria: "
        );

        int edadMin = Integer.parseInt(
            leer(
                sc,
                "Edad minima: "
            )
        );

        int edadMax = Integer.parseInt(
            leer(
                sc,
                "Edad maxima: "
            )
        );

        int cupo = Integer.parseInt(
            leer(
                sc,
                "Cupo maximo: "
            )
        );

        // VALIDAR DATOS

        if (nombre.isBlank() ||
            deporte.isBlank() ||
            categoria.isBlank() ||
            minimo < 1 ||
            edadMin < 0 ||
            edadMax < edadMin ||
            cupo < minimo) {

            throw new IllegalArgumentException(
                "Datos del equipo invalidos"
            );
        }

        // EVITAR NOMBRES DUPLICADOS

        for (Equipo existente :
                sistema.getClub().getEquipos()) {

            if (existente.getNombre()
                    .equalsIgnoreCase(nombre)) {

                throw new IllegalArgumentException(
                    "Ya existe un equipo con ese nombre"
                );
            }
        }

        // CREAR EQUIPO

        sistema.getClub().registrarEquipo(
            new Equipo(
                nombre,
                new Deporte(
                    deporte,
                    minimo
                ),
                new Categoria(
                    categoria,
                    edadMin,
                    edadMax
                ),
                cupo
            )
        );

        // GUARDAR CAMBIOS

        guardar(
            sistema,
            ruta
        );

        System.out.println(
            "Equipo registrado y guardado."
        );
    }

    // MENU DE USUARIO AUTENTICADO

    private static void sesion(
            Scanner sc,
            SistemaClub sistema,
            String token,
            Path ruta) {

        while (true) {

            try {

                Rol rol =
                    sistema.getUsuarios()
                        .rolAutenticado(token);

                System.out.println(
                    "\n--- MENU (" + rol + ") ---"
                );

                System.out.println(
                    "1. Ver resumen del club"
                );

                System.out.println(
                    "2. Ver mi cuenta"
                );

                if (rol == Rol.ADMINISTRADOR) {

                    System.out.println(
                        "3. Listar usuarios"
                    );

                    System.out.println(
                        "4. Registrar usuario"
                    );

                    System.out.println(
                        "5. Registrar equipo"
                    );

                    System.out.println(
                        "6. Guardar ahora"
                    );
                }

                System.out.println(
                    "0. Cerrar sesion"
                );

                String opcion = leer(
                    sc,
                    "Opcion: "
                );

                switch (opcion) {

                    case "0":

                        sistema.getUsuarios()
                            .cerrarSesion(token);

                        return;

                    case "1":

                        resumen(sistema);

                        break;

                    case "2":

                        System.out.println(
                            "Usuario: "
                            + sistema.getUsuarios()
                                .usuarioAutenticado(token)
                                .getLogin()
                        );

                        System.out.println(
                            "Rol: " + rol
                        );

                        break;

                    case "3":

                        sistema.getUsuarios().exigirRol(
                            token,
                            Rol.ADMINISTRADOR
                        );

                        for (Usuario u :
                                sistema.getUsuarios()
                                    .getUsuarios(token)) {

                            System.out.println(
                                "- " + u.getLogin()
                                + " ("
                                + u.getClass()
                                    .getSimpleName()
                                + ")"
                            );
                        }

                        break;

                    case "4":

                        registrarUsuario(
                            sc,
                            sistema,
                            token,
                            ruta
                        );

                        break;

                    case "5":

                        registrarEquipo(
                            sc,
                            sistema,
                            token,
                            ruta
                        );

                        break;

                    case "6":

                        sistema.getUsuarios().exigirRol(
                            token,
                            Rol.ADMINISTRADOR
                        );

                        guardar(
                            sistema,
                            ruta
                        );

                        System.out.println(
                            "Datos guardados."
                        );

                        break;

                    default:

                        System.out.println(
                            "Opcion no reconocida."
                        );
                }

            } catch (ReglaNegocioException |
                     IllegalArgumentException e) {

                System.out.println(
                    "Operacion rechazada: "
                    + e.getMessage()
                );

            } catch (IOException e) {

                System.err.println(
                    "ERROR: los cambios no se pudieron "
                    + "guardar: "
                    + e.getMessage()
                );
            }
        }
    }

    // PUNTO DE ENTRADA REAL DE LA APLICACION

    public static void main(String[] args) {

        // PERMITE UNA RUTA DIFERENTE EN PRUEBAS

        Path ruta = args.length > 0
            ? Path.of(args[0])
            : ARCHIVO;

        boolean primeraVez =
            !Files.exists(ruta);

        try (Scanner sc =
                new Scanner(System.in)) {

            // RECUPERAR EL SISTEMA
            // O CREARLO SOLO SI NO EXISTE ARCHIVO

            SistemaClub sistema =
                cargarOCrear(ruta);

            if (primeraVez) {

                crearAdministradorInicial(
                    sc,
                    sistema,
                    ruta
                );

            } else {

                System.out.println(
                    "Sistema recuperado desde: "
                    + ruta
                );
            }

            // MENU PRINCIPAL

            while (true) {

                System.out.println(
                    "\n=== CLUB DEPORTIVO SENECA ==="
                );

                System.out.println(
                    "1. Iniciar sesion"
                );

                System.out.println(
                    "0. Guardar y salir"
                );

                String opcion = leer(
                    sc,
                    "Opcion: "
                );

                if (opcion.equals("0")) {

                    guardar(
                        sistema,
                        ruta
                    );

                    System.out.println(
                        "Sistema guardado. Hasta pronto."
                    );

                    return;
                }

                if (!opcion.equals("1")) {

                    System.out.println(
                        "Opcion no reconocida."
                    );

                    continue;
                }

                // SOLICITAR CREDENCIALES

                String login = leer(
                    sc,
                    "Login: "
                );

                String clave = leerClave(
                    sc,
                    "Contrasena: "
                );

                try {

                    String token =
                        sistema.getUsuarios()
                            .iniciarSesion(
                                login,
                                clave
                            );

                    sesion(
                        sc,
                        sistema,
                        token,
                        ruta
                    );

                } catch (ReglaNegocioException e) {

                    System.out.println(
                        "No fue posible iniciar sesion: "
                        + e.getMessage()
                    );
                }
            }

        } catch (IOException |
                 ClassNotFoundException e) {

            // IMPORTANTE:
            // NO REEMPLAZAR UN ARCHIVO DANADO
            // CON UN SISTEMA VACIO

            System.err.println(
                "No se pudo abrir o guardar el sistema: "
                + e.getMessage()
            );

            System.err.println(
                "El archivo anterior NO fue reemplazado "
                + "por un sistema vacio."
            );

        } catch (RuntimeException e) {

            System.err.println(
                "Error al ejecutar la aplicacion: "
                + e.getMessage()
            );
        }
    }
}
