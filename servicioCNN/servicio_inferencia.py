"""
SECCT — Servicio de inferencia CNN.

Contrato: POST /evaluar
  Request:  { "imagen_base64": "<bytes en base64>" }
  Response: { "partes": [ { "parte": "OJO"|"PIEL", "categoria_ntc": 1-5, "confianza": 0-1 } ] }

Variables de entorno:
  SECCT_MODELO_H5   Ruta al archivo .h5 del modelo entrenado (opcional).
                    Por defecto: ../models/modelo_calidad_tilapia.h5

Mapeo score → categoria_ntc:
  score ∈ [0, 1]  (salida directa del modelo)
  categoria_ntc = max(1, min(5, ceil(score * 5)))
  confianza     = score  (sin transformar)
"""

import base64
import io
import math
import os

import uvicorn
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

# ── Configuración al arrancar ──────────────────────────────────────────────────

_DEFAULT_MODELO = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "models", "modelo_calidad_tilapia.h5"))
MODELO_H5 = os.getenv("SECCT_MODELO_H5", _DEFAULT_MODELO).strip()
if not os.path.isfile(MODELO_H5):
    raise SystemExit(
        f"\nERROR: El archivo del modelo no existe: {MODELO_H5}\n"
        "  Verificá que SECCT_MODELO_H5 apunte a un .h5 válido.\n"
    )

os.environ.setdefault("TF_CPP_MIN_LOG_LEVEL", "2")
import tensorflow as tf

from tensorflow.keras.layers import DepthwiseConv2D as _DepthwiseConv2D

class _DepthwiseConv2DCompat(_DepthwiseConv2D):
    def __init__(self, *args, **kwargs):
        kwargs.pop("groups", None)
        super().__init__(*args, **kwargs)

# compile=False: omitimos optimizer/loss/metrics del checkpoint (Keras 2).
# No se necesitan para inferencia y evitan errores de deserialización en Keras 3.
model = tf.keras.models.load_model(
    MODELO_H5,
    custom_objects={"DepthwiseConv2D": _DepthwiseConv2DCompat},
    compile=False,
)
print(f"Modelo cargado: {MODELO_H5}")

# ── Aplicación ─────────────────────────────────────────────────────────────────

app = FastAPI(
    title="SECCT Inferencia CNN",
    version="1.0.0",
    docs_url="/docs",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET", "POST"],
    allow_headers=["*"],
)

# ── Modelos Pydantic ────────────────────────────────────────────────────────────

class EvaluarRequest(BaseModel):
    imagen_base64: str

# ── Lógica de inferencia ────────────────────────────────────────────────────────

def _score_a_categoria(score: float) -> int:
    """max(1, min(5, ceil(score * 5)))  —  mapea [0,1] a [1,5]."""
    return max(1, min(5, math.ceil(score * 5)))


def _inferir(imagen_bytes: bytes) -> dict:
    import numpy as np
    from PIL import Image

    img = Image.open(io.BytesIO(imagen_bytes)).convert("RGB").resize((224, 224))
    arr = np.expand_dims(np.array(img, dtype=np.float32) / 255.0, axis=0)

    # El modelo devuelve [ojos_score, piel_score] por imagen.
    preds = model.predict(arr, verbose=0)
    ojos_score = float(preds[0][0])
    piel_score  = float(preds[0][1])

    return {
        "partes": [
            {
                "parte": "OJO",
                "categoria_ntc": _score_a_categoria(ojos_score),
                "confianza": round(ojos_score, 4),
            },
            {
                "parte": "PIEL",
                "categoria_ntc": _score_a_categoria(piel_score),
                "confianza": round(piel_score, 4),
            },
        ]
    }

# ── Endpoints ──────────────────────────────────────────────────────────────────

@app.get("/")
def root():
    return {
        "servicio": "SECCT Inferencia CNN",
        "modelo": MODELO_H5,
    }


@app.post("/evaluar")
def evaluar(req: EvaluarRequest):
    try:
        imagen_bytes = base64.b64decode(req.imagen_base64)
    except Exception:
        raise HTTPException(status_code=400, detail="imagen_base64 no es base64 válido.")

    if not imagen_bytes:
        raise HTTPException(status_code=400, detail="imagen_base64 está vacío.")

    try:
        return _inferir(imagen_bytes)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Error en inferencia: {e}")


# ── Arranque ────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    print(f"Modelo: {MODELO_H5}")
    uvicorn.run("servicio_inferencia:app", host="0.0.0.0", port=8000, reload=False)
