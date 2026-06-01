package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.nio.file.Path;
import java.util.Optional;


public class EvaluarUnidadUseCase {

    private final LoteRepository loteRepository;
    private final ClasificadorCnnPort clasificador;

    
    public EvaluarUnidadUseCase(LoteRepository loteRepository,
                                ClasificadorCnnPort clasificador) {
        this.loteRepository = loteRepository;
        this.clasificador = clasificador;
    }

    
    public OperationResult execute(String loteId, Path imagen) {
        Optional<Lote> encontrado = loteRepository.findById(loteId);

        if (!encontrado.isPresent()) {
            return OperationResult.fail("Lote no encontrado: " + loteId);
        }

        Lote lote = encontrado.get();

        if (!lote.estaDisponible()) {
            return OperationResult.fail(
                    "El lote '" + loteId + "' no está disponible para nuevas "
                            + "evaluaciones (estado: " + lote.getEstado()
                            + ", evaluadas: " + lote.cantidadEvaluaciones()
                            + "/" + lote.getNumeroUnidadesMuestra() + ").");
        }

        ResultadoClasificacion resultado = clasificador.clasificar(imagen);

        String idImagen = imagen.getFileName() != null
                ? imagen.getFileName().toString()
                : imagen.toString();

        Evaluacion evaluacion = new Evaluacion(idImagen, resultado.getCategoriaNtc(), lote);
        lote.registrarEvaluacion(evaluacion);
        loteRepository.save(lote);

        return OperationResult.ok(
                "Unidad evaluada. Imagen: " + idImagen
                        + ". Categoría NTC: " + resultado.getCategoriaNtc()
                        + ". Confianza: " + resultado.getPuntajeConfianza()
                        + ". Evaluadas: " + lote.cantidadEvaluaciones()
                        + "/" + lote.getNumeroUnidadesMuestra() + ".");
    }
}
