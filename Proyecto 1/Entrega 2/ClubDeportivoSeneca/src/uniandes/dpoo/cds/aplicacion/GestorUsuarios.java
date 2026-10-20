
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Fisioterapeuta;
import uniandes.dpoo.cds.modelo.usuarios.Socio;
import uniandes.dpoo.cds.modelo.usuarios.Usuario;

public class GestorUsuarios implements Serializable {

    private static final long serialVersionUID = 1L;

    // PARAMETROS DE SEGURIDAD

    private static final int ITERACIONES = 120000;
    private static final int LONGITUD_CLAVE = 256;
    private static final int LONGITUD_SAL = 16;

    // TIPOS DE USUARIO

    public enum Rol {
        ADMINISTRADOR,
        EMPLEADO,
        ENTRENADOR,
        FISIOTERAPEUTA,
        SOCIO
    }

    // CUENTA DE UN USUARIO REGISTRADO

    private static class Cuenta implements Serializable {

        private static final long serialVersionUID = 1L;

        private final Usuario usuario;
        private final Rol rol;
        private final byte[] sal;
        private final byte[] clave;

        private Cuenta(
                Usuario usuario,
                Rol rol,
                byte[] sal,
                byte[] clave) {

            this.usuario = usuario;
            this.rol = rol;
            this.sal = sal.clone();
            this.clave = clave.clone();
        }
    }

    // USUARIOS REGISTRADOS

    private final Map<String, Cuenta> cuentas =
        new LinkedHashMap<>();

    // SESIONES ACTIVAS

    // TRANSIENT: NO GUARDAR SESIONES EN DISCO

    private transient Map<String, String> sesiones;

    private Map<String, String> sesiones() {

        if (sesiones == null) {
            sesiones = new HashMap<>();
        }

        return sesiones;
    }

    // NORMALIZAR LOGIN

    private static String claveLogin(String login) {

        return login == null
            ? ""
            : login.trim().toLowerCase(Locale.ROOT);
    }

    // IDENTIFICAR ROL SEGUN TIPO DE USUARIO

    private static Rol tipo(Usuario usuario)
            throws ReglaNegocioException {

        if (usuario instanceof Administrador) {
            return Rol.ADMINISTRADOR;
        }

        if (usuario instanceof Entrenador) {
            return Rol.ENTRENADOR;
        }

        if (usuario instanceof Fisioterapeuta) {
            return Rol.FISIOTERAPEUTA;
        }

        if (usuario instanceof Empleado) {
            return Rol.EMPLEADO;
        }

        if (usuario instanceof Socio) {
            return Rol.SOCIO;
        }

        throw new ReglaNegocioException(
            "Tipo de usuario no permitido"
        );
    }

    // PROTEGER CONTRASENA CON PBKDF2

