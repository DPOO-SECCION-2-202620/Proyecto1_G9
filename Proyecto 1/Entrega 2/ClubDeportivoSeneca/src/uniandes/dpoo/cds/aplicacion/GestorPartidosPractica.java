
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.individuales.*;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class GestorPartidosPractica implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final double ELO_INICIAL = 1000.0;
    private static final double FACTOR_K = 32.0;

    // Todas estas colecciones se guardan en el mismo archivo.
    private final Map<String, InscripcionDisciplina> inscripciones = new HashMap<>();
    private final Map<String, Double> rankings = new HashMap<>();
    private final Set<String> entrenadores = new HashSet<>();
    private final List<PartidoPractica> partidos = new ArrayList<>();

    private static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
    }

    private static String clave(Socio socio, Disciplina disciplina) {
        return normalizar(disciplina.getNombre()) + "::" + normalizar(socio.getLogin());
    }

    public void registrarInscripcion(InscripcionDisciplina inscripcion)
            throws ReglaNegocioException {
        if (inscripcion == null || inscripcion.getSocio() == null ||
            inscripcion.getDisciplina() == null || inscripcion.getNivel() == null ||
            normalizar(inscripcion.getSocio().getLogin()).isEmpty() ||
            normalizar(inscripcion.getDisciplina().getNombre()).isEmpty()) {
            throw new ReglaNegocioException("La inscripcion esta incompleta");
        }
        String id = clave(inscripcion.getSocio(), inscripcion.getDisciplina());
        if (inscripciones.containsKey(id)) {
            throw new ReglaNegocioException("El socio ya esta inscrito en esa disciplina");
        }
        inscripciones.put(id, inscripcion);
        rankings.put(id, ELO_INICIAL);
    }

    public void registrarEntrenador(Entrenador entrenador)
            throws ReglaNegocioException {
        if (entrenador == null || normalizar(entrenador.getLogin()).isEmpty()) {
            throw new ReglaNegocioException("Entrenador invalido");
        }
        entrenadores.add(normalizar(entrenador.getLogin()));
    }

    public double getRanking(Socio socio, Disciplina disciplina)
            throws ReglaNegocioException {
        if (socio == null || disciplina == null ||
            !rankings.containsKey(clave(socio, disciplina))) {
            throw new ReglaNegocioException("El socio no esta inscrito en esta disciplina");
        }
        return rankings.get(clave(socio, disciplina));
    }

    public List<PartidoPractica> getPartidos() {
        return Collections.unmodifiableList(partidos);
    }

    public PartidoPractica programarPartido(LocalDate fecha, Disciplina disciplina,
            Modalidad modalidad, Socio... jugadores) throws ReglaNegocioException {

        if (fecha == null || disciplina == null || modalidad == null || jugadores == null) {
            throw new ReglaNegocioException("Faltan datos del partido");
        }
        String deporte = normalizar(disciplina.getNombre());
        boolean raqueta = deporte.equals("tenis") || deporte.equals("padel");
        boolean mesa = deporte.equals("tenis de mesa") || deporte.equals("ping pong");
        if (!raqueta && !mesa) {
            throw new ReglaNegocioException("Disciplina no permitida");
        }
        if (deporte.equals("padel") && modalidad != Modalidad.DOBLES) {
            throw new ReglaNegocioException("Padel se juega en dobles");
        }
        int esperado = modalidad == Modalidad.SENCILLOS ? 2 : 4;
        if (jugadores.length != esperado) {
            throw new ReglaNegocioException("Numero de participantes incompatible con modalidad");
        }

        List<InscripcionDisciplina> datos = new ArrayList<>();
        Set<String> identificadores = new HashSet<>();
        for (Socio socio : jugadores) {
            if (socio == null || normalizar(socio.getLogin()).isEmpty() ||
                !identificadores.add(normalizar(socio.getLogin()))) {
                throw new ReglaNegocioException("Jugador invalido o duplicado");
            }
            InscripcionDisciplina inscripcion = inscripciones.get(clave(socio, disciplina));
            if (inscripcion == null) {
                throw new ReglaNegocioException("Jugador no inscrito en la disciplina");
            }
            datos.add(inscripcion);
        }

        // Se admiten niveles iguales o adyacentes, salvo excepcion con entrenador.
        for (int i = 0; i < datos.size(); i++) {
            for (int j = i + 1; j < datos.size(); j++) {
                int diferencia = Math.abs(datos.get(i).getNivel().ordinal() -
                                          datos.get(j).getNivel().ordinal());
                boolean excepcion = entrenadores.contains(normalizar(jugadores[i].getLogin())) ||
                                   entrenadores.contains(normalizar(jugadores[j].getLogin()));
                if (diferencia > 1 && !excepcion) {
                    throw new ReglaNegocioException("Los niveles no son iguales ni adyacentes");
                }
            }
        }
        PartidoPractica nuevo = new PartidoPractica(
            fecha, disciplina, modalidad, Arrays.asList(jugadores));
        partidos.add(nuevo);
        return nuevo;
    }

    public void registrarEstadistica(PartidoPractica partido,
            EstadisticaPractica estadistica) throws ReglaNegocioException {
        if (partido == null || !partidos.contains(partido) || estadistica == null) {
            throw new ReglaNegocioException("Partido o estadistica no registrados");
        }
        if (partido.isFinalizado()) {
            throw new ReglaNegocioException("El partido ya finalizo");
        }
        String login = normalizar(estadistica.getJugador().getLogin());
        boolean participa = partido.getParticipantes().stream()
            .anyMatch(s -> normalizar(s.getLogin()).equals(login));
        if (!participa) {
            throw new ReglaNegocioException("El jugador no participa en este partido");
        }
        String deporte = normalizar(partido.getDisciplina().getNombre());
        boolean mesa = deporte.equals("tenis de mesa") || deporte.equals("ping pong");
        if ((mesa && !(estadistica instanceof EstadisticaTenisMesa)) ||
            (!mesa && !(estadistica instanceof EstadisticaRaqueta))) {
            throw new ReglaNegocioException("Tipo de estadistica incorrecto");
        }
        boolean repetida = partido.getEstadisticas().stream()
            .anyMatch(e -> normalizar(e.getJugador().getLogin()).equals(login));
        if (repetida) {
            throw new ReglaNegocioException("El jugador ya tiene estadistica");
        }
        partido.agregarEstadistica(estadistica);
    }

    public void finalizarPartido(PartidoPractica partido, Socio... ganadores)
            throws ReglaNegocioException {
        if (partido == null || !partidos.contains(partido) || ganadores == null) {
            throw new ReglaNegocioException("Partido o ganadores invalidos");
        }
        if (partido.isFinalizado()) {
            throw new ReglaNegocioException("El partido ya finalizo");
        }
        List<Socio> todos = partido.getParticipantes();
        if (partido.getEstadisticas().size() != todos.size()) {
            throw new ReglaNegocioException("Faltan estadisticas de participantes");
        }
        int mitad = todos.size() / 2;
        if (ganadores.length != mitad) {
            throw new ReglaNegocioException("Cantidad incorrecta de ganadores");
        }

        Set<String> ganan = new HashSet<>();
        Set<String> primerLado = new HashSet<>();
        Set<String> segundoLado = new HashSet<>();
        // En dobles, los primeros dos forman un lado y los dos siguientes el otro.
        for (int i = 0; i < todos.size(); i++) {
            String login = normalizar(todos.get(i).getLogin());
            if (i < mitad) primerLado.add(login); else segundoLado.add(login);
        }
        for (Socio ganador : ganadores) {
            if (ganador == null || !ganan.add(normalizar(ganador.getLogin()))) {
                throw new ReglaNegocioException("Ganadores invalidos o repetidos");
            }
        }
        if (!ganan.equals(primerLado) && !ganan.equals(segundoLado)) {
            throw new ReglaNegocioException("Los ganadores deben pertenecer al mismo lado");
        }

        double sumaGanadores = 0;
        double sumaPerdedores = 0;
        List<Socio> ganadoresConfirmados = new ArrayList<>();
        for (Socio jugador : todos) {
            double actual = rankings.get(clave(jugador, partido.getDisciplina()));
            if (ganan.contains(normalizar(jugador.getLogin()))) {
                sumaGanadores += actual;
                ganadoresConfirmados.add(jugador);
            } else {
                sumaPerdedores += actual;
            }
        }
        double mediaGanadores = sumaGanadores / mitad;
        double mediaPerdedores = sumaPerdedores / mitad;
        double probabilidad = 1.0 / (1.0 + Math.pow(10,
            (mediaPerdedores - mediaGanadores) / 400.0));
        double cambio = FACTOR_K * (1.0 - probabilidad);
        for (Socio jugador : todos) {
            String id = clave(jugador, partido.getDisciplina());
            boolean ganador = ganan.contains(normalizar(jugador.getLogin()));
            rankings.put(id, rankings.get(id) + (ganador ? cambio : -cambio));
        }
        partido.marcarFinalizado(ganadoresConfirmados);
    }
}
