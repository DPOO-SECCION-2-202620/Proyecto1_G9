
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;

import java.util.EnumSet;
import java.util.Map;

import uniandes.dpoo.cds.aplicacion.ResumenDeportivo;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.modelo.comercial.Articulo;

import uniandes.dpoo.cds.modelo.entrenamientos.EstadoAsistencia;
import uniandes.dpoo.cds.modelo.entrenamientos.SesionEntrenamiento;

import uniandes.dpoo.cds.modelo.individuales.Disciplina;
import uniandes.dpoo.cds.modelo.individuales.Instalacion;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Socio;

import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class PruebaPersistenciaEntrenamientos {

    private static int correctas = 0;
    private static int fallidas = 0;

    // VERIFICAR RESULTADOS

    private static void verificar(
            String texto,
            boolean ok) {

        System.out.println(
            (ok ? "[OK] " : "[FALLO] ") + texto
        );

        if (ok) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // PROGRAMA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        // ARCHIVO GENERAL DE PRUEBA

        Path archivo = Path.of(
            "datos",
            "sistema-entrenamientos-prueba.dat"
        );

        // CONFIGURAR HORARIO

        LocalTime apertura = LocalTime.of(8, 0);
        LocalTime cierre = LocalTime.of(18, 0);

        EnumSet<DayOfWeek> dias =
            EnumSet.of(DayOfWeek.MONDAY);

        LocalDate fecha =
            LocalDate.of(2026, 11, 5);

        // CREAR USUARIOS

        Socio ana = new Socio(
            "anaDep",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Socio luis = new Socio(
            "luisDep",
            "demo",
            LocalDate.of(2000, 1, 1)
        );

        Entrenador profe = new Entrenador(
            "entDep",
            "demo",
            "D-01"
        );

        Administrador admin = new Administrador(
            "adminDep",
            "demo"
        );

        Empleado empleado = new Empleado(
            "empleadoDep",
            "demo",
            "E-01"
        );

        // CREAR INSTALACION

        Instalacion cancha = new Instalacion(
            "TEN-D",
            new Disciplina("Tenis", 2),
            4,
            false
        );

        // CREAR SISTEMA ORIGINAL

        SistemaClub primero = new SistemaClub(
            apertura,
            cierre,
            dias
        );

        // PROGRAMAR ENTRENAMIENTO

        SesionEntrenamiento sesion =
            new SesionEntrenamiento(
                fecha,
                LocalTime.of(10, 0),
                90,
                profe,
                cancha
            );

        primero.getEntrenamientos()
            .programarSesion(sesion);

        // CONVOCAR SOCIOS

        sesion.convocarSocio(ana);
        sesion.convocarSocio(luis);

        // FINALIZAR Y REGISTRAR ASISTENCIAS

        sesion.finalizarSesion(profe);

        sesion.registrarAsistencia(
            profe,
            ana,
            EstadoAsistencia.ASISTIO
        );

        sesion.registrarAsistencia(
            profe,
            luis,
            EstadoAsistencia.TARDE
        );

        // EVALUAR A ANA

        sesion.registrarEvaluacion(
            profe,
            ana,
            5,
            "Buen rendimiento"
        );

        // REGISTRAR DATOS COMERCIALES

        primero.getTienda().registrarArticulo(
            new Articulo(
                "BAL-D",
                "Balon",
                "Balones",
                20000
            ),
            3
        );

        primero.getTienda().vender(
            ana,
            Map.of("BAL-D", 1),
            null,
            0
        );

        primero.getReportesFinancieros()
            .registrarMensualidad(
                ana,
                YearMonth.of(2026, 11),
                fecha,
                100000
            );

        // CREAR SUGERENCIA PENDIENTE

        int sugerenciaId =
            primero.getSugerencias()
                .proponerArticulo(
                    empleado,
                    new Articulo(
                        "MED-D",
                        "Medias",
                        "Ropa",
                        10000
                    ),
                    "Ampliar catalogo"
                ).getNumero();

        System.out.println(
            "PRUEBAS DE PERSISTENCIA DE ENTRENAMIENTOS"
        );

        System.out.println(
            "--------------------------------------------"
        );

        // VALIDAR ANTES DE GUARDAR

        verificar(
            "Sesion original finalizada",
            sesion.isFinalizada()
        );

        verificar(
            "Dos convocados y dos asistencias",

            sesion.getConvocados().size() == 2 &&

            sesion.getAsistencias().size() == 2
        );

        // GUARDAR ESTADO

        PersistenciaSistema.guardar(
            primero,
            archivo
        );

        verificar(
            "Archivo general creado",
            Files.isRegularFile(archivo)
        );

        // PRIMER REINICIO

        SistemaClub segundo =
            PersistenciaSistema.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        verificar(
            "Se recupera una sesion",

            segundo.getEntrenamientos()
                .getSesiones().size() == 1
        );

        // CONSULTAR SESION RESTAURADA

        SesionEntrenamiento restaurada =
            segundo.getEntrenamientos()
                .getSesiones().get(0);

        verificar(
            "Se recuperan fecha, horario y duracion",

            restaurada.getFecha().equals(fecha) &&

            restaurada.getHora().equals(
                LocalTime.of(10, 0)
            ) &&

            restaurada.getDuracionMinutos() == 90
        );

        verificar(
            "Entrenador e instalacion conservados",

            restaurada.getEntrenador()
                .getLogin().equals("entDep") &&

            restaurada.getInstalacion()
                .getCodigo().equals("TEN-D")
        );

        verificar(
            "Sesion sigue finalizada",
            restaurada.isFinalizada()
        );

        verificar(
            "Se recuperan las asistencias",

            restaurada.getAsistencias().size() == 2 &&

            restaurada.getAsistencias().get(0)
                .getEstado() == EstadoAsistencia.ASISTIO &&

            restaurada.getAsistencias().get(1)
                .getEstado() == EstadoAsistencia.TARDE
        );

        verificar(
            "Evaluacion y comentario se conservan",

            restaurada.getAsistencias().get(0)
                .getEvaluacion().getNota() == 5 &&

            restaurada.getAsistencias().get(0)
                .getEvaluacion().getComentario()
                .equals("Buen rendimiento")
        );

        // CONSULTAR REPORTE DEPORTIVO

        ResumenDeportivo resumen =
            segundo.getReportesDeportivos()
                .generarResumen(
                    admin,
                    fecha,
                    fecha
                );

        verificar(
            "Reporte deportivo usa sesion restaurada",

            resumen.getSesionesFinalizadas() == 1 &&

            resumen.getConvocatorias() == 2 &&

            resumen.getPorcentajeAsistencia() == 100.0
        );

        // COMPROBAR DATOS COMERCIALES

        verificar(
            "Venta e inventario comercial conservados",

            segundo.getTienda()
                .getVentas().size() == 1 &&

            segundo.getTienda()
                .getExistencias("BAL-D") == 2
        );

        verificar(
            "Mensualidad y puntos conservados",

            segundo.getReportesFinancieros()
                .getMensualidades().size() == 1 &&

            Math.abs(
                segundo.getFidelidad().getSaldo(ana) - 476
            ) < 0.001
        );

        verificar(
            "Sugerencia pendiente conservada",

            segundo.getSugerencias()
                .getPendientes().size() == 1
        );

        // APROBAR SUGERENCIA RESTAURADA

        segundo.getSugerencias().aprobar(
            admin,
            sugerenciaId,
            "Aprobado"
        );

        verificar(
            "Sugerencia restaurada sigue operativa",

            segundo.getTienda()
                .getExistencias("MED-D") == 0
        );

        // PROGRAMAR OTRA SESION

        SesionEntrenamiento nueva =
            new SesionEntrenamiento(
                fecha.plusDays(1),
                LocalTime.of(10, 0),
                60,
                profe,
                cancha
            );

        segundo.getEntrenamientos()
            .programarSesion(nueva);

        verificar(
            "Se pueden programar nuevas sesiones tras cargar",

            segundo.getEntrenamientos()
                .getSesiones().size() == 2
        );

        // SEGUNDO GUARDADO

        PersistenciaSistema.guardar(
            segundo,
            archivo
        );

        // SEGUNDO REINICIO

        SistemaClub tercero =
            PersistenciaSistema.cargarSistema(
                archivo,
                apertura,
                cierre,
                dias
            );

        verificar(
            "Segundo reinicio conserva ambas sesiones",

            tercero.getEntrenamientos()
                .getSesiones().size() == 2
        );

        verificar(
            "Segundo reinicio conserva evaluaciones y datos comerciales",

            tercero.getEntrenamientos()
                .getSesiones().get(0)
                .getAsistencias().get(0)
                .getEvaluacion().getNota() == 5 &&

            tercero.getTienda()
                .getExistencias("MED-D") == 0 &&

            tercero.getSugerencias()
                .getPendientes().isEmpty()
        );

        // RESULTADOS FINALES

        System.out.println(
            "--------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Sesiones recuperadas: "
            + tercero.getEntrenamientos()
                .getSesiones().size()
        );
    }
}
