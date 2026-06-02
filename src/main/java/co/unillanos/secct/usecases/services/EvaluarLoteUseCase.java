package co.unillanos.secct.usecases.services;

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

        try {
            lote.reportarEvaluacion();
        } catch (IllegalStateException e) {
            return OperationResult.fail(e.getMessage());
        }

        loteRepository.save(lote);

        String clasificacionFormateada = String.format(Locale.ROOT, "%.2f", lote.getClasificacionFinal());
        return OperationResult.ok(
                "Lote '" + loteId + "' reportado. Clasificación final: " + clasificacionFormateada + ".");
    }
}
