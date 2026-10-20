
package uniandes.dpoo.cds.aplicacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;
import uniandes.dpoo.cds.modelo.conjunto.PartidoInterno;
import uniandes.dpoo.cds.modelo.entrenamientos.SesionEntrenamiento;

import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.PartidoPractica;
import uniandes.dpoo.cds.modelo.individuales.RegistroRanking;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class GestorReportesDeportivos {

    // ATRIBUTOS

    private final GestorEntrenamientos entrenamientos;
    private final GestorPartidosInternos internos;
    private final GestorPartidosCompetencia oficiales;
    private final GestorPartidosPractica individuales;

    private final List<RegistroRanking> historialRankings =
        new ArrayList<>();

    // CONSTRUCTOR

    public GestorReportesDeportivos(
            GestorEntrenamientos entrenamientos,
            GestorPartidosInternos internos,
            GestorPartidosCompetencia oficiales,
            GestorPartidosPractica individuales) {

        if (entrenamientos == null ||
            internos == null ||
            oficiales == null ||
            individuales == null) {

            throw new IllegalArgumentException(
                "Todos los gestores deportivos son obligatorios"
            );
        }

        this.entrenamientos = entrenamientos;
        this.internos = internos;
        this.oficiales = oficiales;
        this.individuales = individuales;
    }

    // VALIDAR ADMINISTRADOR

    private static void validarAdministrador(
            Administrador administrador)
            throws ReglaNegocioException {

        if (administrador == null ||
            administrador.getLogin() == null ||
            administrador.getLogin().isBlank()) {

            throw new ReglaNegocioException(
                "El reporte requiere un administrador"
            );
        }
    }

    // VALIDAR FECHAS

    private static void validarFechas(
            LocalDate inicio,
            LocalDate fin)
            throws ReglaNegocioException {

        if (inicio == null ||
            fin == null ||
            inicio.isAfter(fin)) {

            throw new ReglaNegocioException(
                "Intervalo de fechas invalido"
            );
        }
    }

    // COMPROBAR SI UNA FECHA ESTA EN EL RANGO

    private static boolean enRango(
            LocalDate fecha,
            LocalDate inicio,
            LocalDate fin) {

        return fecha != null &&
            !fecha.isBefore(inicio) &&
            !fecha.isAfter(fin);
    }

    // GENERAR REPORTE DEPORTIVO

    public ResumenDeportivo generarResumen(
            Administrador administrador,
            LocalDate inicio,
            LocalDate fin)
            throws ReglaNegocioException {

        validarAdministrador(administrador);
        validarFechas(inicio, fin);

        ResumenDeportivo resumen =
            new ResumenDeportivo(inicio, fin);

        // REVISAR ENTRENAMIENTOS

        for (SesionEntrenamiento sesion :
                entrenamientos.getSesiones()) {

            if (enRango(
                    sesion.getFecha(),
                    inicio,
                    fin)) {

                resumen.contarSesion(sesion);
            }
        }

        // REVISAR PARTIDOS INTERNOS

        for (PartidoInterno partido :
                internos.getPartidos()) {

            if (partido.estaFinalizado() &&
                enRango(
                    partido.getFecha(),
                    inicio,
                    fin)) {

                resumen.contarInterno();
            }
        }

        // REVISAR PARTIDOS OFICIALES

        for (PartidoCompetencia partido :
                oficiales.getPartidos()) {

            if (partido.estaFinalizado() &&
                enRango(
                    partido.getFecha(),
                    inicio,
                    fin)) {

                resumen.contarOficial(partido);
            }
        }

        // REVISAR PARTIDOS INDIVIDUALES

        for (PartidoPractica partido :
                individuales.getPartidos()) {

            if (partido.isFinalizado() &&
                enRango(
                    partido.getFecha(),
                    inicio,
                    fin)) {

                resumen.contarPractica();
            }
        }

        return resumen;
    }

    // REGISTRAR UNA INSTANTANEA DEL RANKING

    public void registrarInstantaneaRanking(
            LocalDate fecha,
            Socio socio,
            Disciplina disciplina)
            throws ReglaNegocioException {

        if (fecha == null ||
            socio == null ||
            disciplina == null) {

            throw new ReglaNegocioException(
                "Datos de ranking invalidos"
            );
        }

        // CONSULTAR ELO ACTUAL

        double puntosActuales =
            individuales.getRanking(socio, disciplina);

        // EVITAR DUPLICADOS

        for (RegistroRanking registro : historialRankings) {

            if (registro.getFecha().equals(fecha) &&
                registro.getLoginSocio()
                    .equalsIgnoreCase(socio.getLogin()) &&
                registro.getNombreDisciplina()
                    .equalsIgnoreCase(
                        disciplina.getNombre())) {

                throw new ReglaNegocioException(
                    "Ya existe una instantanea para esa fecha y disciplina"
                );
            }
        }

        // ALMACENAR ELO DE ESA FECHA

        historialRankings.add(
            new RegistroRanking(
                fecha,
                socio.getLogin(),
                disciplina.getNombre(),
                puntosActuales
            )
        );
    }

    // NUEVO: CONSULTAR HISTORIAL COMPLETO

    public List<RegistroRanking> getHistorialRankings() {

        return Collections.unmodifiableList(
            historialRankings
        );
    }

    // NUEVO: RESTAURAR HISTORIAL SIN RECALCULAR ELO

    public void restaurarHistorialRankings(
            List<RegistroRanking> registros)
            throws ReglaNegocioException {

        if (registros == null ||
            !historialRankings.isEmpty()) {

            throw new ReglaNegocioException(
                "El historial de ranking no puede restaurarse dos veces"
            );
        }

        Set<String> claves = new HashSet<>();

        // VALIDAR ANTES DE MODIFICAR EL HISTORIAL

        for (RegistroRanking registro : registros) {

            if (registro == null ||
                registro.getFecha() == null ||
                registro.getLoginSocio() == null ||
                registro.getLoginSocio().isBlank() ||
                registro.getNombreDisciplina() == null ||
                registro.getNombreDisciplina().isBlank() ||
                !Double.isFinite(registro.getPuntos()) ||
                registro.getPuntos() < 0) {

                throw new ReglaNegocioException(
                    "El archivo contiene un ranking invalido"
                );
            }

            String clave =
                registro.getFecha() + "::" +

                registro.getLoginSocio()
                    .trim()
                    .toLowerCase(Locale.ROOT) + "::" +

                registro.getNombreDisciplina()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (!claves.add(clave)) {

                throw new ReglaNegocioException(
                    "El archivo contiene instantaneas duplicadas"
                );
            }
        }

        // RESTAURAR LOS VALORES HISTORICOS ORIGINALES

        historialRankings.addAll(registros);
    }

    // CONSULTAR EVOLUCION DEL RANKING

    public List<RegistroRanking> evolucionRanking(
            Administrador administrador,
            Socio socio,
            Disciplina disciplina,
            LocalDate inicio,
            LocalDate fin)
            throws ReglaNegocioException {

        validarAdministrador(administrador);
        validarFechas(inicio, fin);

        if (socio == null ||
            disciplina == null ||
            socio.getLogin() == null ||
            disciplina.getNombre() == null) {

            throw new ReglaNegocioException(
                "Socio o disciplina invalidos"
            );
        }

        List<RegistroRanking> resultado =
            new ArrayList<>();

        // FILTRAR HISTORIAL POR SOCIO Y DISCIPLINA

        for (RegistroRanking registro : historialRankings) {

            if (registro.getLoginSocio()
                    .equalsIgnoreCase(socio.getLogin()) &&

                registro.getNombreDisciplina()
                    .equalsIgnoreCase(
                        disciplina.getNombre()) &&

                enRango(
                    registro.getFecha(),
                    inicio,
                    fin)) {

                resultado.add(registro);
            }
        }

        // ORDENAR POR FECHA

        resultado.sort(
            Comparator.comparing(
                RegistroRanking::getFecha
            )
        );

        return Collections.unmodifiableList(resultado);
    }
}
