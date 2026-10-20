import type { BatchFormField, BatchFormValues, RegisterBatchRequestDTO } from "../types/batch";

export type BatchFormErrors = Partial<Record<BatchFormField, string>>;

const maxWeightKg = 10000;
const maxNotesLength = 500;

/** Today in the user's local time, formatted yyyy-MM-dd. */
export function getTodayIsoDate(): string {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${month}-${day}`;
}

function parseWeight(rawWeight: string): number {
  return Number(rawWeight.trim().replace(",", "."));
}

export function validateBatchForm(values: BatchFormValues): BatchFormErrors {
  const errors: BatchFormErrors = {};

  if (!values.captureDate) {
    errors.captureDate = "La fecha de captura es obligatoria";
  } else if (values.captureDate > getTodayIsoDate()) {
    errors.captureDate = "La fecha de captura no puede ser futura";
  }

  const rawWeight = values.weightKg.trim();
  if (!rawWeight) {
    errors.weightKg = "El peso es obligatorio";
  } else if (!/^\d+([.,]\d+)?$/.test(rawWeight)) {
    errors.weightKg = "El peso debe ser un número";
  } else {
    const weight = parseWeight(rawWeight);
    if (weight <= 0) {
      errors.weightKg = "El peso debe ser mayor que 0";
    } else if (weight > maxWeightKg) {
      errors.weightKg = "El peso no puede superar 10.000,00";
    } else if (!/^\d+([.,]\d{1,2})?$/.test(rawWeight)) {
      errors.weightKg = "El peso admite máximo 2 decimales";
    }
  }

  const rawUnits = values.sampleUnitCount.trim();
  if (!rawUnits) {
    errors.sampleUnitCount = "El número de unidades de muestra es obligatorio";
  } else {
    const units = Number(rawUnits);
    if (!Number.isInteger(units) || units < 1 || units > 500) {
      errors.sampleUnitCount = "Las unidades de muestra deben ser un entero entre 1 y 500";
    }
  }

  if (values.notes.length > maxNotesLength) {
    errors.notes = "Las observaciones admiten máximo 500 caracteres";
  }

  return errors;
}

/** Builds the request body. Call it only after validateBatchForm returned no errors. */
export function toRegisterBatchRequest(values: BatchFormValues): RegisterBatchRequestDTO {
  const notes = values.notes.trim();
  return {
    captureDate: values.captureDate,
    weightKg: parseWeight(values.weightKg),
    sampleUnitCount: Number(values.sampleUnitCount),
    notes: notes === "" ? null : notes,
  };
}
