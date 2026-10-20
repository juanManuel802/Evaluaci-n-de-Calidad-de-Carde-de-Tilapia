import { useEffect, useState } from "react";
import SideNav from "../components/SideNav";
import { ApiError } from "../services/apiClient";
import { consultEvaluatedBatches as apiConsultEvaluatedBatches } from "../services/batchService";
import { getUserFullName } from "../services/authStorage";
import type { BatchResponseDTO } from "../types/batch";

const errorStyle = { color: "#b42318", fontWeight: 600 };

/**
 * HU-05: Page for consulting evaluated batches implementing DC-HU5 and Mockup HU-05.
 */
export default function EvaluatedBatchesPage() {
  const [batches, setBatches] = useState<BatchResponseDTO[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isEmpty, setIsEmpty] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const userFullName = getUserFullName();

  /**
   * Implements showEvaluatedBatches from DC-HU5 diagram.
   */
  function showEvaluatedBatches(data: BatchResponseDTO[]): void {
    setBatches(data);
    setIsEmpty(false);
  }

  /**
   * Implements showEmptyResults from DC-HU5 diagram (PL-5.01, CN-08).
   */
  function showEmptyResults(): void {
    setBatches([]);
    setIsEmpty(true);
  }

  /**
   * Implements consultEvaluatedBatches from DC-HU5 diagram.
   */
  async function consultEvaluatedBatches(): Promise<void> {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const data = await apiConsultEvaluatedBatches();
      if (!data || data.length === 0) {
        showEmptyResults();
      } else {
        showEvaluatedBatches(data);
      }
    } catch (error) {
      const msg =
        error instanceof ApiError
          ? error.message
          : "Ocurrió un error inesperado al consultar los lotes evaluados.";
      setErrorMessage(msg);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void consultEvaluatedBatches();
  }, []);

  return (
    <div className="app min-h-screen">
      <SideNav current="evaluados" />
      <main id="main-content" className="main">
        <header className="top">
          <hgroup>
            <h1 id="evaluated-batches-title" style={{ fontSize: "36px" }}>Lotes evaluados</h1>
            <p style={{ margin: "4px 0 0" }}>
              Revisa la información registrada y los resultados de calidad de tus lotes.
            </p>
          </hgroup>
          <span aria-label="Usuario autenticado" className="bg-[#e6eff6] text-[#094b72] px-4 py-1.5 rounded-full text-xs font-semibold shadow-sm">
            {userFullName}
          </span>
        </header>

        {errorMessage && (
          <aside role="alert" aria-live="assertive">
            <small style={errorStyle}>
              {errorMessage}
            </small>
          </aside>
        )}

        <section aria-labelledby="evaluated-batches-title" className="card rounded-3xl overflow-hidden p-6 md:p-8 bg-white border border-[#d5e3ee] shadow-sm">
          {isLoading ? (
            <div className="py-12 text-center text-slate-500 font-medium" role="status">
              Cargando lotes evaluados...
            </div>
          ) : isEmpty || batches.length === 0 ? (
            <section
              role="status"
              aria-live="polite"
              id="empty-results-container"
              className="py-16 text-center flex flex-col items-center justify-center gap-3"
            >
              <p
                id="empty-results-text"
                className="text-base font-semibold text-slate-600 m-0"
              >
                Sin resultados
              </p>
              <p className="text-xs text-slate-400 m-0 max-w-sm">
                Solo se muestran tus lotes en estado EVALUADO. Si no hay ninguno, la pantalla muestra &quot;Sin resultados&quot;.
              </p>
            </section>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left" id="evaluated-batches-table">
                <caption className="sr-only">
                  Listado de lotes evaluados con clasificación final NTC 1443
                </caption>
                <thead>
                  <tr>
                    <th scope="col">CÓDIGO</th>
                    <th scope="col">FECHA DE CAPTURA</th>
                    <th scope="col">PESO (KG)</th>
                    <th scope="col">UNIDADES</th>
                    <th scope="col">OBSERVACIONES</th>
                    <th scope="col">EVALUACIONES</th>
                    <th scope="col">CLASIFICACIÓN FINAL</th>
                  </tr>
                </thead>
                <tbody>
                  {batches.map((batch) => (
                    <tr key={batch.id || batch.code} className="hover:bg-slate-50/80 transition-colors">
                      <td className="font-semibold text-[#0b4f7a] whitespace-nowrap">
                        {batch.code}
                      </td>
                      <td className="whitespace-nowrap">
                        <time dateTime={batch.captureDate}>{batch.captureDate}</time>
                      </td>
                      <td className="whitespace-nowrap">{batch.weightKg}</td>
                      <td className="text-center">{batch.sampleUnitCount}</td>
                      <td className="max-w-xs truncate" title={batch.notes ?? undefined}>
                        {/* PQ-04: Secure text node rendering to block stored XSS */}
                        {batch.notes && batch.notes.trim().length > 0 ? batch.notes : "—"}
                      </td>
                      <td className="text-center">{batch.evaluationCount}</td>
                      <td className="whitespace-nowrap">
                        <span className="bg-[#e0edf7] text-[#094b72] font-semibold px-3 py-1 rounded-full text-xs inline-block">
                          <data value={batch.finalClassification ?? ""}>
                            {batch.finalClassification !== null && batch.finalClassification !== undefined
                              ? Number(batch.finalClassification).toFixed(2)
                              : "0,00"}
                          </data>
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <footer className="mt-5">
                <p className="text-xs text-[#5b738b] m-0">
                  Solo se muestran tus lotes en estado EVALUADO. Si no hay ninguno, la pantalla muestra &quot;Sin resultados&quot;.
                </p>
              </footer>
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
