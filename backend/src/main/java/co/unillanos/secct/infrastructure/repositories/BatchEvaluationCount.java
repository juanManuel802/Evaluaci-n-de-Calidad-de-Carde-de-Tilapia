package co.unillanos.secct.infrastructure.repositories;

/** Projection: number of evaluated units of one batch. */
public record BatchEvaluationCount(Integer batchId, long evaluationCount) {
}
