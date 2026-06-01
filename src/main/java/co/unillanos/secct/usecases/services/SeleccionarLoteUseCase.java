package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.EstadoLote;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.util.List;
import java.util.Optional;


public class SeleccionarLoteUseCase {

    private final LoteRepository loteRepository;

    
    public SeleccionarLoteUseCase(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    public List<Lote> listarDisponibles() {
        return loteRepository.findByEstadoIn(EstadoLote.ABIERTO, EstadoLote.EN_EVALUACION);
    }

    public List<Lote> listarEvaluados() {
        return loteRepository.findByEstadoIn(EstadoLote.EVALUADO, EstadoLote.REPORTADO);
    }

    
    public OperationResult execute(String loteId) {
        Optional<Lote> encontrado = loteRepository.findById(loteId);

        if (!encontrado.isPresent()) {
            return OperationResult.fail("Lote no encontrado: " + loteId);
        }

        Lote lote = encontrado.get();

        if (!lote.estaDisponible()) {
            return OperationResult.fail(
                    "El lote '" + loteId + "' ha alcanzado la cantidad "
                            + "comprometida de unidades de muestra o no admite "
                            + "más evaluaciones en su estado actual ("
                            + lote.getEstado() + ").");
        }

        return OperationResult.ok("Lote '" + loteId + "' seleccionado. "
                + lote.cantidadEvaluaciones() + "/"
                + lote.getNumeroUnidadesMuestra() + " unidades evaluadas.");
    }
}
