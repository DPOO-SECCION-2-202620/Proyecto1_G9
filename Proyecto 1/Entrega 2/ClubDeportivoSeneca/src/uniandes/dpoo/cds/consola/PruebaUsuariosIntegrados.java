
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.GestorUsuarios.Rol;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Articulo;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Fisioterapeuta;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class PruebaUsuariosIntegrados {

    private static int correctas;
    private static int fallidas;

    @FunctionalInterface
    private interface Accion {

        void ejecutar() throws Exception;
    }

    // COMPROBAR RESULTADOS

    private static void verificar(
            String descripcion,
            boolean cumple) {

        System.out.println(
            (cumple ? "[OK] " : "[FALLO] ")
            + descripcion
        );

        if (cumple) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // COMPROBAR OPERACIONES RECHAZADAS

    private static void rechazar(
            String descripcion,
            Accion accion) {

        try {

            accion.ejecutar();

            verificar(
                descripcion,
                false
            );

        } catch (ReglaNegocioException esperado) {

            verificar(
                descripcion,
                true
            );

        } catch (Exception inesperado) {

            System.out.println(
                "Error inesperado: "
                + inesperado
            );

            verificar(
                descripcion,
                false
            );
        }
    }

    // CREAR UN SISTEMA NUEVO

    private static SistemaClub nuevo() {

        return new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    // RECUPERAR SISTEMA DESDE DISCO

    private static SistemaClub cargar(Path ruta)
            throws Exception {

        return PersistenciaSistema.cargarSistema(
            ruta,
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        Path ruta = Path.of(
            "datos",
            "sistema-usuarios-prueba.dat"
        );

        SistemaClub inicial = nuevo();

        // CREAR ADMINISTRADOR

        Administrador admin =
            new Administrador(
                "adminIntegrado",
                "ClaveAdmin2026!"
            );

        // CREAR SOCIO

        Socio ana = new Socio(
            "anaIntegrada",
            "ClaveAna2026!",
            LocalDate.of(2000, 1, 1)
        );

        System.out.println(
            "PRUEBAS DE USUARIOS INTEGRADOS Y PERSISTENCIA"
        );

        System.out.println(
            "----------------------------------------------"
        );

        // COMPROBAR NUEVO GESTOR

        verificar(
            "SistemaClub contiene un gestor de usuarios",

            inicial.getUsuarios() != null
        );

        // REGISTRAR ADMINISTRADOR

        inicial.getUsuarios()
            .registrarAdministradorInicial(admin);

        // INICIAR SESION COMO ADMINISTRADOR

        String sesionAdmin =
            inicial.getUsuarios().iniciarSesion(
                "ADMININTEGRADO",
                "ClaveAdmin2026!"
            );

        verificar(
            "Administrador autenticado en el sistema central",

            inicial.getUsuarios()
                .rolAutenticado(sesionAdmin)
                == Rol.ADMINISTRADOR
        );

        // REGISTRAR SOCIO

        inicial.getUsuarios().registrarUsuario(
            sesionAdmin,
            ana
        );

        // REGISTRAR EMPLEADO

        inicial.getUsuarios().registrarUsuario(
            sesionAdmin,

            new Empleado(
                "empleadoIntegrado",
                "ClaveEmpleado2026!",
                "EMP-I"
            )
        );

        // REGISTRAR ENTRENADOR

        inicial.getUsuarios().registrarUsuario(
            sesionAdmin,

            new Entrenador(
                "entrenadorIntegrado",
                "ClaveEntrenador2026!",
                "ENT-I"
            )
        );

        // REGISTRAR FISIOTERAPEUTA

        inicial.getUsuarios().registrarUsuario(
            sesionAdmin,

            new Fisioterapeuta(
                "fisioIntegrado",
                "ClaveFisio2026!",
                "FIS-I"
            )
        );

        verificar(
            "Cinco cuentas registradas",

            inicial.getUsuarios()
                .getUsuarios(sesionAdmin)
                .size() == 5
        );

        // COMPROBAR CONTRASENAS

        verificar(
            "Las contrasenas no quedan en los objetos registrados",

            admin.getContrasena() == null &&

            ana.getContrasena() == null
        );

        // INICIAR SESION COMO SOCIO

        String sesionAna =
            inicial.getUsuarios().iniciarSesion(
                "anaIntegrada",
                "ClaveAna2026!"
            );

        verificar(
            "El socio tiene rol SOCIO",

            inicial.getUsuarios()
                .rolAutenticado(sesionAna)
                == Rol.SOCIO
        );

        // COMPROBAR QUE NO PUEDE REGISTRAR CUENTAS

        rechazar(
            "Socio no puede registrar cuentas",

            () -> inicial.getUsuarios()
                .registrarUsuario(
                    sesionAna,

                    new Socio(
                        "intruso",
                        "ClaveIntruso2026!",
                        LocalDate.of(2000, 1, 1)
                    )
                )
        );

        // AGREGAR DATOS DE OTROS MODULOS

        Equipo equipo = new Equipo(
            "Equipo persistente",
            new Deporte("Futbol", 7),
            new Categoria("Mayores", 18, 99),
            15
        );

        inicial.getClub()
            .registrarEquipo(equipo);

        inicial.getTienda()
            .registrarArticulo(
                new Articulo(
                    "BAL-US",
                    "Balon",
                    "Deportes",
                    10000
                ),
                2
            );

        inicial.getTienda().vender(
            ana,
            Map.of("BAL-US", 1),
            null,
            0
        );

        verificar(
            "Equipo, venta y puntos existen antes de guardar",

            inicial.getClub()
                .getEquipos().size() == 1 &&

            inicial.getTienda()
                .getVentas().size() == 1 &&

            Math.abs(
                inicial.getFidelidad().getSaldo(ana) - 238
            ) < 0.001
        );

        // GUARDAR SISTEMA COMPLETO

        PersistenciaSistema.guardar(
            inicial,
            ruta
        );

        verificar(
            "Archivo general de usuarios creado",

            Files.isRegularFile(ruta)
        );

        // PRIMER REINICIO

        SistemaClub restaurado = cargar(ruta);

        verificar(
            "SistemaClub recupera el gestor con sus cinco usuarios",

            restaurado.getUsuarios() != null &&

            restaurado.getUsuarios().iniciarSesion(
                "adminIntegrado",
                "ClaveAdmin2026!"
            ) != null
        );

        // COMPROBAR QUE NO SE GUARDARON SESIONES

        rechazar(
            "La sesion antigua del socio se invalida al reiniciar",

            () -> restaurado.getUsuarios()
                .usuarioAutenticado(sesionAna)
        );

        rechazar(
            "La sesion antigua del administrador se invalida",

            () -> restaurado.getUsuarios()
                .usuarioAutenticado(sesionAdmin)
        );

        // NUEVO INICIO DE SESION

        String nuevoAdmin =
            restaurado.getUsuarios().iniciarSesion(
                "adminIntegrado",
                "ClaveAdmin2026!"
            );

        verificar(
            "Credenciales del administrador siguen siendo validas",

            restaurado.getUsuarios()
                .rolAutenticado(nuevoAdmin)
                == Rol.ADMINISTRADOR
        );

        verificar(
            "Los cinco usuarios y sus roles se recuperan",

            restaurado.getUsuarios()
                .getUsuarios(nuevoAdmin).size() == 5 &&

            restaurado.getUsuarios()
                .rolAutenticado(
                    restaurado.getUsuarios()
                        .iniciarSesion(
                            "fisioIntegrado",
                            "ClaveFisio2026!"
                        )
                ) == Rol.FISIOTERAPEUTA
        );

        // COMPROBAR CONTRASENA INCORRECTA

        rechazar(
            "No acepta contrasena incorrecta despues de cargar",

            () -> restaurado.getUsuarios()
                .iniciarSesion(
                    "anaIntegrada",
                    "claveMala"
                )
        );

        // COMPROBAR PERMISOS DEL SOCIO

        rechazar(
            "El socio sigue sin poder listar cuentas",

            () -> restaurado.getUsuarios()
                .getUsuarios(
                    restaurado.getUsuarios()
                        .iniciarSesion(
                            "anaIntegrada",
                            "ClaveAna2026!"
                        )
                )
        );

        // COMPROBAR QUE TAMBIEN SE RECUPERAN LOS DEMAS MODULOS

        verificar(
            "El equipo y las ventas tambien se recuperan",

            restaurado.getClub()
                .getEquipos().size() == 1 &&

            restaurado.getTienda()
                .getVentas().size() == 1 &&

            restaurado.getTienda()
                .getExistencias("BAL-US") == 1
        );

        verificar(
            "Los puntos se conservan en tienda y cafeteria",

            Math.abs(
                restaurado.getTienda()
                    .getSaldoPuntos(ana) - 238
            ) < 0.001 &&

            Math.abs(
                restaurado.getCafeteria()
                    .getSaldoPuntos(ana) - 238
            ) < 0.001
        );

        // REGISTRAR UN USUARIO NUEVO TRAS RECUPERAR

        restaurado.getUsuarios().registrarUsuario(
            nuevoAdmin,

            new Socio(
                "nuevoIntegrado",
                "ClaveNuevo2026!",
                LocalDate.of(2002, 1, 1)
            )
        );

        verificar(
            "Se puede registrar otro usuario despues de cargar",

            restaurado.getUsuarios()
                .getUsuarios(nuevoAdmin)
                .size() == 6
        );

        // SEGUNDO GUARDADO

        PersistenciaSistema.guardar(
            restaurado,
            ruta
        );

        // SEGUNDO REINICIO

        SistemaClub tercera = cargar(ruta);

        String ultimaSesion =
            tercera.getUsuarios().iniciarSesion(
                "adminIntegrado",
                "ClaveAdmin2026!"
            );

        verificar(
            "Segundo reinicio conserva las seis cuentas",

            tercera.getUsuarios()
                .getUsuarios(ultimaSesion)
                .size() == 6
        );

        verificar(
            "Nuevo socio puede iniciar sesion tras segundo reinicio",

            tercera.getUsuarios()
                .rolAutenticado(
                    tercera.getUsuarios()
                        .iniciarSesion(
                            "nuevoIntegrado",
                            "ClaveNuevo2026!"
                        )
                ) == Rol.SOCIO
        );

        rechazar(
            "La sesion del primer reinicio tampoco sobrevive",

            () -> tercera.getUsuarios()
                .usuarioAutenticado(nuevoAdmin)
        );

        // RESULTADOS FINALES

        System.out.println(
            "----------------------------------------------"
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
            "Usuarios recuperados: "
            + tercera.getUsuarios()
                .getUsuarios(ultimaSesion)
                .size()
        );
    }
}
