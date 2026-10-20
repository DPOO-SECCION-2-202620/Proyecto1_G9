
package uniandes.dpoo.cds.aplicacion;

import java.time.LocalDate;

import uniandes.dpoo.cds.modelo.entrenamientos.Asistencia;
import uniandes.dpoo.cds.modelo.entrenamientos.EstadoAsistencia;
import uniandes.dpoo.cds.modelo.entrenamientos.SesionEntrenamiento;
import uniandes.dpoo.cds.modelo.competencias.PartidoCompetencia;

public class ResumenDeportivo {

    private final LocalDate inicio;
    private final LocalDate fin;

    private int sesionesProgramadas;
    private int sesionesFinalizadas;
    private int convocatorias;
    private int asistieron;
    private int llegaronTarde;
    private int faltaron;
    private int sinRegistrar;

    private int partidosInternosFinalizados;
    private int partidosOficialesFinalizados;
    private int partidosPracticaFinalizados;

    private int victoriasOficiales;
    private int empatesOficiales;
    private int derrotasOficiales;

    // CONSTRUCTOR

    ResumenDeportivo(LocalDate inicio, LocalDate fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    // CONTABILIZAR SESIONES Y ASISTENCIAS

    void contarSesion(SesionEntrenamiento sesion) {

        sesionesProgramadas++;

        if (sesion.isFinalizada()) {
            sesionesFinalizadas++;
        }

        convocatorias += sesion.getConvocados().size();

        int registrados = 0;

        for (Asistencia asistencia : sesion.getAsistencias()) {

            registrados++;

            if (asistencia.getEstado() ==
                EstadoAsistencia.ASISTIO) {

                asistieron++;

            } else if (asistencia.getEstado() ==
                       EstadoAsistencia.TARDE) {

                llegaronTarde++;

            } else if (asistencia.getEstado() ==
                       EstadoAsistencia.FALTO) {

                faltaron++;
            }
        }

        sinRegistrar +=
            sesion.getConvocados().size() - registrados;
    }

    // CONTABILIZAR PARTIDOS INTERNOS

    void contarInterno() {
        partidosInternosFinalizados++;
    }

    // CONTABILIZAR PARTIDOS INDIVIDUALES

    void contarPractica() {
        partidosPracticaFinalizados++;
    }

    // CONTABILIZAR PARTIDOS OFICIALES

    void contarOficial(PartidoCompetencia partido) {

        partidosOficialesFinalizados++;

        if (partido.getMarcadorClub() >
            partido.getMarcadorRival()) {

            victoriasOficiales++;

        } else if (partido.getMarcadorClub()
                .equals(partido.getMarcadorRival())) {

            empatesOficiales++;

        } else {

            derrotasOficiales++;
        }
    }

    // GETTERS

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    public int getSesionesProgramadas() {
        return sesionesProgramadas;
    }

    public int getSesionesFinalizadas() {
        return sesionesFinalizadas;
    }

    public int getConvocatorias() {
        return convocatorias;
    }

    public int getAsistieron() {
        return asistieron;
    }

    public int getLlegaronTarde() {
        return llegaronTarde;
    }

    public int getFaltaron() {
        return faltaron;
    }

    public int getSinRegistrar() {
        return sinRegistrar;
    }

    public int getPartidosInternosFinalizados() {
        return partidosInternosFinalizados;
    }

    public int getPartidosOficialesFinalizados() {
        return partidosOficialesFinalizados;
    }

    public int getPartidosPracticaFinalizados() {
        return partidosPracticaFinalizados;
    }

    public int getVictoriasOficiales() {
        return victoriasOficiales;
    }

    public int getEmpatesOficiales() {
        return empatesOficiales;
    }

    public int getDerrotasOficiales() {
        return derrotasOficiales;
    }

    public int getTotalPartidosFinalizados() {

        return partidosInternosFinalizados
            + partidosOficialesFinalizados
            + partidosPracticaFinalizados;
    }

    public double getPorcentajeAsistencia() {

        if (convocatorias == 0) {
            return 0;
        }

        return 100.0 *
            (asistieron + llegaronTarde) /
            convocatorias;
    }
}
