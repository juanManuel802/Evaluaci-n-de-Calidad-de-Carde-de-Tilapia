package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.CodigoLote;
import co.unillanos.secct.usecases.dto.DatosNuevoLote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.ports.GeneradorCodigoLotePort;
import co.unillanos.secct.usecases.ports.LoteRepository;


public class SecctApp {

    private final RegistrarLoteUseCase registrarLoteUseCase;

    public SecctApp(LoteRepository loteRepository,
                    GeneradorCodigoLotePort generador) {
        this.registrarLoteUseCase = new RegistrarLoteUseCase(loteRepository, generador);
    }

    // --- CU-001 Registrar Lote ---

    public CodigoLote obtenerCodigoNuevoLote() {
        return registrarLoteUseCase.obtenerCodigoNuevoLote();
    }

    public OperationResult registrarLote(DatosNuevoLote datos) {
        return registrarLoteUseCase.execute(datos);
    }
}
