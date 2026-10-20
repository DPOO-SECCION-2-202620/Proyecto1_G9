
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaBaloncesto;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaFutbol;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaJugador;
import uniandes.dpoo.cds.modelo.conjunto.EstadisticaVoleibol;
import uniandes.dpoo.cds.modelo.conjunto.InscripcionEquipo;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;

public class GestorEstadisticas implements Serializable {

    private static final long serialVersionUID = 1L;

    // ESTADISTICAS POR PARTIDO

    private final Map<PartidoInterno, List<EstadisticaJugador>>
        estadisticas = new IdentityHashMap<>();

    // SANCIONES PENDIENTES

    private final Map<Equipo, Set<String>>
        suspendidosSiguientePartido = new IdentityHashMap<>();

    // SANCIONES QUE SE CUMPLEN EN CADA PARTIDO

    private final Map<PartidoInterno, Map<Equipo, Set<String>>>
        suspendidosEnPartido = new IdentityHashMap<>();

    // INICIAR UN PARTIDO

    public void iniciarPartido(PartidoInterno partido)
            throws ReglaNegocioException {

        if (partido == null) {
            throw new ReglaNegocioException(
                "El partido no puede ser nulo"
            );
        }

        if (partido.estaFinalizado()) {
            throw new ReglaNegocioException(
                "Un partido finalizado no puede iniciarse"
            );
        }

        if (suspendidosEnPartido.containsKey(partido)) {
            throw new ReglaNegocioException(
                "El partido ya fue iniciado"
            );
        }

        Equipo local = partido.getEquipoLocal();
        Equipo visitante = partido.getEquipoVisitante();

        if (local == null || visitante == null ||
            local == visitante) {

            throw new ReglaNegocioException(
                "Los equipos del partido no son validos"
            );
        }

        Set<String> bloqueadosLocal = new HashSet<>(
            suspendidosSiguientePartido.getOrDefault(
                local,
                Collections.emptySet()
            )
        );

        Set<String> bloqueadosVisitante = new HashSet<>(
            suspendidosSiguientePartido.getOrDefault(
                visitante,
                Collections.emptySet()
            )
        );

        // COMPROBAR JUGADORES DISPONIBLES

        if (contarHabilitados(local, bloqueadosLocal)
                < local.getDeporte().getMinimoJugadores() ||
            contarHabilitados(visitante, bloqueadosVisitante)
                < visitante.getDeporte().getMinimoJugadores()) {

            throw new ReglaNegocioException(
                "No hay suficientes jugadores habilitados para jugar"
            );
        }

        Map<Equipo, Set<String>> bloqueos =
            new IdentityHashMap<>();

        bloqueos.put(local, bloqueadosLocal);
        bloqueos.put(visitante, bloqueadosVisitante);

        suspendidosEnPartido.put(partido, bloqueos);

        // LA SANCION SE CUMPLE EN ESTE PARTIDO

        suspendidosSiguientePartido.remove(local);
        suspendidosSiguientePartido.remove(visitante);
    }

    // CONTAR JUGADORES HABILITADOS

    private int contarHabilitados(
            Equipo equipo,
            Set<String> bloqueados) {

        int total = 0;

        for (InscripcionEquipo inscripcion :
                equipo.getInscripciones()) {

            if (!bloqueados.contains(
                    inscripcion.getSocio().getLogin())) {

                total++;
            }
        }

        return total;
    }

    // REGISTRAR ESTADISTICAS

    public void registrarEstadistica(
            EstadisticaJugador dato)
            throws ReglaNegocioException {

        if (dato == null ||
            dato.getPartido() == null ||
            dato.getEquipo() == null ||
            dato.getJugador() == null) {

            throw new ReglaNegocioException(
                "La estadistica no tiene los datos necesarios"
            );
        }

        PartidoInterno partido = dato.getPartido();
        Equipo equipo = dato.getEquipo();
        String login = dato.getJugador().getLogin();

        if (!suspendidosEnPartido.containsKey(partido)) {
            throw new ReglaNegocioException(
                "Primero debe iniciarse el partido"
            );
        }

        if (partido.estaFinalizado()) {
            throw new ReglaNegocioException(
                "No se pueden registrar estadisticas tras finalizar el partido"
            );
        }

        if (equipo != partido.getEquipoLocal() &&
            equipo != partido.getEquipoVisitante()) {

            throw new ReglaNegocioException(
                "El equipo no participa en este partido"
            );
        }

        if (login == null ||
            !equipo.contieneSocio(dato.getJugador())) {

            throw new ReglaNegocioException(
                "El jugador no pertenece al equipo"
            );
        }

        // COMPROBAR SANCIONES DEL PARTIDO

        if (suspendidosEnPartido.get(partido)
                .get(equipo).contains(login)) {

            throw new ReglaNegocioException(
                "El jugador esta suspendido para este partido"
            );
        }

        // IDENTIFICAR DISCIPLINA

        String nombre = Normalizer.normalize(
            equipo.getDeporte().getNombre(),
            Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "")
         .trim()
         .toLowerCase(Locale.ROOT);

        boolean futbol = nombre.equals("futbol");

        boolean baloncesto =
            nombre.equals("baloncesto") ||
            nombre.equals("basket") ||
            nombre.equals("basquetbol");

        boolean voleibol =
            nombre.equals("voleibol") ||
            nombre.equals("volley") ||
            nombre.equals("volleyball");

        // VALIDAR EL TIPO DE ESTADISTICA

        if (!((futbol && dato instanceof EstadisticaFutbol) ||
              (baloncesto &&
               dato instanceof EstadisticaBaloncesto) ||
              (voleibol &&
               dato instanceof EstadisticaVoleibol))) {

            throw new ReglaNegocioException(
                "El tipo de estadistica no corresponde al deporte"
            );
        }

        // BUSCAR ESTADISTICAS DEL PARTIDO

        List<EstadisticaJugador> lista =
            estadisticas.computeIfAbsent(
                partido,
                p -> new ArrayList<>()
            );

        // RECHAZAR REGISTROS DUPLICADOS

        for (EstadisticaJugador existente : lista) {

            if (existente.getEquipo() == equipo &&
                existente.getJugador()
                    .getLogin().equals(login)) {

                throw new ReglaNegocioException(
                    "El jugador ya tiene estadisticas en este partido"
                );
            }
        }

        // GUARDAR REGISTRO

        lista.add(dato);

        // APLICAR SANCIONES DE FUTBOL

        if (dato instanceof EstadisticaFutbol) {

            EstadisticaFutbol futbolDato =
                (EstadisticaFutbol) dato;

            if (futbolDato.getAmarillas() >= 2 ||
                futbolDato.getRojas() >= 1) {

                suspendidosSiguientePartido
                    .computeIfAbsent(
                        equipo,
                        k -> new HashSet<>()
                    )
                    .add(login);
            }
        }
    }

    // CONSULTAR ESTADISTICAS

    public List<EstadisticaJugador> getEstadisticas(
            PartidoInterno partido) {

        return Collections.unmodifiableList(
            estadisticas.getOrDefault(
                partido,
                Collections.emptyList()
            )
        );
    }

    // CONSULTAR SUSPENSIONES PENDIENTES

    public boolean suspendidoEnProximoPartido(
            Equipo equipo,
            String login) {

        return suspendidosSiguientePartido
            .getOrDefault(
                equipo,
                Collections.emptySet()
            )
            .contains(login);
    }
}
