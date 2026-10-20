
package uniandes.dpoo.cds.consola;

import java.time.LocalDate;
import java.time.LocalTime;

import uniandes.dpoo.cds.aplicacion.GestorEntrenamientos;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.entrenamientos.Asistencia;
import uniandes.dpoo.cds.modelo.entrenamientos.EstadoAsistencia;
import uniandes.dpoo.cds.modelo.entrenamientos.SesionEntrenamiento;
import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

public class PruebaEntrenamientos {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void aceptar(
            String caso,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + caso);
            correctas++;

        } catch (Exception e) {
            System.out.println(
                "[FALLO] " + caso + " -> " + e
            );
            fallidas++;
        }
    }

    private static void rechazar(
            String caso,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + caso + " fue aceptado"
            );

            fallidas++;

        } catch (ReglaNegocioException e) {
            System.out.println(
                "[OK] " + caso + " -> " + e.getMessage()
            );
            correctas++;

        } catch (Exception e) {
            System.out.println(
                "[FALLO] " + caso + " -> " + e
            );
            fallidas++;
        }
    }

    private static void verificar(
            String caso,
            boolean condicion) {

        System.out.println(
            (condicion ? "[OK] " : "[FALLO] ") + caso
        );

        if (condicion) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    public static void main(String[] args)
            throws Exception {

        GestorEntrenamientos gestor =
            new GestorEntrenamientos();

        Disciplina tenis =
            new Disciplina("Tenis", 2);

        Instalacion cancha1 =
            new Instalacion(
                "TEN-01", tenis, 4, false
            );

        Instalacion cancha2 =
            new Instalacion(
                "TEN-02", tenis, 4, false
            );

        Entrenador profe =
            new Entrenador(
                "profe01", "demo", "DES1"
            );

        Entrenador otro =
            new Entrenador(
                "profe02", "demo", "DES2"
            );

        Socio ana = new Socio(
            "ana01",
            "demo",
            LocalDate.of(2003, 1, 1)
        );

        Socio juan = new Socio(
            "juan01",
            "demo",
            LocalDate.of(2002, 5, 2)
        );

        Socio noConvocado = new Socio(
            "pedro01",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        LocalDate fecha =
            LocalDate.of(2026, 11, 25);

        SesionEntrenamiento sesion =
            new SesionEntrenamiento(
                fecha,
                LocalTime.of(10, 0),
                90,
                profe,
                cancha1
            );

        System.out.println(
            "PRUEBAS DE ENTRENAMIENTOS"
        );

        System.out.println(
            "---------------------------------"
        );

        // PROGRAMACION

        rechazar("Sesion nula", () ->
            gestor.programarSesion(null)
        );

        aceptar("Programar sesion", () ->
            gestor.programarSesion(sesion)
        );

        rechazar("Conflicto en la misma cancha", () ->
            gestor.programarSesion(
                new SesionEntrenamiento(
                    fecha,
                    LocalTime.of(10, 30),
                    60,
                    otro,
                    cancha1
                )
            )
        );

        rechazar("Conflicto de entrenador", () ->
            gestor.programarSesion(
                new SesionEntrenamiento(
                    fecha,
                    LocalTime.of(10, 45),
                    60,
                    profe,
                    cancha2
                )
            )
        );

        aceptar("Sesion consecutiva del entrenador", () ->
            gestor.programarSesion(
                new SesionEntrenamiento(
                    fecha,
                    LocalTime.of(11, 30),
                    60,
                    profe,
                    cancha2
                )
            )
        );

        // CONVOCATORIAS

        aceptar("Convocar a Ana", () ->
            sesion.convocarSocio(ana)
        );

        aceptar("Convocar a Juan", () ->
            sesion.convocarSocio(juan)
        );

        rechazar("No duplicar convocatoria", () ->
            sesion.convocarSocio(
                new Socio(
                    "ANA01",
                    "demo",
                    LocalDate.of(2003, 1, 1)
                )
            )
        );

        // CONTROL DE SESION

        rechazar(
            "No registrar asistencia antes de finalizar",
            () -> sesion.registrarAsistencia(
                profe,
                ana,
                EstadoAsistencia.ASISTIO
            )
        );

        rechazar(
            "Otro entrenador no puede finalizar",
            () -> sesion.finalizarSesion(otro)
        );

        aceptar("Finalizar sesion", () ->
            sesion.finalizarSesion(profe)
        );

        rechazar(
            "No convocar despues de finalizar",
            () -> sesion.convocarSocio(noConvocado)
        );

        // ASISTENCIAS

        rechazar(
            "Rechazar socio no convocado",
            () -> sesion.registrarAsistencia(
                profe,
                noConvocado,
                EstadoAsistencia.ASISTIO
            )
        );

        rechazar(
            "Otro entrenador no registra asistencia",
            () -> sesion.registrarAsistencia(
                otro,
                ana,
                EstadoAsistencia.ASISTIO
            )
        );

        aceptar("Registrar asistencia de Ana", () ->
            sesion.registrarAsistencia(
                profe,
                ana,
                EstadoAsistencia.ASISTIO
            )
        );

        aceptar("Registrar llegada tarde de Juan", () ->
            sesion.registrarAsistencia(
                profe,
                juan,
                EstadoAsistencia.TARDE
            )
        );

        rechazar("No duplicar asistencia", () ->
            sesion.registrarAsistencia(
                profe,
                ana,
                EstadoAsistencia.ASISTIO
            )
        );

        // EVALUACIONES

        rechazar("Evaluacion con nota invalida", () ->
            sesion.registrarEvaluacion(
                profe,
                ana,
                6,
                "Bien"
            )
        );

        aceptar("Registrar evaluacion de Ana", () ->
            sesion.registrarEvaluacion(
                profe,
                ana,
                5,
                "Muy buen desempeño"
            )
        );

        rechazar("No duplicar evaluacion", () ->
            sesion.registrarEvaluacion(
                profe,
                ana,
                4,
                "Otra"
            )
        );

        rechazar("No evaluar sin asistencia", () ->
            sesion.registrarEvaluacion(
                profe,
                noConvocado,
                3,
                "Otra"
            )
        );

        // VERIFICACIONES FINALES

        verificar(
            "Dos convocados y dos asistencias",
            sesion.getConvocados().size() == 2 &&
            sesion.getAsistencias().size() == 2
        );

        Asistencia asistenciaAna =
            sesion.getAsistencias().get(0);

        verificar(
            "La evaluacion de Ana se conservo",
            asistenciaAna.getEvaluacion() != null &&
            asistenciaAna.getEvaluacion().getNota() == 5
        );

        verificar(
            "Dos sesiones programadas",
            gestor.getSesiones().size() == 2
        );

        // RESULTADOS

        System.out.println(
            "---------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Sesiones programadas: "
            + gestor.getSesiones().size()
        );
    }
}
