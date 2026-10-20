/** Result of one fish part (OJO or PIEL) of an evaluated unit. */
export interface ClassificationResultDTO {
  fishPartCode: string;
  ntcCategory: number;
  confidence: number;
}

/** Response body returned for an evaluated unit. */
export interface EvaluationResponseDTO {
  id: number;
  evaluatedAt: string;
  results: ClassificationResultDTO[];
}

/** One row of the results table: the unit number and its classification. */
export interface EvaluatedUnitRow {
  unitNumber: number;
  evaluation: EvaluationResponseDTO;
}
