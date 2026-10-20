import { useRef, useState } from "react";
import type { ChangeEvent, DragEvent, KeyboardEvent } from "react";
import { validateImageFile } from "../validation/imageValidation";

interface UnitImageFormProps {
  classifiedCount: number;
  totalUnits: number | null;
  disabled: boolean;
  isUploading: boolean;
  onSubmit: (image: File) => Promise<boolean>;
}

const errorStyle = { color: "#b42318", fontWeight: 600 };

export default function UnitImageForm({ classifiedCount, totalUnits, disabled, isUploading, onSubmit }: UnitImageFormProps) {
  const [file, setFile] = useState<File | null>(null);
  const [fileError, setFileError] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  async function chooseFile(candidate: File | undefined) {
    setFile(null);
    setFileError(null);
    if (!candidate) {
      return;
    }
    const error = await validateImageFile(candidate);
    if (error) {
      setFileError(error);
      return;
    }
    setFile(candidate);
  }

  function handleInputChange(event: ChangeEvent<HTMLInputElement>) {
    void chooseFile(event.target.files?.[0]);
    event.target.value = "";
  }

  function handleDrop(event: DragEvent<HTMLElement>) {
    event.preventDefault();
    setIsDragging(false);
    if (!disabled) {
      void chooseFile(event.dataTransfer.files[0]);
    }
  }

  function handleDropKeyDown(event: KeyboardEvent<HTMLElement>) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      inputRef.current?.click();
    }
  }

  async function handleUpload() {
    if (!file) {
      return;
    }
    const wasEvaluated = await onSubmit(file);
    if (wasEvaluated) {
      setFile(null);
    }
  }

  return (
    <form style={{ display: "flex", flexDirection: "column", gap: "16px" }} onSubmit={(event) => event.preventDefault()}>
      <figure
        className="drop"
        style={{ margin: 0, cursor: disabled ? "not-allowed" : "pointer", opacity: disabled ? 0.6 : 1, borderColor: isDragging ? "#0b4f7a" : undefined }}
        role="button"
        tabIndex={disabled ? -1 : 0}
        aria-disabled={disabled}
        aria-label="Cargar imagen de muestra"
        onClick={() => !disabled && inputRef.current?.click()}
        onKeyDown={handleDropKeyDown}
        onDragOver={(event) => {
          event.preventDefault();
          setIsDragging(true);
        }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={handleDrop}
      >
        <figcaption style={{ fontSize: "inherit", color: "inherit", textAlign: "center" }}>
          {file ? (
            <strong>{file.name}</strong>
          ) : (
            <>
              Arrastra una imagen aquí o selecciónala
              <br />
              JPG o PNG, máximo 5 MB
            </>
          )}
        </figcaption>
        <input
          ref={inputRef}
          id="unit-image-file"
          type="file"
          accept="image/jpeg,image/png"
          hidden
          onChange={handleInputChange}
        />
      </figure>
      {fileError && (
        <aside role="alert" aria-live="assertive">
          <small style={errorStyle}>
            {fileError}
          </small>
        </aside>
      )}
      <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
        <output aria-live="polite" style={{ fontSize: "12px", color: "#4d6a80" }}>
          Unidades clasificadas: {totalUnits === null ? "-" : classifiedCount} de {totalUnits ?? "-"}
        </output>
        {totalUnits !== null && (
          <progress
            value={classifiedCount}
            max={totalUnits}
            className="sr-only"
            aria-label="Progreso de unidades clasificadas"
          >
            {classifiedCount} de {totalUnits}
          </progress>
        )}
      </div>
      <button className="o" type="button" disabled={disabled || !file || isUploading} onClick={handleUpload}>
        {isUploading ? "Clasificando..." : "Cargar imagen"}
      </button>
    </form>
  );
}
