
package uniandes.dpoo.cds.aplicacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.competencias.Competencia;

public class FaseASCUN implements Serializable {

    private static final long serialVersionUID = 1L;

    // RESULTADO DE UN PARTIDO

    public static class Resultado implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String local;
        private final String visitante;
        private final int golesLocal;
        private final int golesVisitante;

        private Resultado(
                String local,
                String visitante,
                int golesLocal,
                int golesVisitante) {

            this.local = local;
            this.visitante = visitante;
            this.golesLocal = golesLocal;
            this.golesVisitante = golesVisitante;
        }

        public String getLocal() {
            return local;
        }

        public String getVisitante() {
            return visitante;
        }

        public int getGolesLocal() {
            return golesLocal;
        }

        public int getGolesVisitante() {
            return golesVisitante;
        }

        public String getGanador() {

            if (golesLocal == golesVisitante) {
                return null;
            }

            return golesLocal > golesVisitante
                ? local : visitante;
        }
    }

    // FILA DE TABLA DE POSICIONES

    public static class Fila {

        private final String equipo;

        private int jugados;
        private int ganados;
        private int empatados;
        private int perdidos;
        private int golesFavor;
        private int golesContra;
        private int puntos;

        private Fila(String equipo) {
            this.equipo = equipo;
        }

        private void registrar(int favor, int contra) {

            jugados++;
            golesFavor += favor;
            golesContra += contra;

            if (favor > contra) {
                ganados++;
                puntos += 3;

            } else if (favor == contra) {
                empatados++;
                puntos++;

            } else {
                perdidos++;
            }
        }

        public String getEquipo() {
            return equipo;
        }

        public int getJugados() {
            return jugados;
        }

        public int getGanados() {
            return ganados;
        }

        public int getEmpatados() {
            return empatados;
        }

        public int getPerdidos() {
            return perdidos;
        }

        public int getPuntos() {
            return puntos;
        }

        public int getGolesFavor() {
            return golesFavor;
        }

        public int getGolesContra() {
            return golesContra;
        }

        public int getDiferenciaGoles() {
            return golesFavor - golesContra;
        }
    }

    // ATRIBUTOS DEL TORNEO

    private final Competencia competencia;

    private final Map<String, List<String>> grupos =
        new LinkedHashMap<>();

    private final Map<String, List<Resultado>> resultados =
        new LinkedHashMap<>();

    private final Map<Integer, Resultado> semifinales =
        new HashMap<>();

    private Resultado finalTorneo;

    private boolean gruposCerrados;

    // CONSTRUCTOR

    public FaseASCUN(Competencia competencia) {

        if (competencia == null ||
            !"GRUPOS_Y_ELIMINACION".equalsIgnoreCase(
                competencia.getFormato()
            ) ||
            !"FUTBOL".equalsIgnoreCase(
                competencia.getDeporte().getNombre()
            )) {

            throw new IllegalArgumentException(
                "Esta version requiere una competencia "
                + "de futbol con grupos y eliminacion"
            );
        }

        this.competencia = competencia;
    }

    public Competencia getCompetencia() {
        return competencia;
    }

    // CREAR GRUPOS

    public void crearGrupo(
            String nombre,
            String... participantes)
            throws ReglaNegocioException {

        if (gruposCerrados ||
            grupos.size() >= 2 ||
            nombre == null ||
            nombre.isBlank() ||
            participantes == null ||
            participantes.length < 2) {

            throw new ReglaNegocioException(
                "Grupo invalido o fase cerrada"
            );
        }

        for (String grupoExistente : grupos.keySet()) {

            if (grupoExistente.equalsIgnoreCase(
                    nombre.trim())) {

                throw new ReglaNegocioException(
                    "El grupo ya existe"
                );
            }
        }

        List<String> nombres = new ArrayList<>();

        Set<String> normalizados = new LinkedHashSet<>();

        for (List<String> existentes : grupos.values()) {

            for (String s : existentes) {
                normalizados.add(
                    s.toLowerCase(Locale.ROOT)
                );
            }
        }

        for (String participante : participantes) {

            if (participante == null ||
                participante.isBlank() ||
                !normalizados.add(
                    participante.trim()
                        .toLowerCase(Locale.ROOT)
                )) {

                throw new ReglaNegocioException(
                    "Participante vacio o duplicado"
                );
            }

            nombres.add(participante.trim());
        }

        grupos.put(nombre.trim(), nombres);

        resultados.put(
            nombre.trim(),
            new ArrayList<>()
        );
    }

    // BUSCAR GRUPO

    private String nombreGrupo(String nombre)
            throws ReglaNegocioException {

        if (nombre != null) {

            for (String grupo : grupos.keySet()) {

                if (grupo.equalsIgnoreCase(nombre.trim())) {
                    return grupo;
                }
            }
        }

        throw new ReglaNegocioException(
            "Grupo no registrado"
        );
    }

    // BUSCAR PARTICIPANTE

    private String nombreParticipante(
            String grupo,
            String nombre)
            throws ReglaNegocioException {

        if (nombre != null) {

            for (String participante : grupos.get(grupo)) {

                if (participante.equalsIgnoreCase(
                        nombre.trim())) {

                    return participante;
                }
            }
        }

        throw new ReglaNegocioException(
            "El participante no pertenece al grupo"
        );
    }

    // REGISTRAR PARTIDO DE GRUPO

    public void registrarPartidoGrupo(
            String nombreGrupo,
            String local,
            String visitante,
            int golesLocal,
            int golesVisitante)
            throws ReglaNegocioException {

        if (gruposCerrados) {
            throw new ReglaNegocioException(
                "La fase de grupos esta cerrada"
            );
        }

        String grupo = nombreGrupo(nombreGrupo);

        String l = nombreParticipante(grupo, local);
        String v = nombreParticipante(grupo, visitante);

        validarResultado(
            l, v, golesLocal, golesVisitante, false
        );

        for (Resultado r : resultados.get(grupo)) {

            if ((r.local.equals(l) &&
                 r.visitante.equals(v)) ||

                (r.local.equals(v) &&
                 r.visitante.equals(l))) {

                throw new ReglaNegocioException(
                    "El partido del grupo ya existe"
                );
            }
        }

        resultados.get(grupo).add(
            new Resultado(
                l, v, golesLocal, golesVisitante
            )
        );
    }

    // VALIDAR MARCADOR

    private static void validarResultado(
            String local,
            String visitante,
            int golesLocal,
            int golesVisitante,
            boolean eliminacion)
            throws ReglaNegocioException {

        if (local.equals(visitante) ||
            golesLocal < 0 ||
            golesVisitante < 0) {

            throw new ReglaNegocioException(
                "Partido o marcador invalido"
            );
        }

        if (eliminacion &&
            golesLocal == golesVisitante) {

            throw new ReglaNegocioException(
                "En eliminacion se necesita ganador "
                + "(sin penales en esta version)"
            );
        }
    }

    // CALCULAR TABLA DE UN GRUPO

    public List<Fila> tablaGrupo(String nombreGrupo)
            throws ReglaNegocioException {

        String grupo = nombreGrupo(nombreGrupo);

        Map<String, Fila> filas = new LinkedHashMap<>();

        for (String nombre : grupos.get(grupo)) {
            filas.put(nombre, new Fila(nombre));
        }

        for (Resultado r : resultados.get(grupo)) {

            filas.get(r.local).registrar(
                r.golesLocal,
                r.golesVisitante
            );

            filas.get(r.visitante).registrar(
                r.golesVisitante,
                r.golesLocal
            );
        }

        List<Fila> tabla =
            new ArrayList<>(filas.values());

        tabla.sort(
            Comparator.comparingInt(Fila::getPuntos)
                      .reversed()
        );

        // DESEMPATAR POR ENFRENTAMIENTOS DIRECTOS

        for (int inicio = 0; inicio < tabla.size();) {

            int fin = inicio + 1;

            while (fin < tabla.size() &&
                   tabla.get(fin).getPuntos() ==
                   tabla.get(inicio).getPuntos()) {

                fin++;
            }

            List<Fila> empate = new ArrayList<>(
                tabla.subList(inicio, fin)
            );

            Set<String> empatados =
                new LinkedHashSet<>();

            for (Fila f : empate) {
                empatados.add(f.equipo);
            }

            Map<String, Fila> directos =
                new HashMap<>();

            for (Fila f : empate) {
                directos.put(
                    f.equipo,
                    new Fila(f.equipo)
                );
            }

            for (Resultado r : resultados.get(grupo)) {

                if (empatados.contains(r.local) &&
                    empatados.contains(r.visitante)) {

                    directos.get(r.local).registrar(
                        r.golesLocal,
                        r.golesVisitante
                    );

                    directos.get(r.visitante).registrar(
                        r.golesVisitante,
                        r.golesLocal
                    );
                }
            }

            empate.sort(
                Comparator.comparingInt(
                    (Fila f) ->
                        directos.get(f.equipo).getPuntos()
                ).reversed()

                .thenComparing(
                    Comparator.comparingInt(
                        (Fila f) ->
                            directos.get(f.equipo)
                                   .getDiferenciaGoles()
                    ).reversed()
                )

                .thenComparing(
                    Comparator.comparingInt(
                        Fila::getDiferenciaGoles
                    ).reversed()
                )

                .thenComparing(
                    Comparator.comparingInt(
                        Fila::getGolesFavor
                    ).reversed()
                )

                .thenComparing(
                    Fila::getEquipo,
                    String.CASE_INSENSITIVE_ORDER
                )
            );

            for (int j = 0; j < empate.size(); j++) {
                tabla.set(
                    inicio + j,
                    empate.get(j)
                );
            }

            inicio = fin;
        }

        return Collections.unmodifiableList(tabla);
    }

    // CERRAR FASE DE GRUPOS

    public void cerrarGrupos()
            throws ReglaNegocioException {

        if (gruposCerrados || grupos.size() != 2) {

            throw new ReglaNegocioException(
                "Deben existir exactamente dos grupos abiertos"
            );
        }

        for (String grupo : grupos.keySet()) {

            int n = grupos.get(grupo).size();

            if (resultados.get(grupo).size() !=
                n * (n - 1) / 2) {

                throw new ReglaNegocioException(
                    "Faltan partidos en el grupo " + grupo
                );
            }
        }

        gruposCerrados = true;
    }

    // DEFINIR CRUCES DE SEMIFINAL

    private String[] cruceSemifinal(int numero)
            throws ReglaNegocioException {

        if (!gruposCerrados ||
            (numero != 1 && numero != 2)) {

            throw new ReglaNegocioException(
                "La semifinal aun no esta disponible"
            );
        }

        List<String> nombres =
            new ArrayList<>(grupos.keySet());

        List<Fila> a =
            tablaGrupo(nombres.get(0));

        List<Fila> b =
            tablaGrupo(nombres.get(1));

        return numero == 1

            ? new String[] {
                a.get(0).getEquipo(),
                b.get(1).getEquipo()
              }

            : new String[] {
                b.get(0).getEquipo(),
                a.get(1).getEquipo()
              };
    }

    public String descripcionSemifinal(int numero)
            throws ReglaNegocioException {

        String[] cruce = cruceSemifinal(numero);

        return cruce[0] + " vs " + cruce[1];
    }

    // REGISTRAR SEMIFINAL

    public void registrarSemifinal(
            int numero,
            int golesLocal,
            int golesVisitante)
            throws ReglaNegocioException {

        String[] cruce = cruceSemifinal(numero);

        if (semifinales.containsKey(numero)) {

            throw new ReglaNegocioException(
                "La semifinal ya fue registrada"
            );
        }

        validarResultado(
            cruce[0],
            cruce[1],
            golesLocal,
            golesVisitante,
            true
        );

        semifinales.put(
            numero,
            new Resultado(
                cruce[0],
                cruce[1],
                golesLocal,
                golesVisitante
            )
        );
    }

    // DEFINIR FINALISTAS

    public String descripcionFinal()
            throws ReglaNegocioException {

        if (!semifinales.containsKey(1) ||
            !semifinales.containsKey(2)) {

            throw new ReglaNegocioException(
                "Faltan semifinales"
            );
        }

        return semifinales.get(1).getGanador()
            + " vs "
            + semifinales.get(2).getGanador();
    }

    // REGISTRAR FINAL

    public void registrarFinal(
            int golesLocal,
            int golesVisitante)
            throws ReglaNegocioException {

        if (finalTorneo != null) {

            throw new ReglaNegocioException(
                "La final ya fue registrada"
            );
        }

        descripcionFinal();

        String local =
            semifinales.get(1).getGanador();

        String visitante =
            semifinales.get(2).getGanador();

        validarResultado(
            local,
            visitante,
            golesLocal,
            golesVisitante,
            true
        );

        finalTorneo = new Resultado(
            local,
            visitante,
            golesLocal,
            golesVisitante
        );
    }

    // CONSULTAR CAMPEON

    public String getCampeon() {

        return finalTorneo == null
            ? null
            : finalTorneo.getGanador();
    }
}
