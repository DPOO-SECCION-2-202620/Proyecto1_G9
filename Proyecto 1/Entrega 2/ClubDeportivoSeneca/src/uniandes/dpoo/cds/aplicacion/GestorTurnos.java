
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.administracion.SolicitudCambioTurno;
import uniandes.dpoo.cds.modelo.administracion.TurnoPersonal;
import uniandes.dpoo.cds.modelo.administracion.TurnoPersonal.Cargo;
import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;

public class GestorTurnos implements Serializable {

    private static final long serialVersionUID = 1L;

    // ATRIBUTOS

    private final LocalTime apertura;
    private final LocalTime cierre;
    private final Set<DayOfWeek> diasAtencion;

    private final List<TurnoPersonal> turnos =
        new ArrayList<>();

    private final List<SolicitudCambioTurno> solicitudes =
        new ArrayList<>();

    private boolean programacionActiva;

    private int siguienteNumero = 1;

    // CONSTRUCTOR

    public GestorTurnos(
            LocalTime apertura,
            LocalTime cierre,
            Set<DayOfWeek> diasAtencion) {

        if (apertura == null ||
            cierre == null ||
            !apertura.isBefore(cierre) ||
            diasAtencion == null ||
            diasAtencion.isEmpty() ||
            diasAtencion.contains(null)) {

            throw new IllegalArgumentException(
                "Horario de atencion invalido"
            );
        }

        this.apertura = apertura;
        this.cierre = cierre;

        this.diasAtencion =
            EnumSet.copyOf(diasAtencion);
    }

    // CONSULTAR CONFIGURACION

    public LocalTime getApertura() {
        return apertura;
    }

    public LocalTime getCierre() {
        return cierre;
    }

    public Set<DayOfWeek> getDiasAtencion() {
        return Collections.unmodifiableSet(diasAtencion);
    }

    // CONSULTAR TURNOS

    public List<TurnoPersonal> getTurnos() {

        return Collections.unmodifiableList(
            turnos
        );
    }

    // CONSULTAR SOLICITUDES

    public List<SolicitudCambioTurno> getSolicitudes() {

        return Collections.unmodifiableList(
            solicitudes
        );
    }

    public boolean isProgramacionActiva() {
        return programacionActiva;
    }

    // CONSULTAR TURNOS DE UN DIA

    public List<TurnoPersonal> getTurnosDia(
            DayOfWeek dia) {

        List<TurnoPersonal> resultado =
            new ArrayList<>();

        for (TurnoPersonal turno : turnos) {

            if (turno.getDia() == dia) {
                resultado.add(turno);
            }
        }

        resultado.sort(
            Comparator.comparing(
                TurnoPersonal::getInicio
            )
        );

        return Collections.unmodifiableList(
            resultado
        );
    }

    // VALIDAR HORARIO

    private void validarHorario(
            TurnoPersonal turno)
            throws ReglaNegocioException {

        if (turno == null ||
            !diasAtencion.contains(turno.getDia()) ||
            turno.getInicio().isBefore(apertura) ||
            turno.getFin().isAfter(cierre)) {

            throw new ReglaNegocioException(
                "Turno fuera del horario de atencion"
            );
        }
    }

    // VERIFICAR CONFLICTOS DE UN MISMO EMPLEADO

    private static boolean conflictoEmpleado(
            TurnoPersonal candidato,
            List<TurnoPersonal> lista) {

        for (TurnoPersonal turno : lista) {

            if (turno.seCruza(candidato) &&
                turno.getEmpleado().getLogin()
                    .equalsIgnoreCase(
                        candidato.getEmpleado().getLogin()
                    )) {

                return true;
            }
        }

        return false;
    }

    // REGISTRAR UN TURNO

    public void registrarTurno(
            TurnoPersonal turno)
            throws ReglaNegocioException {

        validarHorario(turno);

        if (conflictoEmpleado(turno, turnos)) {

            throw new ReglaNegocioException(
                "El empleado ya tiene un turno en ese horario"
            );
        }

        turnos.add(turno);
    }

    // VERIFICAR COBERTURA EN TODOS LOS INTERVALOS

    private boolean coberturaCompleta(
            List<TurnoPersonal> propuesta) {

        for (DayOfWeek dia : diasAtencion) {

            TreeSet<LocalTime> limites =
                new TreeSet<>();

            limites.add(apertura);
            limites.add(cierre);

            // IDENTIFICAR CAMBIOS DE PERSONAL

            for (TurnoPersonal turno : propuesta) {

                if (turno.getDia() == dia) {

                    limites.add(
                        turno.getInicio()
                    );

                    limites.add(
                        turno.getFin()
                    );
                }
            }

            List<LocalTime> tramos =
                new ArrayList<>(limites);

            // REVISAR CADA INTERVALO DE TIEMPO

            for (int i = 0;
                 i < tramos.size() - 1;
                 i++) {

                LocalTime instante =
                    tramos.get(i);

                int entrenadores = 0;
                int fisioterapeutas = 0;

                for (TurnoPersonal turno : propuesta) {

                    if (turno.getDia() == dia &&
                        turno.cubre(instante)) {

                        if (turno.getCargo() ==
                            Cargo.ENTRENADOR) {

                            entrenadores++;

                        } else {

                            fisioterapeutas++;
                        }
                    }
                }

                // REGLA MINIMA DEL CLUB

                if (entrenadores < 2 ||
                    fisioterapeutas < 1) {

                    return false;
                }
            }
        }

        return true;
    }

    // ACTIVAR PROGRAMACION SEMANAL

