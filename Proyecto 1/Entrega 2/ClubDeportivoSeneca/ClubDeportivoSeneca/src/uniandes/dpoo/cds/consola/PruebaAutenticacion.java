
package uniandes.dpoo.cds.consola;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDate;

import uniandes.dpoo.cds.aplicacion.GestorUsuarios;
import uniandes.dpoo.cds.aplicacion.GestorUsuarios.Rol;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Fisioterapeuta;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaAutenticacion {

    private static int correctas;
    private static int fallidas;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // COMPROBAR RESULTADOS

    private static void comprobar(
            String texto,
            boolean bien) {

        System.out.println(
            (bien ? "[OK] " : "[FALLO] ")
            + texto
        );

        if (bien) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // COMPROBAR QUE UNA OPERACION SE RECHAZA

    private static void rechazar(
            String texto,
            Accion accion) {

        try {

            accion.ejecutar();

            comprobar(texto, false);

        } catch (ReglaNegocioException esperado) {

            comprobar(texto, true);

        } catch (Exception error) {

            System.out.println(
                "Error inesperado: "
                + error
            );

            comprobar(texto, false);
        }
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // CREAR GESTOR DE USUARIOS

        GestorUsuarios gestor =
            new GestorUsuarios();

        // REGISTRAR ADMINISTRADOR INICIAL

        Administrador admin =
            new Administrador(
                "AdminSeneca",
                "ClaveAdmin2026!"
            );

        gestor.registrarAdministradorInicial(
            admin
        );

        System.out.println(
            "PRUEBAS DE AUTENTICACION Y ROLES"
        );

        System.out.println(
            "--------------------------------------"
        );

        // COMPROBAR ADMINISTRADOR INICIAL

        comprobar(
            "Administrador inicial registrado",
            admin.getContrasena() == null
        );

        rechazar(
            "No permitir segundo administrador inicial",

            () -> gestor.registrarAdministradorInicial(
                new Administrador(
                    "segundo",
                    "ClaveSegura123"
                )
            )
        );

        // RECHAZAR CONTRASENA INCORRECTA

        rechazar(
            "Rechazar contrasena incorrecta",

            () -> gestor.iniciarSesion(
                "AdminSeneca",
                "erronea"
            )
        );

        // INICIAR SESION COMO ADMINISTRADOR

        String sesionAdmin =
            gestor.iniciarSesion(
                "adminseneca",
                "ClaveAdmin2026!"
            );

        comprobar(
            "Administrador inicia sesion sin importar mayusculas",

            gestor.usuarioAutenticado(
                sesionAdmin
            ) == admin
        );

        comprobar(
            "Administrador tiene rol ADMINISTRADOR",

            gestor.rolAutenticado(
                sesionAdmin
            ) == Rol.ADMINISTRADOR
        );

        // CREAR SOCIO

        Socio ana = new Socio(
            "AnaSeneca",
            "ClaveAna2026!",
            LocalDate.of(2000, 1, 1)
        );

        gestor.registrarUsuario(
            sesionAdmin,
            ana
        );

        // REGISTRAR EMPLEADO

        gestor.registrarUsuario(
            sesionAdmin,

            new Empleado(
                "empleado1",
                "ClaveEmpl2026!",
                "DES1"
            )
        );

        // REGISTRAR ENTRENADOR

        gestor.registrarUsuario(
            sesionAdmin,

            new Entrenador(
                "profe1",
                "ClaveProfe2026!",
                "DES2"
            )
        );

        // REGISTRAR FISIOTERAPEUTA

        gestor.registrarUsuario(
            sesionAdmin,

            new Fisioterapeuta(
                "fisio1",
                "ClaveFisio2026!",
                "DES3"
            )
        );

        comprobar(
            "Cinco usuarios registrados",

            gestor.getUsuarios(
                sesionAdmin
            ).size() == 5
        );

        comprobar(
            "La contrasena del socio no queda en texto plano",

            ana.getContrasena() == null
        );

        // EVITAR LOGIN DUPLICADO

        rechazar(
            "No permitir login duplicado",

            () -> gestor.registrarUsuario(
                sesionAdmin,

                new Socio(
                    "ANASENECA",
                    "OtraClave2026!",
                    LocalDate.of(2001, 1, 1)
                )
            )
        );

        // EVITAR CONTRASENAS CORTAS

        rechazar(
            "No permitir contrasenas demasiado cortas",

            () -> gestor.registrarUsuario(
                sesionAdmin,

                new Socio(
                    "sinClave",
                    "123",
                    LocalDate.of(2001, 1, 1)
                )
            )
        );

        // INICIAR SESION COMO SOCIO

        String sesionAna =
            gestor.iniciarSesion(
                "anaseneca",
                "ClaveAna2026!"
            );

        comprobar(
            "Socio inicia sesion",

            gestor.rolAutenticado(
                sesionAna
            ) == Rol.SOCIO
        );

        // SOCIO NO PUEDE REGISTRAR USUARIOS

        rechazar(
            "Socio no registra usuarios",

            () -> gestor.registrarUsuario(
                sesionAna,

                new Empleado(
                    "otro",
                    "ClaveNuevo2026!",
                    "DES4"
                )
            )
        );

        // SOCIO NO PUEDE ASUMIR ROL DE ADMIN

        rechazar(
            "Socio no obtiene permiso de administrador",

            () -> gestor.exigirRol(
                sesionAna,
                Rol.ADMINISTRADOR
            )
        );

        // ENTRAR COMO ENTRENADOR

        String sesionProfe =
            gestor.iniciarSesion(
                "profe1",
                "ClaveProfe2026!"
            );

        comprobar(
            "Entrenador tiene su rol especifico",

            gestor.rolAutenticado(
                sesionProfe
            ) == Rol.ENTRENADOR
        );

        // COMPROBAR PERMISO DEL ENTRENADOR

        gestor.exigirRol(
            sesionProfe,
            Rol.ENTRENADOR,
            Rol.ADMINISTRADOR
        );

        comprobar(
            "Entrenador accede a funcion permitida",
            true
        );

        // RECHAZAR TOKEN INVENTADO

        rechazar(
            "Token inventado no tiene permisos",

            () -> gestor.usuarioAutenticado(
                "TOKEN-INVALIDO"
            )
        );

        // CERRAR SESION

        gestor.cerrarSesion(
            sesionAna
        );

        rechazar(
            "Cerrar sesion invalida el token",

            () -> gestor.usuarioAutenticado(
                sesionAna
            )
        );

        // GUARDAR CUENTAS EN ARCHIVO DE PRUEBA

        Path archivo = Path.of(
            "datos",
            "usuarios-prueba.dat"
        );

        Files.createDirectories(
            archivo.toAbsolutePath().getParent()
        );

        try (ObjectOutputStream salida =
                new ObjectOutputStream(
                    Files.newOutputStream(archivo))) {

            salida.writeObject(
                gestor
            );
        }

        // RECUPERAR CUENTAS

        GestorUsuarios restaurado;

        try (ObjectInputStream entrada =
                new ObjectInputStream(
                    Files.newInputStream(archivo))) {

            restaurado =
                (GestorUsuarios) entrada.readObject();
        }

        comprobar(
            "Se guardan y recuperan las cuentas",

            Files.isRegularFile(
                archivo
            )
        );

        // LAS SESIONES NO DEBEN RESTAURARSE

        rechazar(
            "Las sesiones no sobreviven al reinicio",

            () -> restaurado.usuarioAutenticado(
                sesionAdmin
            )
        );

        // INICIAR NUEVA SESION TRAS RESTAURAR

        String nuevaSesion =
            restaurado.iniciarSesion(
                "adminseneca",
                "ClaveAdmin2026!"
            );

        comprobar(
            "La clave correcta sigue funcionando tras reiniciar",

            restaurado.getUsuarios(
                nuevaSesion
            ).size() == 5
        );

        comprobar(
            "Los roles quedan conservados al reiniciar",

            restaurado.rolAutenticado(
                restaurado.iniciarSesion(
                    "fisio1",
                    "ClaveFisio2026!"
                )
            ) == Rol.FISIOTERAPEUTA
        );

        // RESULTADOS FINALES

        System.out.println(
            "--------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: "
            + correctas
        );

        System.out.println(
            "Pruebas fallidas: "
            + fallidas
        );
    }
}
