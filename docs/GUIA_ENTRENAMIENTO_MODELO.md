# Guía de Entrenamiento — Nuevo Modelo CNN para SECCT
**Imágenes completas de tilapia · Integración con `servicio_inferencia.py`**

---

## Punto de partida

El software espera un archivo `models/modelo_entrenado.h5` que cumpla este contrato sin excepción:

| Parámetro | Valor requerido |
|---|---|
| Entrada | Tensor `(1, 224, 224, 3)`, dtype `float32`, valores en `[0.0, 1.0]` |
| Salida | Tensor `(1, N)` — una neurona sigmoide por parte del pez |
| Índice 0 | Score OJO |
| Índice 1 | Score PIEL |
| Índice N | (partes futuras en el mismo orden que `PartePez`) |
| Formato | HDF5 (`.h5`) guardado con `model.save()` |

`servicio_inferencia.py` preprocesa la imagen exactamente así antes de pasarla al modelo:

```python
img = Image.open(...).convert("RGB").resize((224, 224))
arr = np.array(img, dtype=np.float32) / 255.0          # normalización a [0, 1]
arr = np.expand_dims(arr, axis=0)                       # batch size = 1
preds = model.predict(arr)
```

**Todo el pipeline de entrenamiento debe usar el mismo preprocesamiento.** Cualquier diferencia produce un modelo que funciona en entrenamiento pero falla en producción.

---

## Paso 1 — Organizar el dataset

### Estructura de carpetas recomendada

```
dataset/
├── imagenes/
│   ├── tilapia_001.jpg
│   ├── tilapia_002.jpg
│   └── ...
└── etiquetas.csv
```

Las imágenes deben ser fotografías de tilapia **completa**, con buena iluminación, fondo neutro si es posible, y tomadas con el mismo tipo de encuadre (pez de lado, ojo visible).

### Archivo `etiquetas.csv`

Cada fila es una imagen con su categoría NTC por parte, asignada por un experto:

```csv
archivo,cat_ojo,cat_piel
tilapia_001.jpg,1,1
tilapia_002.jpg,2,1
tilapia_003.jpg,3,3
tilapia_004.jpg,4,4
tilapia_005.jpg,5,5
```

- `cat_ojo` y `cat_piel` son enteros de **1 a 5** según NTC 1443.
- Cada parte se etiqueta **independientemente**: un pez puede tener ojo en categoría 4 y piel en categoría 2.
- **Cantidad mínima orientativa:** 150–200 imágenes por categoría por parte. Con menos, el modelo no generaliza bien. 5 categorías × 2 partes → idealmente 1500–2000 imágenes en total, aunque con data augmentation (paso 3) se puede trabajar con menos.

---

## Paso 2 — Convertir categorías a scores

El modelo produce scores continuos en `[0, 1]`. La fórmula que usa el servicio para convertirlos de vuelta a categorías es:

```python
categoria = max(1, min(5, ceil(score × 5)))
```

La inversa exacta para las etiquetas de entrenamiento es:

```python
score = (categoria - 1) / 4.0
```

| Categoría NTC | Score de entrenamiento |
|:---:|:---:|
| 1 (excelente) | 0.00 |
| 2 | 0.25 |
| 3 | 0.50 |
| 4 | 0.75 |
| 5 (no apto) | 1.00 |

```python
import pandas as pd
import numpy as np

df = pd.read_csv("dataset/etiquetas.csv")
df["score_ojo"]  = (df["cat_ojo"]  - 1) / 4.0
df["score_piel"] = (df["cat_piel"] - 1) / 4.0
```

---

## Paso 3 — Cargar imágenes y aplicar preprocesamiento

El preprocesamiento **debe ser idéntico** al de `servicio_inferencia.py`:

```python
from PIL import Image

def cargar_imagen(ruta):
    img = Image.open(ruta).convert("RGB").resize((224, 224))
    return np.array(img, dtype=np.float32) / 255.0

X = np.array([cargar_imagen(f"dataset/imagenes/{f}") for f in df["archivo"]])
y = df[["score_ojo", "score_piel"]].values.astype(np.float32)
# y.shape → (N, 2)
```

### Data augmentation

Con imágenes completas de tilapia el dataset suele ser pequeño. Augmentation multiplica el dataset sin necesitar más fotos:

```python
from tensorflow.keras.preprocessing.image import ImageDataGenerator

aug = ImageDataGenerator(
    rotation_range=10,
    horizontal_flip=True,       # el pez puede estar orientado en cualquier sentido
    brightness_range=[0.8, 1.2],
    zoom_range=0.1,
    width_shift_range=0.05,
    height_shift_range=0.05,
)
```

**No usar** `vertical_flip=True` (un pez boca arriba es una imagen anómala) ni `shear` agresivo.

---

## Paso 4 — Dividir el dataset

```python
from sklearn.model_selection import train_test_split

X_train, X_val, y_train, y_val = train_test_split(
    X, y, test_size=0.2, random_state=42
)
```

Reservar también un conjunto de test completamente separado (10–15 % del total) que no se use en ningún momento durante el entrenamiento ni la validación.

---

## Paso 5 — Definir la arquitectura

Transfer learning sobre MobileNetV2 (misma familia que el modelo actual, liviana y eficiente en CPU):

