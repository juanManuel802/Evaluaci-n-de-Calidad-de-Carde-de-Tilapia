package co.unillanos.secct.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lote {

    private static final int MAX_ESTACION_ORIGEN = 100;
    private static final int MAX_OBSERVACIONES = 500;

    private final CodigoLote codigo;
    private final String estacionOrigen;
    private final FechaCaptura fechaCaptura;
    private final PesoLote pesoLote;
    private int numeroUnidadesMuestra;
    private final PuntoEvaluacion puntoEvaluacion;
    private EstadoLote estado;
    private final String observaciones;
    private final List<Evaluacion> evaluaciones;
    private double clasificacionFinal;

    public Lote(CodigoLote codigo,
                String estacionOrigen,
                FechaCaptura fechaCaptura,
                PesoLote pesoLote,
                int numeroUnidadesMuestra,
                PuntoEvaluacion puntoEvaluacion,
                String observaciones) {

        if (codigo == null) {
            throw new IllegalArgumentException(
                    "El código del lote no puede ser nulo (RN-001).");
        }
        if (estacionOrigen == null || estacionOrigen.isBlank()) {
            throw new IllegalArgumentException(
                    "La estación de origen es obligatoria (RN-002).");
        }
        if (estacionOrigen.length() > MAX_ESTACION_ORIGEN) {
            throw new IllegalArgumentException(
                    "La estación de origen no puede superar " + MAX_ESTACION_ORIGEN
                            + " caracteres (RN-002); longitud recibida: "
                            + estacionOrigen.length());
        }
        if (fechaCaptura == null) {
            throw new IllegalArgumentException(
                    "La fecha de captura es obligatoria (RN-003).");
        }
        if (pesoLote == null) {
            throw new IllegalArgumentException(
                    "El peso del lote es obligatorio (RN-004).");
        }
        if (numeroUnidadesMuestra < 1) {
            throw new IllegalArgumentException(
                    "El número de unidades de muestra debe ser ≥ 1 (RN-005); valor recibido: "
                            + numeroUnidadesMuestra);
        }
        if (puntoEvaluacion == null) {
            throw new IllegalArgumentException(
                    "El punto de evaluación es obligatorio (RN-006).");
        }
        String obs = (observaciones != null) ? observaciones : "";
        if (obs.length() > MAX_OBSERVACIONES) {
            throw new IllegalArgumentException(
                    "Las observaciones no pueden superar " + MAX_OBSERVACIONES
                            + " caracteres (RN-008); longitud recibida: " + obs.length());
        }

        this.codigo = codigo;
        this.estacionOrigen = estacionOrigen;
        this.fechaCaptura = fechaCaptura;
        this.pesoLote = pesoLote;
        this.numeroUnidadesMuestra = numeroUnidadesMuestra;
        this.puntoEvaluacion = puntoEvaluacion;
        this.estado = EstadoLote.ABIERTO;
        this.observaciones = obs;
        this.evaluaciones = new ArrayList<Evaluacion>();
        this.clasificacionFinal = 0.0;
    }

    public void cerrarEvaluacion() {
        if (estado != EstadoLote.EN_EVALUACION) {
            throw new IllegalStateException(
                    "El lote '" + codigo.getValor()
                            + "' debe estar en estado EN_EVALUACION para cerrar la evaluación (RN-020); estado actual: "
                            + estado);
        }
        if (evaluaciones.isEmpty()) {
            throw new IllegalStateException(
                    "El lote '" + codigo.getValor()
                            + "' debe tener al menos una evaluación registrada para cerrar (RN-021).");
        }
        double suma = 0.0;
        for (Evaluacion e : evaluaciones) {
            suma += e.getClasificacion();
        }
        this.clasificacionFinal = suma / evaluaciones.size();
        this.estado = EstadoLote.EVALUADO;
    }

    public void registrarEvaluacion(Evaluacion evaluacion) {
        if (evaluacion == null) {
            throw new IllegalArgumentException(
                    "La evaluacion a registrar no puede ser null.");
        }
        if (estado == EstadoLote.EVALUADO || estado == EstadoLote.REPORTADO) {
            throw new IllegalStateException(
                    "El lote '" + codigo.getValor()
                            + "' no acepta nuevas evaluaciones en estado "
                            + estado + " (RN-012).");
        }

        boolean esPrimeraEvaluacion = evaluaciones.isEmpty();
        evaluaciones.add(evaluacion);

        if (esPrimeraEvaluacion && estado == EstadoLote.ABIERTO) {
            estado = EstadoLote.EN_EVALUACION;
        }
    }

    public int cantidadEvaluaciones() {
        return evaluaciones.size();
    }

    public boolean estaDisponible() {
        boolean bajoQuota = evaluaciones.size() < numeroUnidadesMuestra;
        boolean estadoAceptable =
                estado == EstadoLote.ABIERTO || estado == EstadoLote.EN_EVALUACION;
        return bajoQuota && estadoAceptable;
    }

    public String getId() {
        return codigo.getValor();
    }

    public CodigoLote getCodigo() {
        return codigo;
    }

    public String getEstacionOrigen() {
        return estacionOrigen;
    }

    public FechaCaptura getFechaCaptura() {
        return fechaCaptura;
    }

    public PesoLote getPesoLote() {
        return pesoLote;
    }

    public int getNumeroUnidadesMuestra() {
        return numeroUnidadesMuestra;
    }

    public PuntoEvaluacion getPuntoEvaluacion() {
        return puntoEvaluacion;
    }

    public EstadoLote getEstado() {
        return estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public List<Evaluacion> getEvaluaciones() {
        return Collections.unmodifiableList(evaluaciones);
    }

    public double getClasificacionFinal() {
        return clasificacionFinal;
    }
}
