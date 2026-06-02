package co.unillanos.secct.usecases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class DatosNuevoLote {

    private final String estacionOrigen;
    private final LocalDate fechaCaptura;
    private final BigDecimal pesoTotal;
    private final int numeroUnidadesMuestra;
    private final String puntoEvaluacion;
    private final String observaciones;

    public DatosNuevoLote(String estacionOrigen,
                          LocalDate fechaCaptura,
                          BigDecimal pesoTotal,
                          int numeroUnidadesMuestra,
                          String puntoEvaluacion,
                          String observaciones) {
        this.estacionOrigen = estacionOrigen;
        this.fechaCaptura = fechaCaptura;
        this.pesoTotal = pesoTotal;
        this.numeroUnidadesMuestra = numeroUnidadesMuestra;
        this.puntoEvaluacion = puntoEvaluacion;
        this.observaciones = observaciones;
    }

    public String getEstacionOrigen() { return estacionOrigen; }
    public LocalDate getFechaCaptura() { return fechaCaptura; }
    public BigDecimal getPesoTotal() { return pesoTotal; }
    public int getNumeroUnidadesMuestra() { return numeroUnidadesMuestra; }
    public String getPuntoEvaluacion() { return puntoEvaluacion; }
    public String getObservaciones() { return observaciones; }
}
