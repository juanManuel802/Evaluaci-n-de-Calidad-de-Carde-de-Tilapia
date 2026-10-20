import type { BatchResponseDTO } from "../types/batch";

interface BatchResultCardProps {
  batch: BatchResponseDTO;
  onRegisterAnother: () => void;
}

function formatWeight(weightKg: number): string {
  return new Intl.NumberFormat("es-CO", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(weightKg);
}

export default function BatchResultCard({ batch, onRegisterAnother }: BatchResultCardProps) {
  return (
    <article className="card" style={{ maxWidth: "860px" }} aria-live="polite">
      <header>
        <h2 style={{ fontSize: "22px" }}>Lote registrado</h2>
      </header>
      <dl style={{ display: "grid", gridTemplateColumns: "max-content 1fr", gap: "8px 16px", margin: 0, fontSize: "14px" }}>
        <dt style={{ color: "#4d6a80" }}>Código del lote:</dt>
        <dd style={{ margin: 0 }}><strong>{batch.code}</strong></dd>
        <dt style={{ color: "#4d6a80" }}>Fecha de captura:</dt>
        <dd style={{ margin: 0 }}>{batch.captureDate}</dd>
        <dt style={{ color: "#4d6a80" }}>Peso:</dt>
        <dd style={{ margin: 0 }}>{formatWeight(batch.weightKg)} kg</dd>
        <dt style={{ color: "#4d6a80" }}>Número de unidades de muestra:</dt>
        <dd style={{ margin: 0 }}>{batch.sampleUnitCount}</dd>
        {batch.notes && (
          <>
            <dt style={{ color: "#4d6a80" }}>Observaciones:</dt>
            <dd style={{ margin: 0 }}>{batch.notes}</dd>
          </>
        )}
        <dt style={{ color: "#4d6a80" }}>Estado:</dt>
        <dd style={{ margin: 0 }}>{batch.statusCode}</dd>
      </dl>
      <footer className="row">
        <button className="p" type="button" onClick={onRegisterAnother}>Registrar otro lote</button>
      </footer>
    </article>
  );
}
