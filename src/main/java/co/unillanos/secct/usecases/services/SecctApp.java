package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;
import co.unillanos.secct.usecases.ports.GeneradorCodigoLotePort;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.util.List;


public class SecctApp {

    private final RegistrarLoteUseCase registrarLoteUseCase;
    private final SeleccionarLoteUseCase seleccionarLoteUseCase;
    private final EvaluarUnidadUseCase evaluarUnidadUseCase;
    private final EvaluarLoteUseCase evaluarLoteUseCase;


    public SecctApp(LoteRepository loteRepository,
                    ClasificadorCnnPort clasificador,
                    GeneradorCodigoLotePort generador) {
        this.registrarLoteUseCase   = new RegistrarLoteUseCase(loteRepository, generador);
        this.seleccionarLoteUseCase = new SeleccionarLoteUseCase(loteRepository);
        this.evaluarUnidadUseCase   = new EvaluarUnidadUseCase(loteRepository, clasificador);
        this.evaluarLoteUseCase     = new EvaluarLoteUseCase(loteRepository);
    }


    // --- CU-001 Registrar Lote ---

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

    // --- CU-003 Evaluar Unidad ---

    public OperationResult evaluarUnidad(String loteId, String nombreImagen, byte[] imagen) {
        return evaluarUnidadUseCase.execute(loteId, nombreImagen, imagen);
    }

    // --- CU-004 Evaluar Lote ---

    public OperationResult evaluarLote(String loteId) {
        return evaluarLoteUseCase.execute(loteId);
    }
}
