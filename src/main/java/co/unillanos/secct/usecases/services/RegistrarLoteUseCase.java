package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.FechaCaptura;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.entities.PesoLote;
import co.unillanos.secct.entities.PuntoEvaluacion;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.GeneradorCodigoLotePort;
import co.unillanos.secct.usecases.ports.LoteRepository;

public class RegistrarLoteUseCase {

    private final LoteRepository loteRepository;
    private final GeneradorCodigoLotePort generadorCodigoLote;

    public RegistrarLoteUseCase(LoteRepository loteRepository,
                                GeneradorCodigoLotePort generadorCodigoLote) {
        this.loteRepository = loteRepository;
        this.generadorCodigoLote = generadorCodigoLote;
    }

    public OperationResult execute(DatosNuevoLote datos) {
        CodigoLote codigo;
        try {
            codigo = generadorCodigoLote.generarCodigoLote();
        } catch (IllegalStateException e) {
            return OperationResult.fail(e.getMessage());
        }

        FechaCaptura fechaCaptura;
        try {
            fechaCaptura = new FechaCaptura(datos.getFechaCaptura());
        } catch (IllegalArgumentException e) {
            return OperationResult.fail(e.getMessage());
        }

        PesoLote pesoLote;
        try {
            pesoLote = new PesoLote(datos.getPesoTotal());
        } catch (IllegalArgumentException e) {
            return OperationResult.fail(e.getMessage());
        }

        PuntoEvaluacion puntoEvaluacion;
        try {
            puntoEvaluacion = PuntoEvaluacion.valueOf(datos.getPuntoEvaluacion());
        } catch (IllegalArgumentException | NullPointerException e) {
            return OperationResult.fail(
                    "Punto de evaluación inválido: '" + datos.getPuntoEvaluacion()
                            + "' (RN-006). Valores permitidos: "
                            + java.util.Arrays.toString(PuntoEvaluacion.values()));
        }

        Lote lote;
        try {
            lote = new Lote(codigo, datos.getEstacionOrigen(), fechaCaptura, pesoLote,
                    datos.getNumeroUnidadesMuestra(), puntoEvaluacion,
                    datos.getObservaciones());
        } catch (IllegalArgumentException e) {
            return OperationResult.fail(e.getMessage());
        }

        loteRepository.save(lote);

        return OperationResult.ok(
                "Lote '" + codigo.getValor() + "' registrado exitosamente. "
                        + "Estado: ABIERTO. "
                        + "Listo para evaluación de unidades de muestra.");
    }
}
