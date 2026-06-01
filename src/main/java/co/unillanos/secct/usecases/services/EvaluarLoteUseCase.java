package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.util.Locale;
import java.util.Optional;


public class EvaluarLoteUseCase {

    private final LoteRepository loteRepository;

    public EvaluarLoteUseCase(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    public OperationResult execute(String loteId) {
        Optional<Lote> encontrado = loteRepository.findById(loteId);

        if (!encontrado.isPresent()) {
            return OperationResult.fail("Lote no encontrado: " + loteId);
        }

        Lote lote = encontrado.get();

        if (lote.getEstado() != EstadoLote.EN_EVALUACION) {
            return OperationResult.fail(
                    "El lote no se encuentra en estado de evaluación (estado actual: "
                            + lote.getEstado() + ").");
        }

        if (lote.cantidadEvaluaciones() == 0) {
            return OperationResult.fail(
                    "No es posible evaluar el lote. Debe tener al menos una unidad evaluada.");
        }

        lote.cerrarEvaluacion();
        loteRepository.save(lote);

        String clasificacionFormateada = String.format(Locale.ROOT, "%.2f", lote.getClasificacionFinal());
        return OperationResult.ok(
                "Lote '" + loteId + "' evaluado. Clasificación: " + clasificacionFormateada + ".");
    }
}
