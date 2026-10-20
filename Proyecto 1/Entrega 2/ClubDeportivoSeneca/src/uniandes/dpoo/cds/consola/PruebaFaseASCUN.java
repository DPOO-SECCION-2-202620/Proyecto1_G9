
package uniandes.dpoo.cds.consola;

import java.util.List;

import uniandes.dpoo.cds.aplicacion.FaseASCUN;
import uniandes.dpoo.cds.excepciones.ReglaNegocioException;
import uniandes.dpoo.cds.modelo.competencias.Competencia;
import uniandes.dpoo.cds.modelo.conjunto.Categoria;
import uniandes.dpoo.cds.modelo.conjunto.Deporte;

public class PruebaFaseASCUN {

    private static int correctas = 0;
    private static int fallidas = 0;

    @FunctionalInterface
    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void aceptar(
            String nombre,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println("[OK] " + nombre);
            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + nombre
                + " -> " + e.getMessage()
            );

            fallidas++;
        }
    }

    private static void rechazar(
            String nombre,
            Accion accion) {

        try {
            accion.ejecutar();

            System.out.println(
                "[FALLO] " + nombre + " fue aceptado"
            );

            fallidas++;

        } catch (ReglaNegocioException e) {

            System.out.println(
                "[OK] " + nombre
                + " -> " + e.getMessage()
            );

            correctas++;

        } catch (Exception e) {

            System.out.println(
                "[FALLO] " + nombre
                + " -> " + e.getMessage()
            );

            fallidas++;
        }
    }

    private static void verificar(
            String nombre,
            boolean cumple) {

        System.out.println(
            (cumple ? "[OK] " : "[FALLO] ")
            + nombre
        );

        if (cumple) {
            correctas++;
        } else {
            fallidas++;
        }
    }

    public static void main(String[] args)
            throws Exception {

        // CREAR COMPETENCIA ASCUN

        Competencia ascun = new Competencia(
            "ASCUN Futbol",
            "ASCUN",
            "2026-2",
            new Deporte("Futbol", 7),
            new Categoria("Mayores", 18, 99),
            "GRUPOS_Y_ELIMINACION",
            7
        );

        FaseASCUN torneo = new FaseASCUN(ascun);

        System.out.println(
            "PRUEBAS FASE DE GRUPOS Y ELIMINACION ASCUN"
        );

        System.out.println(
            "-------------------------------------------"
        );

        // CREAR GRUPOS

        aceptar("Crear Grupo A", () ->
            torneo.crearGrupo(
                "A",
                "Seneca",
                "Andes",
                "Nacional"
            )
        );

        rechazar("No repetir equipos entre grupos", () ->
            torneo.crearGrupo(
                "B",
                "Seneca",
                "Rosario"
            )
        );

        aceptar("Crear Grupo B", () ->
            torneo.crearGrupo(
                "B",
                "Javeriana",
                "Rosario",
                "Externado"
            )
        );

        // VALIDACIONES INICIALES

        rechazar("No cerrar grupos incompletos", () ->
            torneo.cerrarGrupos()
        );

        rechazar("Equipo ajeno al grupo", () ->
            torneo.registrarPartidoGrupo(
                "A", "Seneca", "Javeriana", 1, 0
            )
        );

        rechazar("Marcador negativo", () ->
            torneo.registrarPartidoGrupo(
                "A", "Seneca", "Andes", -1, 0
            )
        );

        // PARTIDOS DEL GRUPO A

        aceptar("Grupo A: Seneca gana a Andes", () ->
            torneo.registrarPartidoGrupo(
                "A", "Seneca", "Andes", 1, 0
            )
        );

        rechazar(
            "Partido repetido aunque cambien localia",
            () -> torneo.registrarPartidoGrupo(
                "A", "Andes", "Seneca", 2, 0
            )
        );

        aceptar("Grupo A: Seneca gana a Nacional", () ->
            torneo.registrarPartidoGrupo(
                "A", "Seneca", "Nacional", 1, 0
            )
        );

        aceptar("Grupo A: Andes gana a Nacional", () ->
            torneo.registrarPartidoGrupo(
                "A", "Andes", "Nacional", 3, 0
            )
        );

        // PARTIDOS DEL GRUPO B

        aceptar("Grupo B: Javeriana vence Rosario", () ->
            torneo.registrarPartidoGrupo(
                "B", "Javeriana", "Rosario", 2, 0
            )
        );

        aceptar("Grupo B: Externado vence Javeriana", () ->
            torneo.registrarPartidoGrupo(
                "B", "Javeriana", "Externado", 0, 2
            )
        );

        aceptar("Grupo B: Rosario vence Externado", () ->
            torneo.registrarPartidoGrupo(
                "B", "Rosario", "Externado", 5, 0
            )
        );

        // VERIFICAR TABLAS

        List<FaseASCUN.Fila> tablaA =
            torneo.tablaGrupo("A");

        verificar(
            "Seneca lidera A con 6 puntos",
            tablaA.get(0).getEquipo().equals("Seneca") &&
            tablaA.get(0).getPuntos() == 6
        );

        verificar(
            "Andes segundo en A",
            tablaA.get(1).getEquipo().equals("Andes") &&
            tablaA.get(1).getPuntos() == 3
        );

        List<FaseASCUN.Fila> tablaB =
            torneo.tablaGrupo("B");

        verificar(
            "Desempate directo en B deja primero a Rosario",

            tablaB.get(0).getEquipo().equals("Rosario") &&
            tablaB.get(1).getEquipo().equals("Javeriana")
        );

        // CERRAR FASE DE GRUPOS

        aceptar("Cerrar los dos grupos completos", () ->
            torneo.cerrarGrupos()
        );

        rechazar(
            "No agregar encuentros despues del cierre",
            () -> torneo.registrarPartidoGrupo(
                "A", "Seneca", "Andes", 0, 0
            )
        );

        // VERIFICAR SEMIFINALES

        verificar(
            "Cruce de semifinal 1 correcto",

            torneo.descripcionSemifinal(1)
                .equals("Seneca vs Javeriana")
        );

        verificar(
            "Cruce de semifinal 2 correcto",

            torneo.descripcionSemifinal(2)
                .equals("Rosario vs Andes")
        );

        // VALIDAR SEMIFINALES

        rechazar("No empates en semifinal", () ->
            torneo.registrarSemifinal(1, 1, 1)
        );

        rechazar("No final sin semifinales", () ->
            torneo.registrarFinal(1, 0)
        );

        // REGISTRAR SEMIFINALES

        aceptar("Semifinal 1 gana Seneca", () ->
            torneo.registrarSemifinal(1, 2, 1)
        );

        aceptar("Semifinal 2 gana Andes", () ->
            torneo.registrarSemifinal(2, 1, 2)
        );

        rechazar("No repetir semifinal", () ->
            torneo.registrarSemifinal(1, 2, 0)
        );

        // VERIFICAR FINAL

        verificar(
            "Final Seneca vs Andes",

            torneo.descripcionFinal()
                .equals("Seneca vs Andes")
        );

        // VALIDAR RESULTADO DE FINAL

        rechazar("Final no permite empate directo", () ->
            torneo.registrarFinal(1, 1)
        );

        aceptar("Registrar final ganada por Seneca", () ->
            torneo.registrarFinal(1, 0)
        );

        // VERIFICAR CAMPEON

        verificar(
            "Campeon del torneo: Seneca",

            "Seneca".equals(torneo.getCampeon())
        );

        rechazar("No repetir final", () ->
            torneo.registrarFinal(2, 0)
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
            "Campeon: " + torneo.getCampeon()
        );
    }
}
