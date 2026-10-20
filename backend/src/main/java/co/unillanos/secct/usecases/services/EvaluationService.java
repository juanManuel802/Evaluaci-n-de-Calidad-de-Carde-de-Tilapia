package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Batch;
import co.unillanos.secct.entities.BatchStatus;
import co.unillanos.secct.entities.Evaluation;
import co.unillanos.secct.entities.FishPart;
import co.unillanos.secct.infrastructure.repositories.BatchRepository;
import co.unillanos.secct.infrastructure.repositories.BatchStatusRepository;
import co.unillanos.secct.infrastructure.repositories.EvaluationRepository;
import co.unillanos.secct.infrastructure.repositories.FishPartRepository;
import co.unillanos.secct.usecases.dto.BatchResponseDTO;
import co.unillanos.secct.usecases.dto.ClassificationResult;
import co.unillanos.secct.usecases.dto.EvaluationResponseDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import co.unillanos.secct.usecases.ports.Classifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
public class EvaluationService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final String UNIT_EVALUATED_EVENT = "UNIT_EVALUATED";
    private static final String EVALUATION_CLOSED_EVENT = "EVALUATION_CLOSED";
    private static final String BATCH_NOT_FOUND_MESSAGE = "Lote no encontrado";

    private final BatchRepository batchRepository;
    private final BatchStatusRepository statusRepository;
    private final EvaluationRepository evaluationRepository;
    private final FishPartRepository fishPartRepository;
    private final Classifier classifier;
    private final ImageValidator imageValidator;
    private final LogService logService;

    public EvaluationService(BatchRepository batchRepository,
                             BatchStatusRepository statusRepository,
                             EvaluationRepository evaluationRepository,
                             FishPartRepository fishPartRepository,
                             Classifier classifier,
                             ImageValidator imageValidator,
                             LogService logService) {
        this.batchRepository = batchRepository;
        this.statusRepository = statusRepository;
        this.evaluationRepository = evaluationRepository;
        this.fishPartRepository = fishPartRepository;
        this.classifier = classifier;
        this.imageValidator = imageValidator;
        this.logService = logService;
    }

    /**
     * Evaluates one unit. Everything is saved in one transaction and the classifier is called before any
     * write, so if the classification service fails the batch is left untouched.
     */
    @Transactional
    public EvaluationResponseDTO evaluateUnit(String code, Integer ownerId, byte[] image) {
        Batch batch = findOwnBatch(code, ownerId);
        ensureOpenForEvaluation(batch);

        imageValidator.validateSize(image, MAX_IMAGE_BYTES);
        imageValidator.validateFormat(image);

        long evaluationCount = evaluationRepository.countByBatchId(batch.getId());
        if (evaluationCount >= batch.getSampleUnitCount()) {
            throw new BusinessException("El lote ya tiene todas sus unidades evaluadas ("
                    + evaluationCount + " de " + batch.getSampleUnitCount() + ")", 409);
        }

        String imageReference = generateImageReference(image);
        if (evaluationRepository.existsByBatchIdAndImageReference(batch.getId(), imageReference)) {
            throw new BusinessException("Esta imagen ya fue evaluada en el lote", 409);
        }

        List<ClassificationResult> results = classifier.classify(image);

        batch.startEvaluation(findStatus(BatchStatus.IN_EVALUATION));
        batchRepository.save(batch);

        Evaluation evaluation = new Evaluation(batch, imageReference);
        for (ClassificationResult result : results) {
            FishPart fishPart = fishPartRepository.findByCode(result.fishPartCode)
                    .orElseThrow(() -> new BusinessException(
                            "La parte " + result.fishPartCode + " no está configurada", 500));
            evaluation.addPoint(fishPart, result.ntcCategory, result.confidence);
        }
        Evaluation saved = evaluationRepository.save(evaluation);

        logService.recordEvent(UNIT_EVALUATED_EVENT, ownerId, batch.getId(), saved.getId(),
                "Unidad " + (evaluationCount + 1) + " del lote " + batch.getCode() + " evaluada");

        return EvaluationResponseDTO.from(saved);
    }

    @Transactional
    public BatchResponseDTO closeEvaluation(String code, Integer ownerId) {
        Batch batch = findOwnBatch(code, ownerId);
        ensureOpenForEvaluation(batch);

        long evaluationCount = evaluationRepository.countByBatchId(batch.getId());
        if (evaluationCount < batch.getSampleUnitCount()) {
            throw new BusinessException("Aún hay unidades sin clasificar (" + evaluationCount + " de "
                    + batch.getSampleUnitCount() + ")", 409);
        }

        BigDecimal finalClassification = calculateFinalClassification(batch.getId());
        batch.close(finalClassification, findStatus(BatchStatus.EVALUATED));
        batchRepository.save(batch);

        logService.recordEvent(EVALUATION_CLOSED_EVENT, ownerId, batch.getId(), null,
                "Evaluación del lote " + batch.getCode() + " cerrada con clasificación final "
                        + finalClassification);

        return BatchResponseDTO.from(batch, evaluationCount);
    }

    private Batch findOwnBatch(String code, Integer ownerId) {
        return batchRepository.findByCodeAndOwnerIdAndIsDeletedFalse(code, ownerId)
                .orElseThrow(() -> new BusinessException(BATCH_NOT_FOUND_MESSAGE, 404));
    }

    private void ensureOpenForEvaluation(Batch batch) {
        if (!batch.isOpenForEvaluation()) {
            throw new BusinessException("El lote ya fue evaluado y no admite más cambios", 409);
        }
    }

    private BatchStatus findStatus(String statusCode) {
        return statusRepository.findByCode(statusCode)
                .orElseThrow(() -> new BusinessException("El estado " + statusCode + " no está configurado", 500));
    }

    /** The reference is the SHA-256 of the image content, so the same image is detected even if renamed. */
    private String generateImageReference(byte[] image) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(image));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    /** Average NTC category of every result of the batch, rounded to 2 decimals. */
    private BigDecimal calculateFinalClassification(Integer batchId) {
        BigDecimal average = evaluationRepository.averageNtcCategoryByBatchId(batchId);
        if (average == null) {
            throw new BusinessException("El lote no tiene resultados para clasificar", 409);
        }
        return average.setScale(2, RoundingMode.HALF_UP);
    }
}
