
package uniandes.dpoo.cds.aplicacion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uniandes.dpoo.cds.excepciones.ReglaNegocioException;

import uniandes.dpoo.cds.modelo.comercial.Articulo;
import uniandes.dpoo.cds.modelo.comercial.ProductoCafeteria;
import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto;

import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto.Area;
import uniandes.dpoo.cds.modelo.comercial.SugerenciaProducto.Estado;

import uniandes.dpoo.cds.modelo.usuarios.Administrador;
import uniandes.dpoo.cds.modelo.usuarios.Empleado;

public class GestorSugerenciasProducto {

    // ATRIBUTOS

    private final GestorTienda tienda;
    private final GestorCafeteria cafeteria;

    private final List<SugerenciaProducto> sugerencias =
        new ArrayList<>();

    private int siguienteNumero = 1;

    // CONSTRUCTOR

    public GestorSugerenciasProducto(
            GestorTienda tienda,
            GestorCafeteria cafeteria) {

        if (tienda == null || cafeteria == null) {

            throw new IllegalArgumentException(
                "La tienda y la cafeteria son obligatorias"
            );
        }

        this.tienda = tienda;
        this.cafeteria = cafeteria;
    }

    // VALIDAR EMPLEADO

    private void validarProponente(
            Empleado empleado,
            String justificacion)
            throws ReglaNegocioException {

        if (empleado == null ||
            empleado.getLogin() == null ||
            empleado.getLogin().isBlank()) {

            throw new ReglaNegocioException(
                "Solo un empleado puede proponer productos"
            );
        }

        if (justificacion == null ||
            justificacion.isBlank()) {

            throw new ReglaNegocioException(
                "La justificacion es obligatoria"
            );
        }
    }

    // EVITAR SUGERENCIAS DUPLICADAS

    private void verificarDuplicada(
            Area area,
            String codigo)
            throws ReglaNegocioException {

        for (SugerenciaProducto sugerencia : sugerencias) {

            if (sugerencia.getArea() == area &&
                sugerencia.getCodigoProducto()
                    .equalsIgnoreCase(codigo) &&
                sugerencia.getEstado() != Estado.RECHAZADA) {

                throw new ReglaNegocioException(
                    "Ya existe una sugerencia activa para ese producto"
                );
            }
        }
    }

    // PROPONER ARTICULO DE TIENDA

    public SugerenciaProducto proponerArticulo(
            Empleado empleado,
            Articulo articulo,
            String justificacion)
            throws ReglaNegocioException {

        validarProponente(
            empleado,
            justificacion
        );

        if (articulo == null) {

            throw new ReglaNegocioException(
                "El articulo es obligatorio"
            );
        }

        verificarDuplicada(
            Area.TIENDA,
            articulo.getCodigo()
        );

        SugerenciaProducto nueva =
            new SugerenciaProducto(
                siguienteNumero++,
                empleado,
                articulo,
                justificacion
            );

        sugerencias.add(nueva);

        return nueva;
    }

    // PROPONER PRODUCTO DE CAFETERIA

    public SugerenciaProducto proponerProductoCafeteria(
            Empleado empleado,
            ProductoCafeteria producto,
            String justificacion)
            throws ReglaNegocioException {

        validarProponente(
            empleado,
            justificacion
        );

        if (producto == null) {

            throw new ReglaNegocioException(
                "El producto de cafeteria es obligatorio"
            );
        }

        verificarDuplicada(
            Area.CAFETERIA,
            producto.getCodigo()
        );

        SugerenciaProducto nueva =
            new SugerenciaProducto(
                siguienteNumero++,
                empleado,
                producto,
                justificacion
            );

        sugerencias.add(nueva);

        return nueva;
    }

    // BUSCAR SUGERENCIA

    private SugerenciaProducto buscar(int numero)
            throws ReglaNegocioException {

        for (SugerenciaProducto sugerencia : sugerencias) {

            if (sugerencia.getNumero() == numero) {
                return sugerencia;
            }
        }

        throw new ReglaNegocioException(
            "Sugerencia no encontrada"
        );
    }

    // VALIDAR ADMINISTRADOR

