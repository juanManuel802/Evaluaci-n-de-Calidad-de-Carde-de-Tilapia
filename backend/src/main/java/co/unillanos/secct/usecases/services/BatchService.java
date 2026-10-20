package co.unillanos.secct.usecases.services;

import co.unillanos.secct.entities.Batch;
import co.unillanos.secct.entities.BatchStatus;
import co.unillanos.secct.entities.User;
import co.unillanos.secct.infrastructure.repositories.BatchRepository;
import co.unillanos.secct.infrastructure.repositories.BatchEvaluationCount;
import co.unillanos.secct.infrastructure.repositories.BatchStatusRepository;
import co.unillanos.secct.infrastructure.repositories.EvaluationRepository;
import co.unillanos.secct.infrastructure.repositories.UserRepository;
import co.unillanos.secct.usecases.dto.BatchResponseDTO;
import co.unillanos.secct.usecases.dto.RegisterBatchRequestDTO;
import co.unillanos.secct.usecases.exceptions.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BatchService {

    private static final String INITIAL_STATUS_CODE = "ABIERTO";
    private static final String BATCH_REGISTERED_EVENT = "BATCH_REGISTERED";
    private static final String EVALUATED_BATCHES_VIEWED_EVENT = "EVALUATED_BATCHES_VIEWED";

    private final UserRepository userRepository;
    private final BatchStatusRepository statusRepository;
    private final BatchRepository batchRepository;
    private final EvaluationRepository evaluationRepository;
    private final BatchCodeGenerator codeGenerator;
    private final LogService logService;

    public BatchService(UserRepository userRepository,
                        BatchStatusRepository statusRepository,
                        BatchRepository batchRepository,
                        EvaluationRepository evaluationRepository,
                        BatchCodeGenerator codeGenerator,
                        LogService logService) {
        this.userRepository = userRepository;
        this.statusRepository = statusRepository;
        this.batchRepository = batchRepository;
        this.evaluationRepository = evaluationRepository;
        this.codeGenerator = codeGenerator;
        this.logService = logService;
    }

    @Transactional
    public BatchResponseDTO register(RegisterBatchRequestDTO request, Integer ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", 401));

        BatchStatus status = statusRepository.findByCode(INITIAL_STATUS_CODE)
                .orElseThrow(() -> new BusinessException(
                        "El estado " + INITIAL_STATUS_CODE + " no está configurado", 500));

        long dailyCount = batchRepository.countByCaptureDate(request.captureDate);
        String code = codeGenerator.generate(request.captureDate, dailyCount + 1);

        Batch batch = new Batch(code, request.captureDate, request.weightKg, request.sampleUnitCount,
                request.notes, owner, status);
        Batch saved = batchRepository.save(batch);

        logService.recordEvent(BATCH_REGISTERED_EVENT, ownerId, saved.getId(), null,
                "Lote " + saved.getCode() + " registrado");

        return BatchResponseDTO.from(saved, 0);
    }

    /** Own batches that still accept evaluated units (ABIERTO or EN_EVALUACION), newest first. */
    @Transactional(readOnly = true)
    public List<BatchResponseDTO> listAvailable(Integer ownerId) {
        List<Batch> batches = batchRepository.findByOwnerIdAndStatusCodeInAndIsDeletedFalse(
                ownerId, List.of(BatchStatus.OPEN, BatchStatus.IN_EVALUATION));
        if (batches.isEmpty()) {
            return List.of();
        }

        List<Integer> batchIds = batches.stream().map(Batch::getId).toList();
        Map<Integer, Long> countsByBatchId = evaluationRepository.countGroupedByBatchIds(batchIds).stream()
                .collect(Collectors.toMap(BatchEvaluationCount::batchId, BatchEvaluationCount::evaluationCount));

        return batches.stream()
                .sorted(Comparator.comparing(Batch::getCreatedAt).reversed())
                .map(batch -> BatchResponseDTO.from(batch, countsByBatchId.getOrDefault(batch.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public BatchResponseDTO getByCodeAndOwner(String code, Integer ownerId) {
        Batch batch = batchRepository.findByCodeAndOwnerIdAndIsDeletedFalse(code, ownerId)
                .orElseThrow(() -> new BusinessException("Lote no encontrado", 404));
        return BatchResponseDTO.from(batch, evaluationRepository.countByBatchId(batch.getId()));
    }

    /**
     * HU-05: Consult evaluated batches for an owner (DC-HU5, DS-HU5).
     * Steps 8 to 21:
     * - Query batches with status "EVALUADO", ownerId, and isDeleted = false.
     * - In a single grouped query (CB-11), count evaluations by batchIds.
     * - Iterate batches (testing 0, 1, and N iterations for CB-02) mapping evaluationCount (0 if absent).
     * - Always records audit event EVALUATED_BATCHES_VIEWED (PL-5.08, PI-13).
     * - Returns List<BatchResponseDTO>.
     */
    @Transactional
    public List<BatchResponseDTO> listEvaluated(Integer ownerId) {
        if (ownerId == null) {
            throw new BusinessException("Usuario no autenticado", 401);
        }

        List<Batch> batches = batchRepository.findByOwnerIdAndStatusCodeAndIsDeletedFalse(
                ownerId, BatchStatus.EVALUATED);

        Map<Integer, Long> countsByBatchId;
        if (batches.isEmpty()) {
            countsByBatchId = Map.of();
        } else {
            List<Integer> batchIds = batches.stream().map(Batch::getId).toList();
            countsByBatchId = evaluationRepository.countGroupedByBatchIds(batchIds).stream()
                    .collect(Collectors.toMap(BatchEvaluationCount::batchId, BatchEvaluationCount::evaluationCount));
        }

        List<BatchResponseDTO> result = new ArrayList<>(batches.size());
        for (Batch batch : batches) {
            long evaluationCount = countsByBatchId.getOrDefault(batch.getId(), 0L);
            result.add(BatchResponseDTO.from(batch, evaluationCount));
        }

        logService.recordEvent(EVALUATED_BATCHES_VIEWED_EVENT, ownerId, null, null,
                "Consulta de lotes evaluados");

        return result;
    }
}
