package co.unillanos.secct.usecases.dto;

import co.unillanos.secct.entities.Evaluation;

import java.time.OffsetDateTime;
import java.util.List;

/** Response body returned for an evaluated unit. */
public class EvaluationResponseDTO {

    public Integer id;
    public OffsetDateTime evaluatedAt;
    public List<ClassificationResult> results;

    public static EvaluationResponseDTO from(Evaluation evaluation) {
        EvaluationResponseDTO dto = new EvaluationResponseDTO();
        dto.id = evaluation.getId();
        dto.evaluatedAt = evaluation.getEvaluatedAt();
        dto.results = evaluation.getPoints().stream()
                .map(point -> new ClassificationResult(point.getFishPart().getCode(),
                        point.getNtcCategory(), point.getConfidence()))
                .toList();
        return dto;
    }

    @Override
    public String toString() {
        return "EvaluationResponseDTO{id=" + id + ", results=" + results + "}";
    }
}
