
package uniandes.dpoo.cds.aplicacion;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.InscripcionEquipo;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.individuales.Modalidad;
import uniandes.dpoo.cds.modelo.individuales.Reserva;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class ClubDeportivoSeneca {

    private List<Equipo> equipos;
    private List<Reserva> reservas;

    public ClubDeportivoSeneca() {
        this.equipos = new ArrayList<>();
        this.reservas = new ArrayList<>();
    }

    public void registrarEquipo(Equipo equipo) {
        if (equipo == null) {
            throw new IllegalArgumentException(
                "El equipo no puede ser nulo"
            );
        }
        equipos.add(equipo);
    }

    public List<Equipo> getEquipos() {
        return Collections.unmodifiableList(equipos);
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    // RESERVAS DE INSTALACIONES DEPORTIVAS

    public void registrarReserva(Reserva nueva)
            throws ReglaNegocioException {

        if (nueva == null) {
            throw new ReglaNegocioException(
                "La reserva no puede ser nula"
            );
        }

        if (nueva.getSocio() == null) {
            throw new ReglaNegocioException(
                "La reserva debe tener un socio"
            );
        }

        if (nueva.getInstalacion() == null ||
            nueva.getInstalacion().getDisciplina() == null ||
            nueva.getInstalacion().getCodigo() == null ||
            nueva.getInstalacion().getCodigo().isBlank()) {

            throw new ReglaNegocioException(
                "La instalacion no es valida"
            );
        }

        if (nueva.getFecha() == null) {
            throw new ReglaNegocioException(
                "La fecha de la reserva no puede ser nula"
            );
        }

        if (nueva.getHoraInicio() == null ||
            nueva.getHoraFin() == null) {

            throw new ReglaNegocioException(
                "Las horas de la reserva no pueden ser nulas"
            );
        }

        if (!nueva.getHoraInicio()
                .isBefore(nueva.getHoraFin())) {

            throw new ReglaNegocioException(
                "El horario de la reserva no es valido"
            );
        }

        if (nueva.getModalidad() == null) {
            throw new ReglaNegocioException(
                "La modalidad es obligatoria"
            );
        }

        // VALIDAR DISCIPLINA Y NUMERO DE JUGADORES

        Instalacion instalacion = nueva.getInstalacion();

        String deporte = nombreNormalizado(
            instalacion.getDisciplina().getNombre()
        );

        int jugadores = nueva.getNumeroJugadores();
        int requeridos;

        switch (deporte) {

            case "tenis":
            case "tenis de mesa":
            case "ping pong":

                requeridos =
                    nueva.getModalidad() == Modalidad.SENCILLOS
                    ? 2 : 4;
                break;

            case "padel":

                if (nueva.getModalidad() != Modalidad.DOBLES) {
                    throw new ReglaNegocioException(
                        "En padel solo se permiten dobles"
                    );
                }

                requeridos = 4;
                break;

            default:
                throw new ReglaNegocioException(
                    "Disciplina no reconocida: " + deporte
                );
        }

        if (jugadores != requeridos ||
            jugadores > instalacion.getCapacidadMaxima()) {

            throw new ReglaNegocioException(
                "Numero de jugadores incompatible "
                + "con la modalidad o la capacidad"
            );
        }

        // VERIFICAR HORARIOS SUPERPUESTOS

        for (Reserva anterior : reservas) {

            if (mismaFecha(anterior, nueva) &&
                mismaInstalacion(
                    anterior.getInstalacion(), instalacion
                ) &&
                seSuperponen(anterior, nueva)) {

                throw new ReglaNegocioException(
                    "La instalacion ya esta reservada "
                    + "en ese horario"
                );
            }
        }

        // VALIDAR MAXIMO DE INSTALACIONES SIMULTANEAS

        int maximo = instalacion.getDisciplina()
                                .getMaximoInstalaciones();

        if (maximo <= 0) {
            throw new ReglaNegocioException(
                "La disciplina no tiene instalaciones disponibles"
            );
        }

        List<LocalTime> instantes = new ArrayList<>();
        instantes.add(nueva.getHoraInicio());

        for (Reserva anterior : reservas) {

            if (mismaFecha(anterior, nueva) &&
                mismaDisciplina(
                    anterior.getInstalacion(), instalacion
                ) &&
                seSuperponen(anterior, nueva) &&
                anterior.getHoraInicio()
                        .isAfter(nueva.getHoraInicio())) {

                instantes.add(anterior.getHoraInicio());
            }
        }

        for (LocalTime instante : instantes) {

            Set<String> instalacionesOcupadas =
                new HashSet<>();

            instalacionesOcupadas.add(
                instalacion.getCodigo()
                           .toLowerCase(Locale.ROOT)
            );

            for (Reserva anterior : reservas) {

                if (mismaFecha(anterior, nueva) &&
                    mismaDisciplina(
                        anterior.getInstalacion(), instalacion
                    ) &&
                    !anterior.getHoraInicio().isAfter(instante) &&
                    anterior.getHoraFin().isAfter(instante)) {

                    instalacionesOcupadas.add(
                        anterior.getInstalacion()
                                .getCodigo()
                                .toLowerCase(Locale.ROOT)
                    );
                }
            }

            if (instalacionesOcupadas.size() > maximo) {
                throw new ReglaNegocioException(
                    "No hay instalaciones disponibles "
                    + "para esa disciplina"
                );
            }
        }

        // REGISTRAR RESERVA
        reservas.add(nueva);
    }

    // METODOS AUXILIARES DE RESERVAS

    private boolean mismaFecha(Reserva a, Reserva b) {
        return a.getFecha().equals(b.getFecha());
    }

    private boolean mismaInstalacion(
            Instalacion a, Instalacion b) {

        return a.getCodigo().equalsIgnoreCase(b.getCodigo());
    }

    private boolean mismaDisciplina(
            Instalacion a, Instalacion b) {

        return nombreNormalizado(
            a.getDisciplina().getNombre()
        ).equals(
            nombreNormalizado(b.getDisciplina().getNombre())
        );
    }

    private boolean seSuperponen(Reserva a, Reserva b) {

        return a.getHoraInicio().isBefore(b.getHoraFin()) &&
               b.getHoraInicio().isBefore(a.getHoraFin());
    }

    private String nombreNormalizado(String texto) {

        if (texto == null) {
            return "";
        }

        String sinTildes = Normalizer.normalize(
            texto, Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        return sinTildes.trim().toLowerCase(Locale.ROOT);
    }

    // INSCRIPCION DE SOCIOS EN EQUIPOS

    public void inscribirSocioEnEquipo(
            Socio socio,
            Equipo equipo,
            String posicion,
            int numeroCamiseta)
            throws ReglaNegocioException {

        if (socio == null || equipo == null) {
            throw new ReglaNegocioException(
                "El socio o equipo no son validos"
            );
        }

        if (!equipos.contains(equipo)) {
            throw new ReglaNegocioException(
                "El equipo no esta registrado en el club"
            );
        }

        LocalDate hoy = LocalDate.now();

        if (socio.getFechaNacimiento() == null ||
            socio.getFechaNacimiento().isAfter(hoy)) {

            throw new ReglaNegocioException(
                "La fecha de nacimiento no es valida"
            );
        }

        int edad = Period.between(
            socio.getFechaNacimiento(), hoy
        ).getYears();

        if (!equipo.getCategoria().admiteEdad(edad)) {
            throw new ReglaNegocioException(
                "El socio no cumple la edad de la categoria"
            );
        }

        for (Equipo otroEquipo : equipos) {

            boolean mismoDeporte =
                otroEquipo.getDeporte().getNombre()
                .equalsIgnoreCase(
                    equipo.getDeporte().getNombre()
                );

            if (mismoDeporte &&
                otroEquipo.contieneSocio(socio)) {

                throw new ReglaNegocioException(
                    "El socio ya pertenece a un equipo "
                    + "de este deporte"
                );
            }
        }

        InscripcionEquipo inscripcion =
            new InscripcionEquipo(
                socio, equipo, posicion, numeroCamiseta
            );

        equipo.agregarInscripcion(inscripcion);
    }
}
