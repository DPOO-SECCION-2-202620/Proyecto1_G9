
package uniandes.dpoo.cds.aplicacion;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.competencias.InscripcionCompetencia;
import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;
import uniandes.dpoo.cds.modelo.competencias.PosicionEquipo;
import uniandes.dpoo.cds.modelo.conjunto.Equipo;

public class TablaPosicionesLiga {

    private final GestorCompetencias gestorCompetencias;
    private final GestorPartidosCompetencia gestorPartidos;

    public TablaPosicionesLiga(
            GestorCompetencias gestorCompetencias,
            GestorPartidosCompetencia gestorPartidos) {

        if (gestorCompetencias == null ||
            gestorPartidos == null) {

            throw new IllegalArgumentException(
                "Los gestores son obligatorios"
            );
        }

        this.gestorCompetencias = gestorCompetencias;
        this.gestorPartidos = gestorPartidos;
    }

    // CALCULAR TABLA

    public List<PosicionEquipo> calcular(
            Competencia competencia)
            throws ReglaNegocioException {

        if (competencia == null ||
            !gestorCompetencias.getCompetencias()
                .contains(competencia)) {

            throw new ReglaNegocioException(
                "Competencia no registrada"
            );
        }

        if (!normalizar(competencia.getFormato())
                .equals("liga") ||
            !normalizar(competencia.getDeporte().getNombre())
                .equals("futbol")) {

            throw new ReglaNegocioException(
                "Esta tabla solo admite competencias "
                + "de futbol en formato liga"
            );
        }

        Map<InscripcionCompetencia, Acumulado> acumulados =
            new LinkedHashMap<>();

        // OBTENER LOS EQUIPOS INSCRITOS

        for (InscripcionCompetencia inscripcion :
                gestorCompetencias.getInscripciones()) {

            if (inscripcion.getCompetencia() == competencia &&
                inscripcion.isOficial()) {

                acumulados.put(
                    inscripcion,
                    new Acumulado(inscripcion.getEquipo())
                );
            }
        }

        // REVISAR RESULTADOS DE PARTIDOS

        for (PartidoCompetencia partido :
                gestorPartidos.getPartidos()) {

            Acumulado acumulado =
                acumulados.get(partido.getInscripcion());

            if (acumulado != null &&
                partido.estaFinalizado()) {

                acumulado.agregar(
                    partido.getMarcadorClub(),
                    partido.getMarcadorRival(),
                    partido.getPuntosLigaFutbol()
                );
            }
        }

        // CONSTRUIR TABLA

        List<PosicionEquipo> tabla = new ArrayList<>();

        for (Acumulado dato : acumulados.values()) {
            tabla.add(dato.construir());
        }

        // ORDENAR EQUIPOS

        tabla.sort(
            Comparator
                .comparingInt(PosicionEquipo::getPuntos)
                .reversed()
                .thenComparing(
                    Comparator.comparingInt(
                        PosicionEquipo::getDiferenciaGoles
                    ).reversed()
                )
                .thenComparing(
                    Comparator.comparingInt(
                        PosicionEquipo::getGolesFavor
                    ).reversed()
                )
                .thenComparing(
                    f -> f.getEquipo().getNombre(),
                    String.CASE_INSENSITIVE_ORDER
                )
        );

        return Collections.unmodifiableList(tabla);
    }

    // NORMALIZAR NOMBRES

    private static String normalizar(String valor) {

        if (valor == null) {
            return "";
        }

        return Normalizer.normalize(
            valor, Normalizer.Form.NFD
        )
        .replaceAll("\\p{M}", "")
        .trim()
        .toLowerCase(Locale.ROOT);
    }

    // ACUMULAR RESULTADOS

    private static class Acumulado {

        private final Equipo equipo;

        private int ganados;
        private int empatados;
        private int perdidos;

        private int golesFavor;
        private int golesContra;
        private int puntos;

        private Acumulado(Equipo equipo) {
            this.equipo = equipo;
        }

        private void agregar(
                int aFavor,
                int enContra,
                int puntosPartido) {

            golesFavor += aFavor;
            golesContra += enContra;
            puntos += puntosPartido;

            if (aFavor > enContra) {
                ganados++;
            } else if (aFavor == enContra) {
                empatados++;
            } else {
                perdidos++;
            }
        }

        private PosicionEquipo construir() {

            return new PosicionEquipo(
                equipo,
                ganados,
                empatados,
                perdidos,
                golesFavor,
                golesContra,
                puntos
            );
        }
    }
}
