import { useState } from "react";
import type { ChangeEvent, SyntheticEvent } from "react";
import type { BatchFormField, BatchFormValues, RegisterBatchRequestDTO } from "../types/batch";
import { ApiError } from "../services/apiClient";
import { toRegisterBatchRequest, validateBatchForm } from "../validation/batchValidation";
import type { BatchFormErrors } from "../validation/batchValidation";

interface BatchRegistrationFormProps {
  onSubmit: (request: RegisterBatchRequestDTO) => Promise<void>;
}

const emptyValues: BatchFormValues = {
  captureDate: "",
  weightKg: "",
  sampleUnitCount: "",
  notes: "",
};

const errorStyle = { color: "#b42318", fontWeight: 600 };

const knownFields: BatchFormField[] = ["captureDate", "weightKg", "sampleUnitCount", "notes"];

export default function BatchRegistrationForm({ onSubmit }: BatchRegistrationFormProps) {
  const [values, setValues] = useState<BatchFormValues>(emptyValues);
  const [errors, setErrors] = useState<BatchFormErrors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) {
    const field = event.target.name as BatchFormField;
    setValues((current) => ({ ...current, [field]: event.target.value }));
  }

  function handleCancel() {
    setValues(emptyValues);
    setErrors({});
    setFormError(null);
  }

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    setFormError(null);

    const validationErrors = validateBatchForm(values);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) {
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(toRegisterBatchRequest(values));
    } catch (error) {
      if (error instanceof ApiError) {
        const serverErrors: BatchFormErrors = {};
        for (const fieldError of error.fieldErrors) {
          if (knownFields.includes(fieldError.field as BatchFormField)) {
            serverErrors[fieldError.field as BatchFormField] = fieldError.message;
          }
        }
        setErrors(serverErrors);
        setFormError(error.message);
      } else {
        setFormError("Ocurrió un error inesperado. Intenta de nuevo.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  function renderError(field: BatchFormField) {
    const message = errors[field];
    if (!message) {
      return null;
    }
    return (
      <small id={`${field}Error`} role="alert" style={errorStyle}>
        {message}
      </small>
    );
  }

  function describedBy(field: BatchFormField): string {
    return errors[field] ? `${field}Error` : `${field}Hint`;
  }

  return (
    <section aria-labelledby="register-batch-title" className="card" style={{ maxWidth: "860px" }}>
      <form
        style={{ display: "flex", flexDirection: "column", gap: "16px" }}
        onSubmit={handleSubmit}
        noValidate
      >
        {formError && (
          <aside role="alert" aria-live="assertive">
            <small style={errorStyle}>{formError}</small>
          </aside>
        )}
        <fieldset>
          <legend className="sr-only">Datos del lote</legend>
          <label htmlFor="captureDate">
            Fecha de captura
            <input
              id="captureDate"
              type="date"
              placeholder="dd/mm/aaaa"
              name="captureDate"
              value={values.captureDate}
              onChange={handleChange}
              aria-invalid={Boolean(errors.captureDate)}
              aria-describedby={describedBy("captureDate")}
            />
            <small id="captureDateHint">No puede ser futura</small>
            {renderError("captureDate")}
          </label>
          <label htmlFor="weightKg">
            Peso (kg)
            <input
              id="weightKg"
              type="text"
              placeholder="0,00"
              name="weightKg"
              value={values.weightKg}
              onChange={handleChange}
              aria-invalid={Boolean(errors.weightKg)}
              aria-describedby={describedBy("weightKg")}
            />
            <small id="weightKgHint">Mayor que 0, hasta 2 decimales, máx. 10.000,00</small>
            {renderError("weightKg")}
          </label>
          <label htmlFor="sampleUnitCount">
            Número de unidades de muestra
            <input
              id="sampleUnitCount"
              type="number"
              placeholder=""
              name="sampleUnitCount"
              value={values.sampleUnitCount}
              onChange={handleChange}
              aria-invalid={Boolean(errors.sampleUnitCount)}
              aria-describedby={describedBy("sampleUnitCount")}
            />
            <small id="sampleUnitCountHint">Entero de 1 a 500</small>
            {renderError("sampleUnitCount")}
          </label>
        </fieldset>

        <fieldset style={{ display: "flex", flexDirection: "column", gap: "6px", border: 0, padding: 0, margin: 0 }}>
          <legend className="sr-only">Observaciones del lote</legend>
          <label htmlFor="notes">
            Observaciones (opcional)
            <textarea
              id="notes"
              placeholder="Máximo 500 caracteres"
              name="notes"
              value={values.notes}
              onChange={handleChange}
              aria-invalid={Boolean(errors.notes)}
              aria-describedby={errors.notes ? "notesError" : "notesHint"}
            />
            {renderError("notes")}
          </label>
        </fieldset>

        <small id="notesHint">
          El código del lote (máx. 30 caracteres) lo genera el sistema al guardar. El lote queda en estado ABIERTO,
          disponible para evaluar.
        </small>
        <footer className="row">
          <button className="o" type="button" onClick={handleCancel}>
            Cancelar
          </button>
          <button className="p" type="submit" disabled={isSubmitting}>
            Guardar lote
          </button>
        </footer>
      </form>
    </section>
  );
}
