
package uniandes.dpoo.cds.modelo.entrenamientos;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class SesionEntrenamiento implements Serializable {

    private static final long serialVersionUID = 1L;

    private final LocalDate fecha;
    private final LocalTime hora;
    private final int duracionMinutos;
    private final Entrenador entrenador;
    private final Instalacion instalacion;

    private final List<Socio> convocados = new ArrayList<>();

    private final Map<String, Asistencia> asistencias =
        new LinkedHashMap<>();

    private boolean finalizada;

    public SesionEntrenamiento(
            LocalDate fecha,
            LocalTime hora,
            int duracionMinutos,
            Entrenador entrenador,
            Instalacion instalacion) {

        if (fecha == null ||
            hora == null ||
            duracionMinutos <= 0 ||
            entrenador == null ||
            instalacion == null ||
            instalacion.getCodigo() == null ||
            instalacion.getCodigo().isBlank()) {

            throw new IllegalArgumentException(
                "Datos de sesion invalidos"
            );
        }

        this.fecha = fecha;
        this.hora = hora;
        this.duracionMinutos = duracionMinutos;
        this.entrenador = entrenador;
        this.instalacion = instalacion;
    }

    // GETTERS

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public Entrenador getEntrenador() {
        return entrenador;
    }

    public Instalacion getInstalacion() {
        return instalacion;
    }

    public boolean isFinalizada() {
        return finalizada;
    }

    public List<Socio> getConvocados() {
        return Collections.unmodifiableList(convocados);
    }

    public List<Asistencia> getAsistencias() {
        return Collections.unmodifiableList(
            new ArrayList<>(asistencias.values())
        );
    }

    // IDENTIFICAR SOCIOS

    private String clave(Socio socio)
            throws ReglaNegocioException {

        if (socio == null ||
            socio.getLogin() == null ||
            socio.getLogin().isBlank()) {

            throw new ReglaNegocioException(
                "Socio invalido"
            );
        }

        return socio.getLogin()
                    .trim()
                    .toLowerCase(Locale.ROOT);
    }

    // VALIDAR ENTRENADOR

    private void verificarEntrenador(
            Entrenador responsable)
            throws ReglaNegocioException {

        if (responsable == null ||
            responsable.getLogin() == null ||
            !responsable.getLogin().equalsIgnoreCase(
                entrenador.getLogin())) {

            throw new ReglaNegocioException(
                "El entrenador no esta autorizado"
            );
        }
    }

    // CONVOCAR SOCIOS

    public void convocarSocio(Socio socio)
            throws ReglaNegocioException {

        if (finalizada) {
            throw new ReglaNegocioException(
                "La sesion ya finalizo"
            );
        }

        String id = clave(socio);

        for (Socio anterior : convocados) {

            if (clave(anterior).equals(id)) {
                throw new ReglaNegocioException(
                    "Socio ya convocado"
                );
            }
        }

        convocados.add(socio);
    }

    // FINALIZAR SESION

    public void finalizarSesion(
            Entrenador responsable)
            throws ReglaNegocioException {

        verificarEntrenador(responsable);

        if (finalizada) {
            throw new ReglaNegocioException(
                "La sesion ya finalizo"
            );
        }

        if (convocados.isEmpty()) {
            throw new ReglaNegocioException(
                "Debe convocar al menos un socio"
            );
        }

        finalizada = true;
    }

    // REGISTRAR ASISTENCIA

    public void registrarAsistencia(
            Entrenador responsable,
            Socio socio,
            EstadoAsistencia estado)
            throws ReglaNegocioException {

        verificarEntrenador(responsable);

        if (!finalizada) {
            throw new ReglaNegocioException(
                "Primero debe finalizar la sesion"
            );
        }

        String id = clave(socio);

        boolean convocado = false;

        for (Socio persona : convocados) {

            if (clave(persona).equals(id)) {
                convocado = true;
                break;
            }
        }

        if (!convocado) {
            throw new ReglaNegocioException(
                "El socio no estaba convocado"
            );
        }

        if (estado == null) {
            throw new ReglaNegocioException(
                "Estado de asistencia obligatorio"
            );
        }

        if (asistencias.containsKey(id)) {
            throw new ReglaNegocioException(
                "La asistencia ya fue registrada"
            );
        }

        asistencias.put(
            id,
            new Asistencia(socio, estado)
        );
    }

    // REGISTRAR EVALUACION

    public void registrarEvaluacion(
            Entrenador responsable,
            Socio socio,
            int nota,
            String comentario)
            throws ReglaNegocioException {

        verificarEntrenador(responsable);

        Asistencia asistencia =
            asistencias.get(clave(socio));

        if (asistencia == null) {
            throw new ReglaNegocioException(
                "Primero registre la asistencia"
            );
        }

        if (asistencia.getEvaluacion() != null) {
            throw new ReglaNegocioException(
                "El socio ya tiene evaluacion"
            );
        }

        asistencia.asignarEvaluacion(
            new Evaluacion(nota, comentario)
        );
    }
}