```python
import tensorflow as tf

base = tf.keras.applications.MobileNetV2(
    input_shape=(224, 224, 3),
    include_top=False,
    weights="imagenet"
)
base.trainable = False   # fase 1: solo entrenar la cabeza

x = tf.keras.layers.GlobalAveragePooling2D()(base.output)
x = tf.keras.layers.Dense(128, activation="relu")(x)
x = tf.keras.layers.Dropout(0.3)(x)
salidas = tf.keras.layers.Dense(2, activation="sigmoid")(x)
# Dense(2, ...) porque son 2 partes: OJO (índice 0) y PIEL (índice 1)

model = tf.keras.Model(inputs=base.input, outputs=salidas)
```

**Por qué `sigmoid` y no `softmax`:** cada neurona de salida evalúa su parte de forma independiente. `softmax` haría que las puntuaciones compitan entre sí sumando 1, lo cual no tiene sentido aquí.

---

## Paso 6 — Entrenar en dos fases

### Fase 1 — Entrenar solo la cabeza (base congelada)

```python
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-3),
    loss="binary_crossentropy",
    metrics=["mae"]
)

history1 = model.fit(
    aug.flow(X_train, y_train, batch_size=32),
    validation_data=(X_val, y_val),
    epochs=20,
    callbacks=[
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True)
    ]
)
```

### Fase 2 — Fine-tuning de las últimas capas de la base

Una vez que la cabeza converge, descongelar las últimas ~30 capas de MobileNetV2 y continuar con learning rate muy bajo para afinar los filtros sin destruir los pesos preentrenados:

```python
base.trainable = True
for layer in base.layers[:-30]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
    loss="binary_crossentropy",
    metrics=["mae"]
)

history2 = model.fit(
    aug.flow(X_train, y_train, batch_size=16),
    validation_data=(X_val, y_val),
    epochs=20,
    callbacks=[
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True)
    ]
)
```

---

## Paso 7 — Evaluar antes de integrar

Antes de reemplazar el modelo en producción, verificar dos cosas:

### 7.1 Métricas sobre el conjunto de test

```python
loss, mae = model.evaluate(X_test, y_test)
print(f"MAE en scores: {mae:.4f}")
# MAE < 0.10 es un buen umbral de referencia (equivale a menos de 0.5 categorías de error)
```

### 7.2 Simular exactamente lo que hará `servicio_inferencia.py`

```python
from PIL import Image
import numpy as np

def inferir_como_el_servicio(ruta_imagen, modelo):
    img = Image.open(ruta_imagen).convert("RGB").resize((224, 224))
    arr = np.expand_dims(np.array(img, dtype=np.float32) / 255.0, axis=0)
    preds = modelo.predict(arr, verbose=0)
    ojos_score = float(preds[0][0])
    piel_score  = float(preds[0][1])
    def categoria(score):
        import math
        return max(1, min(5, math.ceil(score * 5)))
    return {
        "OJO":  {"score": round(ojos_score, 4), "categoria": categoria(ojos_score)},
        "PIEL": {"score": round(piel_score,  4), "categoria": categoria(piel_score)},
    }

# Probar con imágenes conocidas
print(inferir_como_el_servicio("dataset/imagenes/tilapia_001.jpg", model))
```

Si los resultados tienen sentido visual, el modelo está listo.

---

## Paso 8 — Guardar e integrar

```python
model.save("modelo_entrenado.h5")
```

Luego copiar el archivo al directorio del proyecto:

```
models/modelo_entrenado.h5   ← reemplazar este archivo
```

No se requiere ningún cambio en el código Java ni en `servicio_inferencia.py`. El script `iniciar_secct.sh` lo levantará automáticamente en el siguiente arranque.

---

## Consideraciones técnicas importantes

**Compatibilidad Keras 2 / Keras 3:** Si entrenas con TF 2.21+ (Keras 3) y guardas con `model.save()`, el `.h5` tendrá formato Keras 3. `servicio_inferencia.py` ya tiene el fix de `DepthwiseConv2D` que resuelve el problema del argumento `groups`, y usa `compile=False` al cargar, así que el modelo nuevo cargará sin errores.

**Desbalance de clases:** Si el dataset tiene muchas imágenes de categoría 1 y pocas de categoría 5 (lo habitual — el pescado fresco es más fácil de conseguir), el modelo aprenderá a predecir siempre scores bajos. Solución: equilibrar las clases con `class_weight` en `model.fit()` o recolectar más imágenes de categorías 4 y 5.

**Variabilidad de la cámara:** Si las imágenes de entrenamiento fueron tomadas con una cámara y las de producción con otra (diferente resolución, temperatura de color, encuadre), el modelo sufrirá degradación de rendimiento. Incluir en el dataset imágenes tomadas con el mismo dispositivo que se usará en campo.

**Score 0.0 con `ceil`:** La fórmula `ceil(score × 5)` produce 0 cuando `score = 0.0`, pero el código fuerza mínimo 1 con `max(1, ...)`. Un score exactamente 0.0 (predicción perfecta de categoría 1) es manejado correctamente.

**Agregar partes futuras:** Si el nuevo modelo incluye BRANQUIAS u otra parte, solo añadir `Dense(3, activation="sigmoid")` en la capa de salida (3 neuronas), actualizar el índice en `servicio_inferencia.py`, y añadir el valor al enum `PartePez` en Java. El resto del software no cambia.
