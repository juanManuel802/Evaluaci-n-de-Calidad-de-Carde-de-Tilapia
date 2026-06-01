package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.GeneradorCodigoLotePort;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.util.List;


public class SecctApp {

    private final RegistrarLoteUseCase registrarLoteUseCase;
    private final SeleccionarLoteUseCase seleccionarLoteUseCase;

    public SecctApp(LoteRepository loteRepository,
                    GeneradorCodigoLotePort generador) {
        this.registrarLoteUseCase = new RegistrarLoteUseCase(loteRepository, generador);
        this.seleccionarLoteUseCase = new SeleccionarLoteUseCase(loteRepository);
    }

    // --- CU-001 Registrar Lote ---

    public CodigoLote obtenerCodigoNuevoLote() {
        return registrarLoteUseCase.obtenerCodigoNuevoLote();
    }

    public OperationResult registrarLote(DatosNuevoLote datos) {
        return registrarLoteUseCase.execute(datos);
    }

    // --- CU-002 Seleccionar Lote ---

    public List<Lote> listarLotesDisponibles() {
        return seleccionarLoteUseCase.listarDisponibles();
    }

    public List<Lote> listarLotesEvaluados() {
        return seleccionarLoteUseCase.listarEvaluados();
    }

    public OperationResult seleccionarLote(String loteId) {
        return seleccionarLoteUseCase.execute(loteId);
    }
}
