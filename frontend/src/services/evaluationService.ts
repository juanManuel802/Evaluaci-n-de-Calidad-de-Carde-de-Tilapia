import type { BatchResponseDTO } from "../types/batch";
import type { EvaluationResponseDTO } from "../types/evaluation";
import { apiPost, apiPostForm } from "./apiClient";

/** Sends the image of one unit; the backend classifies it and stores the result. */
export function evaluateUnit(code: string, image: File): Promise<EvaluationResponseDTO> {
  const formData = new FormData();
  formData.append("image", image);
  return apiPostForm<EvaluationResponseDTO>(`/api/evaluations/batches/${encodeURIComponent(code)}/units`, formData);
}

export function closeEvaluation(code: string): Promise<BatchResponseDTO> {
  return apiPost<Record<string, never>, BatchResponseDTO>(
    `/api/evaluations/batches/${encodeURIComponent(code)}/close`,
    {},
  );
}
