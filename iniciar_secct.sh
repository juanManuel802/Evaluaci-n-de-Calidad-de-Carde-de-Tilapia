#!/bin/bash
# Script de arranque del SECCT.
# 1. Lanza el servicio de inferencia CNN (Python/FastAPI) con el modelo entrenado.
# 2. Espera a que el servicio esté listo.
# 3. Abre la aplicación Java/JavaFX.
# 4. Al cerrar la app, apaga el servicio Python.

PROJECT="$(cd "$(dirname "$0")" && pwd)"
VENV="$PROJECT/servicioCNN/.venv"
MODEL="$PROJECT/models/modelo_calidad_tilapia.h5"
SERVICIO="$PROJECT/servicioCNN/servicio_inferencia.py"
LOG_DIR="$PROJECT/.logs"
mkdir -p "$LOG_DIR"

ICON="applications-science"

# ── Validaciones previas ───────────────────────────────────────────────────────

if [ ! -f "$MODEL" ]; then
    notify-send "SECCT — Error" \
        "No se encontró el modelo:\n$MODEL" \
        --icon=dialog-error 2>/dev/null
    zenity --error --title="SECCT — Error" \
        --text="No se encontró el modelo entrenado:\n$MODEL" 2>/dev/null
    exit 1
fi

if [ ! -f "$VENV/bin/python" ]; then
    notify-send "SECCT — Error" \
        "Entorno virtual Python no encontrado en:\n$VENV" \
        --icon=dialog-error 2>/dev/null
    exit 1
fi

# ── Matar instancias previas del servicio ─────────────────────────────────────

pkill -f "servicio_inferencia.py" 2>/dev/null
sleep 0.5

# ── Arrancar servicio Python CNN ──────────────────────────────────────────────

notify-send "SECCT" "Cargando modelo CNN...\nEsto puede tardar unos segundos." \
    --icon="$ICON" 2>/dev/null

SECCT_MODELO_H5="$MODEL" \
    "$VENV/bin/python" "$SERVICIO" \
    > "$LOG_DIR/python.log" 2>&1 &
PY_PID=$!

# ── Esperar a que el servicio responda (máx. 90 s) ────────────────────────────

LISTO=0
for i in $(seq 1 90); do
    if curl -sf http://localhost:8000/ > /dev/null 2>&1; then
        LISTO=1
        break
    fi
    # Si el proceso murió, no tiene sentido seguir esperando
    if ! kill -0 $PY_PID 2>/dev/null; then
        break
    fi
    sleep 1
done

if [ $LISTO -eq 0 ]; then
    notify-send "SECCT — Error" \
        "El servicio CNN no arrancó.\nRevisa el log: $LOG_DIR/python.log" \
        --icon=dialog-error 2>/dev/null
    kill $PY_PID 2>/dev/null
    exit 1
fi

notify-send "SECCT" "Servicio listo. Abriendo aplicación..." \
    --icon="$ICON" 2>/dev/null

# ── Lanzar aplicación Java/JavaFX ────────────────────────────────────────────

cd "$PROJECT"
CNN_URL=http://localhost:8000 mvn -q javafx:run

# ── Apagar servicio Python al cerrar la app ───────────────────────────────────

kill $PY_PID 2>/dev/null
