import { useState } from "react";
import BatchRegistrationForm from "../components/BatchRegistrationForm";
import BatchResultCard from "../components/BatchResultCard";
import { registerBatch } from "../services/batchService";
import { getUserFullName } from "../services/authStorage";
import type { BatchResponseDTO, RegisterBatchRequestDTO } from "../types/batch";
import SideNav from "../components/SideNav";

export default function RegisterBatchPage() {
  const [registeredBatch, setRegisteredBatch] = useState<BatchResponseDTO | null>(null);
  const userFullName = getUserFullName();

  async function submitBatchRegistration(request: RegisterBatchRequestDTO): Promise<void> {
    const response = await registerBatch(request);
    showBatchResult(response);
  }

  function showBatchResult(response: BatchResponseDTO): void {
    setRegisteredBatch(response);
  }

  return (
    <div className="app min-h-screen">
      <SideNav current="register" />
      <main id="main-content" className="main">
        <header className="top">
          <hgroup>
            <h1 id="register-batch-title" style={{ fontSize: "36px" }}>Registrar lote</h1>
            <p style={{ margin: "4px 0 0" }}>Identifica tu lote para llevar su trazabilidad.</p>
          </hgroup>
          <span aria-label="Usuario autenticado" className="bg-[#e6eff6] text-[#094b72] px-4 py-1.5 rounded-full text-xs font-semibold shadow-sm">
            {userFullName}
          </span>
        </header>
        {registeredBatch ? (
          <BatchResultCard batch={registeredBatch} onRegisterAnother={() => setRegisteredBatch(null)} />
        ) : (
          <BatchRegistrationForm onSubmit={submitBatchRegistration} />
        )}
      </main>
    </div>
  );
}
