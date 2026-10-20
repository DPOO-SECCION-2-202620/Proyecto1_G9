
package uniandes.dpoo.cds.consola;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;

import uniandes.dpoo.cds.aplicacion.FaseASCUN;
import uniandes.dpoo.cds.aplicacion.SistemaClub;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.administracion.SolicitudCambioTurno;
import uniandes.dpoo.cds.modelo.administracion.TurnoPersonal;

import uniandes.dpoo.cds.modelo.competencias.Competencia;

import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Entrenador;
import uniandes.dpoo.cds.modelo.usuarios.Fisioterapeuta;

import uniandes.dpoo.cds.persistencia.PersistenciaSistema;

public class PruebaPersistenciaOperativa {

    private static int correctas;
    private static int fallidas;

    // COMPROBAR RESULTADOS

    private static void comprobar(
            String nombre,
            boolean cumple) {

        System.out.println(
            (cumple ? "[OK] " : "[FALLO] ") + nombre
        );

        if (cumple) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    // CREAR SISTEMA NUEVO

    private static SistemaClub nuevo() {

        return new SistemaClub(
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    // CARGAR SISTEMA GUARDADO

    private static SistemaClub cargar(Path ruta)
            throws Exception {

        return PersistenciaSistema.cargarSistema(
            ruta,
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            EnumSet.of(DayOfWeek.MONDAY)
        );
    }

    // PRUEBA PRINCIPAL

    public static void main(String[] args)
            throws Exception {

        Path ruta = Path.of(
            "datos",
            "sistema-operativo-prueba.dat"
        );

        // CREAR SISTEMA Y USUARIOS

        SistemaClub original = nuevo();

        Administrador admin =
            new Administrador("adminOp", "demo");

        Entrenador e1 =
            new Entrenador("entOp1", "demo", "OP1");

        Entrenador e2 =
            new Entrenador("entOp2", "demo", "OP2");

        Entrenador e3 =
            new Entrenador("entOp3", "demo", "OP3");

        Fisioterapeuta fis =
            new Fisioterapeuta("fisOp", "demo", "OPF");

        LocalTime ocho = LocalTime.of(8, 0);
        LocalTime dieciocho = LocalTime.of(18, 0);

        // REGISTRAR TURNOS

        original.getTurnos().registrarTurno(
            new TurnoPersonal(
                e1,
                DayOfWeek.MONDAY,
                ocho,
                dieciocho
            )
        );

        original.getTurnos().registrarTurno(
            new TurnoPersonal(
                e2,
                DayOfWeek.MONDAY,
                ocho,
                dieciocho
            )
        );

        original.getTurnos().registrarTurno(
            new TurnoPersonal(
                e3,
                DayOfWeek.MONDAY,
                ocho,
                dieciocho
            )
        );

        original.getTurnos().registrarTurno(
            new TurnoPersonal(
                fis,
                DayOfWeek.MONDAY,
                ocho,
                dieciocho
            )
        );

        // ACTIVAR PROGRAMACION

        original.getTurnos()
            .activarProgramacion();

        // CREAR SOLICITUD VALIDA

        SolicitudCambioTurno solicitud =
            original.getTurnos().solicitarCambio(
                e1,
                original.getTurnos()
                    .getTurnos().get(0),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                dieciocho,
                "Ajuste valido"
            );

        // CREAR SOLICITUD QUE AFECTARIA LA COBERTURA

        SolicitudCambioTurno solicitudFis =
            original.getTurnos().solicitarCambio(
                fis,
                original.getTurnos()
                    .getTurnos().get(3),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                dieciocho,
                "Ajuste sin cobertura"
            );

        // CREAR COMPETENCIA ASCUN

        Competencia competencia = new Competencia(
            "ASCUN Futbol",
            "ASCUN",
            "2026-2",
            new Deporte("Futbol", 7),
            new Categoria("Mayores", 18, 99),
            "GRUPOS_Y_ELIMINACION",
            7
        );

        original.getCompetencias()
            .registrarCompetencia(competencia);

        // CREAR FASE ASCUN

        FaseASCUN fase =
            new FaseASCUN(competencia);

        original.registrarFaseASCUN(fase);

        // CREAR GRUPOS

        fase.crearGrupo(
            "A",
            "Seneca",
            "Andes"
        );

        fase.crearGrupo(
            "B",
            "Javeriana",
            "Rosario"
        );

        // REGISTRAR RESULTADOS DE GRUPO

        fase.registrarPartidoGrupo(
            "A",
            "Seneca",
            "Andes",
            2,
            0
        );

        fase.registrarPartidoGrupo(
            "B",
            "Javeriana",
            "Rosario",
            1,
            0
        );

        fase.cerrarGrupos();

        // REGISTRAR PRIMERA SEMIFINAL

        fase.registrarSemifinal(
            1,
            2,
            1
        );

        System.out.println(
            "PRUEBAS DE PERSISTENCIA DE ASCUN Y TURNOS"
        );

        System.out.println(
            "-------------------------------------------"
        );

        // VALIDAR ESTADO ORIGINAL

        comprobar(
            "Programacion original activa y con cuatro turnos",

            original.getTurnos().isProgramacionActiva() &&

            original.getTurnos().getTurnos().size() == 4
        );

        comprobar(
            "Dos solicitudes originales pendientes",

            original.getTurnos().getSolicitudes().size() == 2 &&

            original.getTurnos().getSolicitudes().stream()
                .allMatch(
                    s -> s.getEstado() ==
                        SolicitudCambioTurno.Estado.PENDIENTE
                )
        );

        comprobar(
            "ASCUN tiene grupos cerrados y primera semifinal",

            fase.tablaGrupo("A")
                .get(0)
                .getEquipo()
                .equals("Seneca") &&

            fase.descripcionSemifinal(2)
                .equals("Javeriana vs Andes")
        );

        // GUARDAR ESTADO

        PersistenciaSistema.guardar(
            original,
            ruta
        );

        comprobar(
            "Archivo de prueba creado",

            Files.isRegularFile(ruta)
        );

        // PRIMER REINICIO

        SistemaClub recuperado =
            cargar(ruta);

        comprobar(
            "Programacion activa y turnos recuperados",

            recuperado.getTurnos()
                .isProgramacionActiva() &&

            recuperado.getTurnos()
                .getTurnos().size() == 4 &&

            recuperado.getTurnos()
                .tieneCoberturaMinima()
        );

        comprobar(
            "Solicitudes pendientes recuperadas con sus numeros",

            recuperado.getTurnos()
                .getSolicitudes().size() == 2 &&

            recuperado.getTurnos()
                .getSolicitudes().get(0)
                .getNumero() == solicitud.getNumero() &&

            recuperado.getTurnos()
                .getSolicitudes().get(1)
                .getNumero() == solicitudFis.getNumero()
        );

        comprobar(
            "ASCUN restaurada pertenece a competencia registrada",

            recuperado.getFasesASCUN().size() == 1 &&

            recuperado.getFasesASCUN()
                .get(0)
                .getCompetencia() ==

            recuperado.getCompetencias()
                .getCompetencias().get(0)
        );

        FaseASCUN cargada =
            recuperado.getFasesASCUN().get(0);

        comprobar(
            "Grupos, tablas y semifinal pendiente recuperados",

            cargada.tablaGrupo("A")
                .get(0)
                .getPuntos() == 3 &&

            cargada.descripcionSemifinal(2)
                .equals("Javeriana vs Andes")
        );

        comprobar(
            "No existe campeon antes de jugar la final",

            cargada.getCampeon() == null
        );

        // APROBAR CAMBIO VALIDO

        recuperado.getTurnos()
            .aprobarCambio(
                admin,
                solicitud.getNumero()
            );

        comprobar(
            "Cambio aprobado mantiene cobertura y actualiza horario",

            recuperado.getTurnos()
                .tieneCoberturaMinima() &&

            recuperado.getTurnos()
                .getTurnos().stream()
                .anyMatch(
                    t -> t.getEmpleado()
                        .getLogin()
                        .equals("entOp1") &&

                    t.getInicio().equals(
                        LocalTime.of(9, 0)
                    )
                )
        );

        // COMPROBAR CAMBIO SIN COBERTURA

        boolean sinCobertura = false;

        try {

            recuperado.getTurnos()
                .aprobarCambio(
                    admin,
                    solicitudFis.getNumero()
                );

        } catch (ReglaNegocioException esperado) {

            sinCobertura = true;
        }

        comprobar(
            "Se rechaza aprobacion que deja sin fisioterapeuta",

            sinCobertura
        );

        // RECHAZAR CAMBIO Y GUARDAR MOTIVO

        recuperado.getTurnos()
            .rechazarCambio(
                admin,
                solicitudFis.getNumero(),
                "Sin cobertura a las 8"
            );

        comprobar(
            "El rechazo queda registrado",

            recuperado.getTurnos()
                .getSolicitudes().get(1)
                .getEstado() ==

            SolicitudCambioTurno.Estado.RECHAZADA
        );

        // FINALIZAR TORNEO ASCUN RESTAURADO

        cargada.registrarSemifinal(
            2,
            1,
            0
        );

        comprobar(
            "Final disponible despues de restaurar ASCUN",

            cargada.descripcionFinal()
                .equals("Seneca vs Javeriana")
        );

        cargada.registrarFinal(
            2,
            0
        );

        comprobar(
            "El torneo restaurado produce un campeon",

            cargada.getCampeon()
                .equals("Seneca")
        );

        // SEGUNDO GUARDADO

        PersistenciaSistema.guardar(
            recuperado,
            ruta
        );

        // SEGUNDO REINICIO

        SistemaClub tercera =
            cargar(ruta);

        comprobar(
            "Segundo reinicio conserva al campeon",

            tercera.getFasesASCUN()
                .get(0)
                .getCampeon()
                .equals("Seneca")
        );

        comprobar(
            "Segundo reinicio conserva turnos y cambios",

            tercera.getTurnos()
                .isProgramacionActiva() &&

            tercera.getTurnos()
                .getSolicitudes().get(0)
                .getEstado() ==
                SolicitudCambioTurno.Estado.APROBADA &&

            tercera.getTurnos()
                .getSolicitudes().get(1)
                .getEstado() ==
                SolicitudCambioTurno.Estado.RECHAZADA
        );

        // COMPROBAR CONSECUTIVO DESPUES DE REINICIAR

        comprobar(
            "Numeracion de solicitudes continua despues de cargar",

            tercera.getTurnos()
                .solicitarCambio(
                    e2,

                    tercera.getTurnos()
                        .getTurnos().stream()
                        .filter(
                            t -> t.getEmpleado()
                                .getLogin()
                                .equals("entOp2")
                        )
                        .findFirst()
                        .orElseThrow(),

                    DayOfWeek.MONDAY,
                    LocalTime.of(9, 0),
                    dieciocho,
                    "Nueva solicitud"
                )
                .getNumero() == 3
        );

        // RESULTADOS FINALES

        System.out.println(
            "-------------------------------------------"
        );

        System.out.println(
            "Pruebas correctas: " + correctas
        );

        System.out.println(
            "Pruebas fallidas: " + fallidas
        );

        System.out.println(
            "Campeon ASCUN: "
            + tercera.getFasesASCUN()
                .get(0)
                .getCampeon()
        );

        System.out.println(
            "Solicitudes conservadas: "
            + tercera.getTurnos()
                .getSolicitudes().size()
        );
    }
}