    private void validarRevision(
            Administrador administrador,
            SugerenciaProducto sugerencia)
            throws ReglaNegocioException {

        if (administrador == null ||
            administrador.getLogin() == null ||
            administrador.getLogin().isBlank()) {

            throw new ReglaNegocioException(
                "Se requiere un administrador"
            );
        }

        if (sugerencia.getEstado() != Estado.PENDIENTE) {

            throw new ReglaNegocioException(
                "La sugerencia ya fue resuelta"
            );
        }
    }

    // APROBAR SUGERENCIA

    public void aprobar(
            Administrador administrador,
            int numero,
            String observacion)
            throws ReglaNegocioException {

        SugerenciaProducto sugerencia =
            buscar(numero);

        validarRevision(
            administrador,
            sugerencia
        );

        // REGISTRAR EL PRODUCTO

        if (sugerencia.getArea() == Area.TIENDA) {

            tienda.registrarArticulo(
                sugerencia.getArticulo(),
                0
            );

        } else {

            cafeteria.registrarProducto(
                sugerencia.getProductoCafeteria()
            );
        }

        // CAMBIAR ESTADO

        sugerencia.resolver(
            administrador,
            Estado.APROBADA,
            observacion
        );
    }

    // RECHAZAR SUGERENCIA

    public void rechazar(
            Administrador administrador,
            int numero,
            String motivo)
            throws ReglaNegocioException {

        SugerenciaProducto sugerencia =
            buscar(numero);

        validarRevision(
            administrador,
            sugerencia
        );

        sugerencia.resolver(
            administrador,
            Estado.RECHAZADA,
            motivo
        );
    }

    // CONSULTAR TODAS LAS SUGERENCIAS

    public List<SugerenciaProducto> getSugerencias() {

        return Collections.unmodifiableList(
            sugerencias
        );
    }

    // CONSULTAR SUGERENCIAS PENDIENTES

    public List<SugerenciaProducto> getPendientes() {

        List<SugerenciaProducto> pendientes =
            new ArrayList<>();

        for (SugerenciaProducto sugerencia : sugerencias) {

            if (sugerencia.getEstado() == Estado.PENDIENTE) {

                pendientes.add(sugerencia);
            }
        }

        return Collections.unmodifiableList(
            pendientes
        );
    }

    // NUEVO METODO:
    // RESTAURAR HISTORIAL DESDE UN ARCHIVO

    public void restaurarHistorial(
            List<SugerenciaProducto> recuperadas)
            throws ReglaNegocioException {

        if (recuperadas == null ||
            !sugerencias.isEmpty()) {

            throw new ReglaNegocioException(
                "Historial invalido o ya inicializado"
            );
        }

        Set<Integer> numeros = new HashSet<>();

        Set<String> propuestasActivas =
            new HashSet<>();

        int mayorNumero = 0;

        // VALIDAR TODOS LOS REGISTROS

        for (SugerenciaProducto item : recuperadas) {

            if (item == null ||
                item.getNumero() <= 0 ||
                item.getNumero() == Integer.MAX_VALUE ||
                item.getEstado() == null ||
                item.getArea() == null ||
                item.getCodigoProducto() == null ||
                item.getCodigoProducto().isBlank() ||
                !numeros.add(item.getNumero())) {

                throw new ReglaNegocioException(
                    "El historial contiene sugerencias invalidas"
                );
            }

            String llave =
                item.getArea().name() + "::" +
                item.getCodigoProducto().trim()
                    .toLowerCase(Locale.ROOT);

            // NO PUEDE HABER DOS PROPUESTAS ACTIVAS
            // PARA EL MISMO PRODUCTO

            if (item.getEstado() != Estado.RECHAZADA &&
                !propuestasActivas.add(llave)) {

                throw new ReglaNegocioException(
                    "El historial contiene propuestas activas duplicadas"
                );
            }

            mayorNumero = Math.max(
                mayorNumero,
                item.getNumero()
            );
        }

        // RESTAURAR SIN VOLVER A APROBAR PRODUCTOS

        sugerencias.addAll(recuperadas);

        // CONTINUAR NUMERACION SIN REPETIR CODIGOS

        siguienteNumero = mayorNumero + 1;
    }
}
