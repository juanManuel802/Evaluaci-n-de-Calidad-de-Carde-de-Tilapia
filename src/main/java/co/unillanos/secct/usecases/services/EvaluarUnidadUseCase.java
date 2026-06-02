package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.util.Optional;


public class EvaluarUnidadUseCase {

    private final LoteRepository loteRepository;
    private final ClasificadorCnnPort clasificador;

    public EvaluarUnidadUseCase(LoteRepository loteRepository,
                                ClasificadorCnnPort clasificador) {
        this.loteRepository = loteRepository;
        this.clasificador = clasificador;
    }

    public OperationResult execute(String loteId, String nombreImagen, byte[] imagen) {
        if (nombreImagen == null || nombreImagen.isBlank()) {
            return OperationResult.fail("El nombre de la imagen no puede estar vacío.");
        }

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

        Evaluacion evaluacion = new Evaluacion(nombreImagen, resultado.getCategoriaNtc(), lote);
        lote.registrarEvaluacion(evaluacion);
        loteRepository.save(lote);

        return OperationResult.ok(
                "Unidad evaluada. Imagen: " + nombreImagen
                        + ". Categoría NTC: " + resultado.getCategoriaNtc()
                        + ". Confianza: " + resultado.getPuntajeConfianza()
                        + ". Evaluadas: " + lote.cantidadEvaluaciones()
                        + "/" + lote.getNumeroUnidadesMuestra() + ".");
    }
}