    public void activarProgramacion()
            throws ReglaNegocioException {

        if (programacionActiva) {

            throw new ReglaNegocioException(
                "La programacion ya esta activa"
            );
        }

        if (!coberturaCompleta(turnos)) {

            throw new ReglaNegocioException(
                "Falta cobertura minima: "
                + "2 entrenadores y 1 fisioterapeuta"
            );
        }

        programacionActiva = true;
    }

    // CONSULTAR COBERTURA

    public boolean tieneCoberturaMinima() {

        return coberturaCompleta(turnos);
    }

    // SOLICITAR CAMBIO DE TURNO

    public SolicitudCambioTurno solicitarCambio(
            Empleado empleado,
            TurnoPersonal original,
            DayOfWeek nuevoDia,
            LocalTime nuevoInicio,
            LocalTime nuevoFin,
            String motivo)
            throws ReglaNegocioException {

        if (!programacionActiva ||
            original == null ||
            !turnos.contains(original)) {

            throw new ReglaNegocioException(
                "Turno no registrado o programacion inactiva"
            );
        }

        // COMPROBAR QUE SOLICITA EL TITULAR

        if (empleado == null ||
            empleado.getLogin() == null ||
            !empleado.getLogin().equalsIgnoreCase(
                original.getEmpleado().getLogin())) {

            throw new ReglaNegocioException(
                "Solo el titular puede solicitar el cambio"
            );
        }

        if (motivo == null ||
            motivo.isBlank() ||
            nuevoDia == null ||
            nuevoInicio == null ||
            nuevoFin == null ||
            !nuevoInicio.isBefore(nuevoFin)) {

            throw new ReglaNegocioException(
                "Datos de solicitud invalidos"
            );
        }

        // EVITAR SOLICITAR EL MISMO HORARIO

        if (nuevoDia == original.getDia() &&
            nuevoInicio.equals(original.getInicio()) &&
            nuevoFin.equals(original.getFin())) {

            throw new ReglaNegocioException(
                "El turno propuesto es igual al actual"
            );
        }

        // EVITAR SOLICITUDES PENDIENTES DUPLICADAS

        for (SolicitudCambioTurno solicitud : solicitudes) {

            if (solicitud.getOriginal() == original &&
                solicitud.getEstado() ==
                    SolicitudCambioTurno.Estado.PENDIENTE) {

                throw new ReglaNegocioException(
                    "Ya existe un cambio pendiente"
                );
            }
        }

        // CREAR TURNO PROPUESTO

        TurnoPersonal propuesto =
            new TurnoPersonal(
                original.getEmpleado(),
                nuevoDia,
                nuevoInicio,
                nuevoFin
            );

        validarHorario(propuesto);

        // CREAR SOLICITUD

        SolicitudCambioTurno solicitud =
            new SolicitudCambioTurno(
                siguienteNumero++,
                original,
                propuesto,
                motivo
            );

        solicitudes.add(solicitud);

        return solicitud;
    }

    // BUSCAR SOLICITUD

    private SolicitudCambioTurno buscar(
            int numero)
            throws ReglaNegocioException {

        for (SolicitudCambioTurno solicitud :
                solicitudes) {

            if (solicitud.getNumero() == numero) {
                return solicitud;
            }
        }

        throw new ReglaNegocioException(
            "Solicitud no encontrada"
        );
    }

    // VALIDAR ADMINISTRADOR

    private static void validarAdministrador(
            Administrador administrador)
            throws ReglaNegocioException {

        if (administrador == null ||
            administrador.getLogin() == null ||
            administrador.getLogin().isBlank()) {

            throw new ReglaNegocioException(
                "Se requiere un administrador"
            );
        }
    }

    // APROBAR CAMBIO DE TURNO

    public void aprobarCambio(
            Administrador administrador,
            int numero)
            throws ReglaNegocioException {

        validarAdministrador(administrador);

        SolicitudCambioTurno solicitud =
            buscar(numero);

        if (solicitud.getEstado() !=
            SolicitudCambioTurno.Estado.PENDIENTE) {

            throw new ReglaNegocioException(
                "La solicitud ya fue resuelta"
            );
        }

        if (!turnos.contains(
                solicitud.getOriginal())) {

            throw new ReglaNegocioException(
                "El turno original ya no esta vigente"
            );
        }

        // SIMULAR LA NUEVA PROGRAMACION

        List<TurnoPersonal> propuestos =
            new ArrayList<>(turnos);

        propuestos.remove(
            solicitud.getOriginal()
        );

        validarHorario(
            solicitud.getPropuesto()
        );

        // VERIFICAR CONFLICTOS

        if (conflictoEmpleado(
                solicitud.getPropuesto(),
                propuestos)) {

            throw new ReglaNegocioException(
                "Cambio cruza otro turno del empleado"
            );
        }

        propuestos.add(
            solicitud.getPropuesto()
        );

        // VERIFICAR COBERTURA DEL CLUB

        if (!coberturaCompleta(propuestos)) {

            throw new ReglaNegocioException(
                "Cambio rechazado: quedarian menos de "
                + "2 entrenadores o 1 fisioterapeuta"
            );
        }

        // APROBAR Y APLICAR EL CAMBIO

        solicitud.resolver(
            administrador,
            SolicitudCambioTurno.Estado.APROBADA,
            "Cumple la cobertura minima"
        );

        turnos.clear();
        turnos.addAll(propuestos);
    }

    // RECHAZAR CAMBIO DE TURNO

    public void rechazarCambio(
            Administrador administrador,
            int numero,
            String motivo)
            throws ReglaNegocioException {

        validarAdministrador(administrador);

        buscar(numero).resolver(
            administrador,
            SolicitudCambioTurno.Estado.RECHAZADA,
            motivo
        );
    }
}
