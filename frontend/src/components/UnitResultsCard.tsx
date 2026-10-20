import type { BatchResponseDTO } from "../types/batch";
import type { ClassificationResultDTO, EvaluatedUnitRow } from "../types/evaluation";

interface UnitResultsCardProps {
  rows: EvaluatedUnitRow[];
  canClose: boolean;
  isClosing: boolean;
  closedBatch: BatchResponseDTO | null;
  onClose: () => void;
}

function formatConfidence(confidence: number): string {
  return new Intl.NumberFormat("es-CO", { minimumFractionDigits: 4, maximumFractionDigits: 4 }).format(confidence);
}

function formatClassification(value: number): string {
  return new Intl.NumberFormat("es-CO", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);
}

function findResult(row: EvaluatedUnitRow, fishPartCode: string): ClassificationResultDTO | undefined {
  return row.evaluation.results.find((result) => result.fishPartCode === fishPartCode);
}

function renderCategory(result: ClassificationResultDTO | undefined) {
  return (
    <span className="inline-block bg-[#e0edf7] text-[#094b72] px-3 py-1 rounded-full text-xs font-semibold">
      {result ? `Categoría ${result.ntcCategory}` : "-"}
    </span>
  );
}

export default function UnitResultsCard({ rows, canClose, isClosing, closedBatch, onClose }: UnitResultsCardProps) {
  return (
    <article className="card" style={{ flex: 1 }} aria-labelledby="unit-results-title">
      <header>
        <h2 id="unit-results-title" style={{ fontSize: "22px" }}>Resultado por unidad</h2>
      </header>
      <div style={{ overflowX: "auto" }}>
        <table>
          <caption className="sr-only">Resultados de clasificación NTC 1443 por unidad</caption>
          <thead>
            <tr>
              <th scope="col">UNIDAD</th>
              <th scope="col">OJO</th>
              <th scope="col">PIEL</th>
              <th scope="col">CONFIANZA (0 a 1)</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 && (
              <tr>
                <td colSpan={4}>Aún no hay unidades clasificadas en esta sesión</td>
              </tr>
            )}
            {rows.map((row) => {
              const eye = findResult(row, "OJO");
              const skin = findResult(row, "PIEL");
              return (
                <tr key={row.evaluation.id}>
                  <td>{row.unitNumber}</td>
                  <td>{renderCategory(eye)}</td>
                  <td>{renderCategory(skin)}</td>
                  <td>
                    OJO {eye ? formatConfidence(eye.confidence) : "-"} · PIEL {skin ? formatConfidence(skin.confidence) : "-"}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
      <footer style={{ display: "flex", flexDirection: "column", gap: "12px", marginTop: "auto" }}>
        {closedBatch ? (
          <p role="status" className="text-xs text-[#4d6a80] m-0">
            Evaluación finalizada. El lote {closedBatch.code} pasó a {closedBatch.statusCode} y su clasificación final es:{" "}
            <strong>{closedBatch.finalClassification !== null ? formatClassification(closedBatch.finalClassification) : "-"}</strong>
          </p>
        ) : (
          <p className="text-xs text-[#4d6a80] m-0">
            Finalizar evaluación se habilita cuando todas las unidades están clasificadas. Al finalizar, el lote pasa a
            EVALUADO y se calcula su clasificación final.
          </p>
        )}
        <div className="row">
          <button className="p" type="button" disabled={!canClose || isClosing} onClick={onClose}>
            {isClosing ? "Finalizando..." : "Finalizar evaluación"}
          </button>
        </div>
      </footer>
    </article>
  );
}