    private static byte[] derivar(
            String contrasena,
            byte[] sal)
            throws ReglaNegocioException {

        char[] caracteres =
            contrasena.toCharArray();

        KeySpec especificacion = new PBEKeySpec(
            caracteres,
            sal,
            ITERACIONES,
            LONGITUD_CLAVE
        );

        try {

            SecretKeyFactory algoritmo =
                SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256"
                );

            return algoritmo.generateSecret(
                especificacion
            ).getEncoded();

        } catch (Exception e) {

            throw new ReglaNegocioException(
                "No se pudo procesar la contrasena"
            );

        } finally {

            java.util.Arrays.fill(
                caracteres,
                '\0'
            );

            ((PBEKeySpec) especificacion)
                .clearPassword();
        }
    }

    // AGREGAR USUARIO AL REGISTRO

    private void agregar(Usuario usuario)
            throws ReglaNegocioException {

        if (usuario == null) {

            throw new ReglaNegocioException(
                "El usuario es obligatorio"
            );
        }

        String login =
            claveLogin(usuario.getLogin());

        String contrasena =
            usuario.getContrasena();

        // VALIDAR DATOS

        if (login.isEmpty() ||
            contrasena == null ||
            contrasena.length() < 8) {

            throw new ReglaNegocioException(
                "El usuario necesita login y contrasena "
                + "de al menos 8 caracteres"
            );
        }

        // EVITAR DUPLICADOS

        if (cuentas.containsKey(login)) {

            throw new ReglaNegocioException(
                "El login ya esta registrado"
            );
        }

        // IDENTIFICAR ROL

        Rol rol = tipo(usuario);

        // CREAR SAL ALEATORIA

        byte[] sal =
            new byte[LONGITUD_SAL];

        new SecureRandom().nextBytes(sal);

        // CALCULAR CLAVE PROTEGIDA

        byte[] clave = derivar(
            contrasena,
            sal
        );

        // EVITAR CONSERVAR CONTRASENA PLANA
        // EN EL OBJETO USUARIO REGISTRADO

        usuario.setContrasena(null);

        cuentas.put(
            login,
            new Cuenta(
                usuario,
                rol,
                sal,
                clave
            )
        );
    }

    // REGISTRAR PRIMER ADMINISTRADOR

    public void registrarAdministradorInicial(
            Administrador administrador)
            throws ReglaNegocioException {

        if (!cuentas.isEmpty()) {

            throw new ReglaNegocioException(
                "El administrador inicial ya existe"
            );
        }

        agregar(administrador);
    }

    // REGISTRAR OTROS USUARIOS
    // SOLO LO HACE UN ADMINISTRADOR AUTENTICADO

    public void registrarUsuario(
            String sesionAdministrador,
            Usuario nuevo)
            throws ReglaNegocioException {

        exigirRol(
            sesionAdministrador,
            Rol.ADMINISTRADOR
        );

        agregar(nuevo);
    }

    // INICIAR SESION

    public String iniciarSesion(
            String login,
            String contrasena)
            throws ReglaNegocioException {

        Cuenta cuenta =
            cuentas.get(claveLogin(login));

        // VALIDAR CREDENCIALES

        if (cuenta == null ||
            contrasena == null ||
            !MessageDigest.isEqual(
                cuenta.clave,
                derivar(contrasena, cuenta.sal)
            )) {

            throw new ReglaNegocioException(
                "Credenciales incorrectas"
            );
        }

        // CREAR TOKEN DE SESION

        String token =
            UUID.randomUUID().toString();

        sesiones().put(
            token,
            claveLogin(login)
        );

        return token;
    }

    // CERRAR SESION

    public void cerrarSesion(String token)
            throws ReglaNegocioException {

        if (token == null ||
            sesiones().remove(token) == null) {

            throw new ReglaNegocioException(
                "Sesion inexistente o vencida"
            );
        }
    }

    // CONSULTAR USUARIO AUTENTICADO

    public Usuario usuarioAutenticado(String token)
            throws ReglaNegocioException {

        String login = token == null
            ? null
            : sesiones().get(token);

        Cuenta cuenta = login == null
            ? null
            : cuentas.get(login);

        if (cuenta == null) {

            throw new ReglaNegocioException(
                "Debe iniciar sesion"
            );
        }

        return cuenta.usuario;
    }

    // CONSULTAR ROL AUTENTICADO

    public Rol rolAutenticado(String token)
            throws ReglaNegocioException {

        String login = usuarioAutenticado(
            token
        ).getLogin();

        return cuentas.get(
            claveLogin(login)
        ).rol;
    }

    // VALIDAR PERMISOS

    public void exigirRol(
            String token,
            Rol... permitidos)
            throws ReglaNegocioException {

        Rol actual =
            rolAutenticado(token);

        if (permitidos == null) {

            throw new ReglaNegocioException(
                "Acceso no autorizado"
            );
        }

        for (Rol rol : permitidos) {

            if (actual == rol) {
                return;
            }
        }

        throw new ReglaNegocioException(
            "Acceso no autorizado"
        );
    }

    // LISTAR USUARIOS
    // REQUIERE PERMISO DE ADMINISTRADOR

    public List<Usuario> getUsuarios(
            String sesionAdministrador)
            throws ReglaNegocioException {

        exigirRol(
            sesionAdministrador,
            Rol.ADMINISTRADOR
        );

        List<Usuario> resultado =
            new ArrayList<>();

        for (Cuenta cuenta : cuentas.values()) {

            resultado.add(
                cuenta.usuario
            );
        }

        return Collections.unmodifiableList(
            resultado
        );
    }
}
