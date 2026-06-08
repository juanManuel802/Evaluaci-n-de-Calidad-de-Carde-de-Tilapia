# Restricciones de integración del modelo — SECCT

> [!NOTE]
> Este documento describe lo que el modelo **debe** cumplir para funcionar en SECCT.
>
> Cómo se entrena, qué arquitectura se usa y cómo se organiza el dataset son decisiones libres del equipo de ML, siempre que se respeten los puntos aquí descritos.

---

## 📑 Contenido

* [1. Archivo del modelo](#1-archivo-del-modelo)
* [2. Preprocesamiento de entrada](#2-preprocesamiento-de-entrada--debe-ser-idéntico-al-del-servicio)
* [3. Forma y semántica de la salida](#3-forma-y-semántica-de-la-salida)
* [4. Etiquetas de entrenamiento](#4-etiquetas-de-entrenamiento--conversión-ntc-a-score)
* [5. Qué hace el servicio con la salida](#5-qué-hace-el-servicio-con-la-salida-referencia)
* [6. Compatibilidad TF 2.21 / Keras 3](#6-nota-técnica-de-compatibilidad-tf-221--keras-3)
* [7. Agregar partes futuras](#7-agregar-partes-futuras)

---

## 1. Archivo del modelo

El script de arranque (`iniciar_secct.sh`) busca el modelo en esta ruta exacta:

```text
models/modelo_entrenado.h5
```

El archivo debe estar en formato HDF5 y guardado con:

```python
model.save("modelo_entrenado.h5")
```

> [!WARNING]
> Si la ruta o el formato no coinciden, el sistema no arranca.

---

## 2. Preprocesamiento de entrada — debe ser idéntico al del servicio

Java envía la imagen como bytes en base64. El servicio Python la procesa así antes de pasarla al modelo:

```python
img = Image.open(io.BytesIO(imagen_bytes)).convert("RGB").resize((224, 224))
arr = np.expand_dims(np.array(img, dtype=np.float32) / 255.0, axis=0)
preds = model.predict(arr)
```

El modelo recibe un tensor de forma:

```text
(1, 224, 224, 3)
```

con valores en:

```text
[0.0, 1.0]
```

> [!IMPORTANT]
> El pipeline de entrenamiento debe aplicar exactamente el mismo preprocesamiento.
>
> Cualquier diferencia — otra normalización, otro orden de canales o un tamaño de entrada distinto — produce un modelo que puntúa bien en entrenamiento pero falla en producción sin generar errores visibles.

---

## 3. Forma y semántica de la salida

La capa de salida del modelo debe cumplir:

| Requisito                  | Valor                    |
| -------------------------- | ------------------------ |
| Forma del tensor de salida | `(1, N)` con `N ≥ 2`     |
| Rango de cada neurona      | `[0.0, 1.0]`             |
| Activación de salida       | **sigmoid** (no softmax) |
| Índice 0                   | Score del **OJO**        |
| Índice 1                   | Score de la **PIEL**     |

> [!IMPORTANT]
> Se debe utilizar **sigmoid**, no **softmax**.

### ¿Por qué sigmoid?

Cada parte se evalúa de forma independiente.

Con `softmax`, los scores competirían entre sí sumando 1, lo que no tiene sentido semántico en este sistema.

> [!WARNING]
> El orden de los índices está escrito directamente en `servicio_inferencia.py` (líneas 96–99).
>
> No existe ningún mecanismo de auto-detección.
>
> Si el índice 0 no corresponde a OJO, las categorías quedarán invertidas sin que el sistema lo detecte.

---

## 4. Etiquetas de entrenamiento — conversión NTC a score

El servicio convierte el score del modelo a categoría NTC mediante:

```python
categoria_ntc = max(1, min(5, ceil(score * 5)))
```

Para que el modelo aprenda a producir scores coherentes con esa fórmula, las etiquetas de entrenamiento deben usar la inversa exacta:

```python
score = (categoria - 1) / 4.0
```

|  Categoría NTC | Score de entrenamiento |
| :------------: | :--------------------: |
| 1 (muy fresco) |          0.00          |
|        2       |          0.25          |
|        3       |          0.50          |
|        4       |          0.75          |
|   5 (no apto)  |          1.00          |

Cada parte (ojo, piel) se etiqueta de forma independiente.

Un pez puede tener:

| Parte | Categoría |
| ----- | --------- |
| OJO   | 1         |
| PIEL  | 4         |

sin ningún problema.

---

## 5. Qué hace el servicio con la salida (referencia)

Para que quede claro qué procesa el sistema y qué no:

* El servicio transforma los scores a categorías NTC.
* El servicio construye la respuesta JSON.
* Java interpreta el campo `"parte"` mediante `PartePez.valueOf(...)`.

### Ejemplo de respuesta

```json
{
  "partes": [
    {
      "parte": "OJO",
      "categoria_ntc": 3,
      "confianza": 0.5
    },
    {
      "parte": "PIEL",
      "categoria_ntc": 1,
      "confianza": 0.08
    }
  ]
}
```

Los strings válidos son exactamente:

```text
OJO
PIEL
```

> [!NOTE]
> El modelo no interactúa directamente con JSON, enums ni lógica de negocio.
>
> Solo produce el tensor numérico; el servicio realiza todo el procesamiento posterior.

---

## 6. Nota técnica de compatibilidad (TF 2.21 / Keras 3)

El entorno validado es:

| Componente | Versión |
| ---------- | ------- |
| Python     | 3.13    |
| TensorFlow | 2.21    |
| Keras      | 3       |

El servicio ya incluye los siguientes ajustes al cargar el modelo:

### `compile=False`

Se omiten el optimizador y la función de pérdida al cargar.

Esto implica que:

* Un modelo guardado con Keras 2 puede cargarse sin problemas de deserialización.
* El modelo se utiliza únicamente para inferencia.
* No es necesario recompilarlo.

### Compatibilidad con `DepthwiseConv2D`

El servicio incluye una subclase de compatibilidad que elimina el argumento `groups` antes de construir la capa.

Si la arquitectura utiliza `DepthwiseConv2D` (por ejemplo, MobileNet y variantes), el ajuste ya está cubierto.

> [!TIP]
> No es necesario realizar configuraciones especiales al guardar el modelo para aprovechar estas compatibilidades.

---

## 7. Agregar partes futuras

Si una nueva versión del modelo evalúa más partes:

1. Añadir neuronas a la capa de salida.

   ```python
   Dense(3, activation="sigmoid")
   ```

2. Añadir el valor correspondiente al enum `PartePez` en Java.

3. Actualizar `servicio_inferencia.py` para incluir el nuevo índice dentro de la respuesta.

> [!SUCCESS]
> El resto del sistema (Java, script de arranque y lógica de evaluación) no requiere cambios.
