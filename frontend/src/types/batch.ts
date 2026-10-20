/** Request body of POST /api/batches. */
export interface RegisterBatchRequestDTO {
  captureDate: string; // yyyy-MM-dd
  weightKg: number;
  sampleUnitCount: number;
  notes: string | null;
}

/** Response body returned for a batch. */
export interface BatchResponseDTO {
  id: number;
  code: string;
  captureDate: string;
  weightKg: number;
  sampleUnitCount: number;
  notes: string | null;
  evaluationCount: number;
  finalClassification: number | null;
  statusCode: string;
}

export interface FieldErrorDTO {
  field: string;
  message: string;
}

export interface ErrorResponseDTO {
  message: string;
  errors: FieldErrorDTO[];
}

/** Raw values typed in the registration form. */
export interface BatchFormValues {
  captureDate: string;
  weightKg: string;
  sampleUnitCount: string;
  notes: string;
}

export type BatchFormField = keyof BatchFormValues;
