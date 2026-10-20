
package uniandes.dpoo.cds.consola;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;

import uniandes.dpoo.cds.aplicacion.GestorTurnos;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.administracion.SolicitudCambioTurno;
import uniandes.dpoo.cds.modelo.administracion.TurnoPersonal;
import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Fisioterapeuta;

public class PruebaTurnos {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    // PRUEBA QUE DEBE SER ACEPTADA

    private static void aceptar(
            String titulo,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + titulo);
            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + titulo + " -> " + e
            );

            fallidas++;
        }
    }

    // PRUEBA QUE DEBE SER RECHAZADA

    private static void rechazar(
            String titulo,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + titulo + " (se acepto)"
            );

            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + titulo + " -> " + e.getMessage()
            );

            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + titulo + " -> " + e
            );

            fallidas++;
        }
    }

    // VERIFICAR RESULTADO

    private static void verificar(
            String titulo,
            boolean cumple) {

        System.out.println(
            (cumple ? "[OK] " : "[FALLO] ")
            + titulo
        );

        if (cumple) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // DIAS Y HORARIOS DE EJEMPLO

        DayOfWeek lunes = DayOfWeek.MONDAY;
        DayOfWeek martes = DayOfWeek.TUESDAY;

        LocalTime ocho =
            LocalTime.of(8, 0);

        LocalTime nueve =
            LocalTime.of(9, 0);

        LocalTime diez =
            LocalTime.of(10, 0);

        LocalTime dieciocho =
            LocalTime.of(18, 0);

        // CREAR GESTOR

        GestorTurnos gestor = new GestorTurnos(
            ocho,
            dieciocho,
            EnumSet.of(lunes, martes)
        );

        // CREAR ENTRENADORES

        Entrenador e1 = new Entrenador(
            "ent01", "demo", "DESC1"
        );

        Entrenador e2 = new Entrenador(
            "ent02", "demo", "DESC2"
        );

        Entrenador e3 = new Entrenador(
            "ent03", "demo", "DESC3"
        );

        // CREAR FISIOTERAPEUTAS

        Fisioterapeuta f1 = new Fisioterapeuta(
            "fis01", "demo", "FIS1"
        );

        Fisioterapeuta f2 = new Fisioterapeuta(
            "fis02", "demo", "FIS2"
        );

        // CREAR ADMINISTRADOR

        Administrador admin = new Administrador(
            "admin01", "demo"
        );

        // CREAR TURNOS DEL LUNES

        TurnoPersonal e1L = new TurnoPersonal(
            e1, lunes, ocho, dieciocho
        );

        TurnoPersonal e2L = new TurnoPersonal(
            e2, lunes, ocho, dieciocho
        );

        TurnoPersonal f1L = new TurnoPersonal(
            f1, lunes, ocho, dieciocho
        );

        // CREAR TURNOS DEL MARTES

        TurnoPersonal e1M = new TurnoPersonal(
            e1, martes, ocho, dieciocho
        );

        TurnoPersonal e2M = new TurnoPersonal(
            e2, martes, ocho, dieciocho
        );

        TurnoPersonal f1M = new TurnoPersonal(
            f1, martes, ocho, dieciocho
        );

        System.out.println(
            "PRUEBAS DE TURNOS Y CAMBIOS DE PERSONAL"
        );

        System.out.println(
            "--------------------------------------------"
        );

        // VALIDAR REGISTROS

        rechazar(
            "No registrar turno nulo",
            () -> gestor.registrarTurno(null)
        );

        aceptar(
            "Registrar entrenador 1 lunes",
            () -> gestor.registrarTurno(e1L)
        );

        aceptar(
            "Registrar entrenador 2 lunes",
            () -> gestor.registrarTurno(e2L)
        );

        aceptar(
            "Registrar fisioterapeuta lunes",
            () -> gestor.registrarTurno(f1L)
        );

        // TODAVIA FALTA EL MARTES

        rechazar(
            "No activar si martes no tiene cobertura",
            () -> gestor.activarProgramacion()
        );

        aceptar(
            "Registrar entrenador 1 martes",
            () -> gestor.registrarTurno(e1M)
        );

        aceptar(
            "Registrar entrenador 2 martes",
            () -> gestor.registrarTurno(e2M)
        );

        aceptar(
            "Registrar fisioterapeuta martes",
            () -> gestor.registrarTurno(f1M)
        );

        // ACTIVAR PROGRAMACION

        aceptar(
            "Activar semana con cobertura completa",
            () -> gestor.activarProgramacion()
        );

        verificar(
            "Cobertura de lunes y martes confirmada",
            gestor.isProgramacionActiva() &&
            gestor.tieneCoberturaMinima()
        );

        // CONFLICTOS DE HORARIO

        rechazar(
            "No duplicar turno de un empleado",
            () -> gestor.registrarTurno(
                new TurnoPersonal(
                    e1, lunes, nueve, diez
                )
            )
        );

        rechazar(
            "No asignar turnos fuera de horario",
            () -> gestor.registrarTurno(
                new TurnoPersonal(
                    e3,
                    lunes,
                    LocalTime.of(7, 30),
                    diez
                )
            )
        );

        // VALIDAR TITULAR DE LA SOLICITUD

        rechazar(
            "Otro empleado no solicita cambio ajeno",
            () -> gestor.solicitarCambio(
                e2,
                e1L,
                lunes,
                diez,
                dieciocho,
                "Cita"
            )
        );

        // SOLICITUD SIN REEMPLAZO

        SolicitudCambioTurno[] primera =
            new SolicitudCambioTurno[1];

        aceptar(
            "Solicitar entrar a las diez el lunes",
            () -> primera[0] = gestor.solicitarCambio(
                e1,
                e1L,
                lunes,
                diez,
                dieciocho,
                "Cita personal"
            )
        );

        rechazar(
            "No duplicar solicitud pendiente",
            () -> gestor.solicitarCambio(
                e1,
                e1L,
                lunes,
                nueve,
                dieciocho,
                "Repetida"
            )
        );

        rechazar(
            "No aprobar sin administrador",
            () -> gestor.aprobarCambio(
                null,
                primera[0].getNumero()
            )
        );

        // NO SE PUEDE APROBAR SIN COBERTURA

        rechazar(
            "Cambio no puede dejar un solo entrenador",
            () -> gestor.aprobarCambio(
                admin,
                primera[0].getNumero()
            )
        );

        verificar(
            "Cambio fallido conserva turno y solicitud pendiente",

            gestor.getTurnos().contains(e1L) &&

            primera[0].getEstado() ==
                SolicitudCambioTurno.Estado.PENDIENTE
        );

        // RECHAZAR FORMALMENTE

        rechazar(
            "Rechazo exige motivo",
            () -> gestor.rechazarCambio(
                admin,
                primera[0].getNumero(),
                " "
            )
        );

        aceptar(
            "Administrador rechaza la solicitud",
            () -> gestor.rechazarCambio(
                admin,
                primera[0].getNumero(),
                "No hay reemplazo suficiente"
            )
        );

        verificar(
            "Rechazo conserva el turno original",

            gestor.getTurnos().contains(e1L) &&

            primera[0].getEstado() ==
                SolicitudCambioTurno.Estado.RECHAZADA
        );

        rechazar(
            "No aprobar solicitud ya rechazada",
            () -> gestor.aprobarCambio(
                admin,
                primera[0].getNumero()
            )
        );

        // INCORPORAR ENTRENADOR DE APOYO

        aceptar(
            "Agregar tercer entrenador el lunes",
            () -> gestor.registrarTurno(
                new TurnoPersonal(
                    e3,
                    lunes,
                    ocho,
                    dieciocho
                )
            )
        );

        // NUEVA SOLICITUD

        SolicitudCambioTurno[] segunda =
            new SolicitudCambioTurno[1];

        aceptar(
            "Solicitar de nuevo cambio de entrenador",
            () -> segunda[0] = gestor.solicitarCambio(
                e1,
                e1L,
                lunes,
                diez,
                dieciocho,
                "Con reemplazo"
            )
        );

        // AHORA SI EXISTE COBERTURA SUFICIENTE

        aceptar(
            "Aprobar cambio con cobertura suficiente",
            () -> gestor.aprobarCambio(
                admin,
                segunda[0].getNumero()
            )
        );

        verificar(
            "Cambio aprobado actualiza el turno correcto",

            !gestor.getTurnos().contains(e1L) &&

            gestor.getTurnos().contains(
                segunda[0].getPropuesto()
            ) &&

            segunda[0].getEstado() ==
                SolicitudCambioTurno.Estado.APROBADA
        );

        verificar(
            "Cobertura se conserva en ambos dias",
            gestor.tieneCoberturaMinima()
        );

        // SOLICITUD DE FISIOTERAPEUTA

        SolicitudCambioTurno[] tercera =
            new SolicitudCambioTurno[1];

        aceptar(
            "Fisioterapeuta solicita cambio lunes",
            () -> tercera[0] = gestor.solicitarCambio(
                f1,
                f1L,
                lunes,
                nueve,
                dieciocho,
                "Tramite"
            )
        );

        // NO SE PERMITE DEJAR EL CLUB SIN FISIOTERAPEUTA

        rechazar(
            "No dejar club sin fisioterapeuta",
            () -> gestor.aprobarCambio(
                admin,
                tercera[0].getNumero()
            )
        );

        // AGREGAR FISIOTERAPEUTA DE APOYO

        aceptar(
            "Registrar fisioterapeuta de apoyo",
            () -> gestor.registrarTurno(
                new TurnoPersonal(
                    f2,
                    lunes,
                    ocho,
                    dieciocho
                )
            )
        );

        // APROBAR CAMBIO

        aceptar(
            "Aprobar cambio del fisioterapeuta",
            () -> gestor.aprobarCambio(
                admin,
                tercera[0].getNumero()
            )
        );

        // VERIFICACIONES FINALES

        verificar(
            "Programacion final valida y solicitudes registradas",

            gestor.tieneCoberturaMinima() &&

            gestor.getTurnos().size() == 8 &&

            gestor.getSolicitudes().size() == 3 &&

            gestor.getTurnosDia(lunes).size() == 5
        );

        // RESULTADOS

        System.out.println(
            "--------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Turnos vigentes: "
            + gestor.getTurnos().size()
        );

        System.out.println(
            "Solicitudes registradas: "
            + gestor.getSolicitudes().size()
        );
    }
}
