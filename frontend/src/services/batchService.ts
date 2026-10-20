import type { BatchResponseDTO, RegisterBatchRequestDTO } from "../types/batch";
import { apiGet, apiPost } from "./apiClient";

export function registerBatch(request: RegisterBatchRequestDTO): Promise<BatchResponseDTO> {
  return apiPost<RegisterBatchRequestDTO, BatchResponseDTO>("/api/batches", request);
}

/** Own batches in state ABIERTO or EN_EVALUACION. */
export function listAvailableBatches(): Promise<BatchResponseDTO[]> {
  return apiGet<BatchResponseDTO[]>("/api/batches");
}

export function getBatchByCode(code: string): Promise<BatchResponseDTO> {
  return apiGet<BatchResponseDTO>(`/api/batches/${encodeURIComponent(code)}`);
}

/** HU-05: Consult evaluated batches of the current user. */
export function consultEvaluatedBatches(): Promise<BatchResponseDTO[]> {
  return apiGet<BatchResponseDTO[]>("/api/batches/evaluated");
}
