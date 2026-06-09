package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Evaluacion;
import co.unillanos.secct.entities.Lote;
import co.unillanos.secct.usecases.dto.OperationResult;
import co.unillanos.secct.usecases.dto.ResultadoClasificacion;
import co.unillanos.secct.usecases.ports.ClasificadorCnnPort;
import co.unillanos.secct.usecases.ports.LoteRepository;

import java.util.List;
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

        List<ResultadoClasificacion> resultados;
        try {
            resultados = clasificador.clasificar(imagen);
        } catch (Exception e) {
            return OperationResult.fail("Error al clasificar la imagen: " + e.getMessage());
        }

        int categoriaFinal = resultados.stream()
                .mapToInt(ResultadoClasificacion::getCategoriaNtc)
                .max()
                .orElseThrow(() -> new IllegalStateException("El clasificador no devolvió resultados."));

        StringBuilder detalle = new StringBuilder();
        for (ResultadoClasificacion r : resultados) {
            detalle.append(r.getParte())
                   .append(": categoría ").append(r.getCategoriaNtc())
                   .append(" (confianza ").append(r.getPuntajeConfianza()).append("); ");
        }

        lote.registrarEvaluacion(new Evaluacion(nombreImagen, categoriaFinal, lote));
        loteRepository.save(lote);

        return OperationResult.ok(
                "Unidad evaluada. Imagen: " + nombreImagen + ". "
                        + detalle
                        + "Evaluadas: " + lote.cantidadEvaluaciones()
                        + "/" + lote.getNumeroUnidadesMuestra() + ".");
    }
}
