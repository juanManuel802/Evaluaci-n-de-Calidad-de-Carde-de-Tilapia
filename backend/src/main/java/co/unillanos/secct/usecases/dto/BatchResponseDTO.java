package co.unillanos.secct.usecases.dto;

import co.unillanos.secct.entities.Batch;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Response body returned for a batch. */
public class BatchResponseDTO {

    public Integer id;
    public String code;
    public LocalDate captureDate;
    public BigDecimal weightKg;
    public Integer sampleUnitCount;
    public String notes;
    public long evaluationCount;
    public BigDecimal finalClassification;
    public String statusCode;

    public static BatchResponseDTO from(Batch batch, long evaluationCount) {
        BatchResponseDTO dto = new BatchResponseDTO();
        dto.id = batch.getId();
        dto.code = batch.getCode();
        dto.captureDate = batch.getCaptureDate();
        dto.weightKg = batch.getWeightKg();
        dto.sampleUnitCount = batch.getSampleUnitCount();
        dto.notes = batch.getNotes();
        dto.evaluationCount = evaluationCount;
        dto.finalClassification = batch.getFinalClassification();
        dto.statusCode = batch.getStatus().getCode();
        return dto;
    }

    @Override
    public String toString() {
        return "BatchResponseDTO{id=" + id + ", code=" + code + ", statusCode=" + statusCode + "}";
    }
}
