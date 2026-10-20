import type { ChangeEvent } from "react";
import type { BatchResponseDTO } from "../types/batch";

interface BatchSelectorProps {
  batches: BatchResponseDTO[];
  selectedCode: string;
  isLoading: boolean;
  disabled: boolean;
  onSelect: (code: string) => void;
}

export default function BatchSelector({ batches, selectedCode, isLoading, disabled, onSelect }: BatchSelectorProps) {
  const hasBatches = batches.length > 0;

  function handleChange(event: ChangeEvent<HTMLSelectElement>) {
    onSelect(event.target.value);
  }

  return (
    <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
      <label htmlFor="batchCode" style={{ fontSize: "14px" }}>
        Lote disponible
      </label>
      <select
        id="batchCode"
        name="batchCode"
        value={selectedCode}
        onChange={handleChange}
        disabled={disabled || isLoading || !hasBatches}
        aria-describedby={!isLoading && !hasBatches ? "batchCodeHelp" : undefined}
      >
        {isLoading && <option value="">Cargando lotes...</option>}
        {!isLoading && !hasBatches && <option value="">Sin resultados</option>}
        {!isLoading && hasBatches && <option value="">Selecciona un lote</option>}
        {batches.map((batch) => (
          <option key={batch.code} value={batch.code}>
            {batch.code} ({batch.evaluationCount}/{batch.sampleUnitCount} unidades)
          </option>
        ))}
      </select>
      {!isLoading && !hasBatches && (
        <small id="batchCodeHelp" role="status">
          Sin resultados: no tienes lotes disponibles para evaluar
        </small>
      )}
    </div>
  );
}
