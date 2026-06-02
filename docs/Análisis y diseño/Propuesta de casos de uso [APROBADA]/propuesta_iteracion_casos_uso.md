# Propuesta de Iteración de Casos de Uso — Fase de Inicio (Inception)

**Proyecto:** Sistema de Evaluación de Calidad de Carne de Tilapia (SECCT)
**Materia:** Ingeniería de Software
**Docente:** Olga Lucero Vega-Márquez
**Metodología:** Proceso Unificado (Schach, 8va edición)
**Iteración:** 1 → 2 (Refinamiento de Casos de Uso de Inception)

---

## 1. Propósito del documento

Este documento sustenta la **iteración de refinamiento del modelo de casos de uso** realizada por el equipo durante la fase de Inicio, antes de cerrar la línea base de Inception y dar paso al Flujo de Análisis.

Schach (Cap. 11) establece explícitamente que **los casos de uso deben refinarse iterativamente a medida que el equipo entiende mejor el dominio**. Esta iteración aplica ese principio: el listado inicial de 5 casos de uso se redujo a **3 casos de uso primarios** después de discutir el alcance del proyecto y la naturaleza real de cada caso de uso candidato.

---

## 2. Estado inicial (Iteración 1)

El equipo había identificado preliminarmente los siguientes 5 casos de uso:

| # | Caso de Uso (Iteración 1) | Actor declarado |
|---|---|---|
| 1 | Registrar lote | Piscicultor |
| 2 | Preprocesar imagen | Piscicultor |
| 3 | Evaluar calidad IA | Sistema |
| 4 | Generar reporte de lote | Piscicultor |
| 5 | Gestionar sincronización offline | Piscicultor |

---

## 3. Estado refinado (Iteración 2)

Después del análisis, la línea base de Inception queda con **3 casos de uso primarios**:

| # | Caso de Uso (Iteración 2) | Actor primario | Actores secundarios |
|---|---|---|---|
| 1 | Registrar lote | Piscicultor | — |
| 2 | Evaluar calidad de unidad | Piscicultor | Servicio CNN (sistema externo) |
| 3 | Generar reporte de lote | Piscicultor | — |

---

## 4. Justificación detallada de los cambios

A continuación se documenta cada decisión de refinamiento, con su sustento metodológico según Schach.

### 4.1 Eliminación del actor "Administrador" y del RF "Configuración de calidad"

**Cambio:** Se elimina la posibilidad de que un actor configure variables morfológicas y umbrales de clasificación.

**Justificación:**

- El alcance del proyecto define **un único tipo de usuario** (Piscicultor). Mantener un actor Administrador habría obligado a documentar permisos, autenticación diferenciada y casos de uso de configuración que exceden el alcance de la entrega.
- Los umbrales de clasificación **no son una decisión del usuario final**, son **regla de negocio fija** definida por la NTC 1443:
  - Nitrógeno básico volátil total (NBV) ≤ 30 mg/100 g
  - Histamina ≤ 10 mg/100 g (especies específicas)
  - pH entre 5,8 y 6,8
  - Cloruro de sodio ≤ 2 %
  - Ácido sulfhídrico negativo
- En consecuencia, los umbrales quedan **hardcodeados según NTC 1443** y forman parte de las **invariantes de las entidades de dominio** (en el lenguaje de Clean Architecture, que se aplicará en el Flujo de Diseño).

**Resultado:** desaparecen los sustantivos "Administrador", "Umbral configurable" y "Variable morfológica configurable" del modelo de dominio, simplificando significativamente la posterior Extracción de Sustantivos.

---

### 4.2 Reducción del alcance a una sola especie (Tilapia)

**Cambio:** Se elimina del alcance la "Selección de especie" (que contemplaba tilapia + cachama).

**Justificación:**

- El nombre del sistema (**SECCT — Sistema de Evaluación de Calidad de Carne de Tilapia**) ya delimita el dominio a una sola especie.
- El modelo CNN se entrenará exclusivamente con muestras de tilapia, por lo que un selector de especie no tendría modelo asociado para la segunda opción.
- Schach indica que los sustantivos fuera del límite del sistema deben ser **excluidos** de la lista de candidatos a clases. "Cachama", "Selector de especie" y "Modelo por especie" quedan fuera del límite por esta decisión.

**Resultado:** simplificación del dominio y eliminación de ambigüedades para los integrantes del equipo en la redacción de flujos.

---

### 4.3 Eliminación del caso de uso "Gestionar sincronización offline"

**Cambio:** Se elimina del alcance la funcionalidad de modo offline y sincronización.

**Justificación:**

- Por restricciones de tiempo de la entrega, el equipo decidió enfocar el esfuerzo en el núcleo funcional del sistema: registrar, evaluar y reportar.
- La sincronización offline es una **característica transversal de infraestructura**, no un caso de uso con valor de negocio independiente. Schach permite eliminar candidatos a caso de uso cuando no aportan valor observable diferenciado al actor.

**Resultado:** desaparecen del modelo los sustantivos "Cola de sincronización", "Estado de conexión" y "Almacenamiento offline".

---

### 4.4 Integración de "Preprocesar imagen" dentro de "Evaluar calidad de unidad"

**Cambio:** Se elimina "Preprocesar imagen" como caso de uso independiente. Su comportamiento se integra como **pasos del flujo básico** del caso de uso "Evaluar calidad de unidad".

**Justificación metodológica (Schach):**

- Un caso de uso, según Schach, debe representar un **objetivo del actor con valor observable**. El piscicultor **no abre la app pensando "voy a preprocesar una imagen"**; abre la app pensando "voy a evaluar la calidad de esta unidad de mi muestra".
- El preprocesamiento (eliminación de fondo, normalización de luminosidad, ajuste de contraste) es una **responsabilidad técnica interna del sistema**, no un objetivo del actor. Mantenerlo como caso de uso independiente habría violado el principio de que **el caso de uso modela el comportamiento desde la perspectiva del usuario**, no desde la perspectiva técnica interna.
- El preprocesamiento **no tiene flujos alternativos ricos** que justifiquen su separación. Schach indica que la riqueza de flujos alternativos es uno de los criterios para decidir si un comportamiento amerita ser un caso de uso propio.
- Tampoco tiene **valor independiente**: una imagen preprocesada que no se evalúa no entrega valor de negocio al piscicultor.

**Decisión sobre `«include»`:** El equipo evaluó la opción de modelar "Preprocesar imagen" como `«include»`, pero la descartó porque:
- El preprocesamiento no se reutiliza desde otros casos de uso (solo "Evaluar calidad" lo necesita).
- Integrarlo como paso del flujo básico mantiene el diagrama de casos de uso más limpio y centrado en los objetivos del usuario.

**Resultado:** los pasos de preprocesamiento se documentarán dentro del flujo básico del caso de uso 2.

---

### 4.5 Integración de "Cargar imagen" como paso del flujo básico (no como caso de uso ni como `«include»`)

**Cambio:** El equipo decidió que el sistema **no captura imágenes**, sino que **el piscicultor carga imágenes ya tomadas con su dispositivo**. Esta acción de carga **no se modela como caso de uso ni como `«include»`**.

**Justificación metodológica (Schach):**

- Bajo la decisión de "cargar desde el equipo" (no capturar), la carga pierde los flujos alternativos ricos que la habrían justificado como caso de uso (validación de encuadre, distancia, iluminación, reintentos guiados). Esas responsabilidades se trasladan a **el manual de usuario**, no al sistema.
- Lo que queda de la "carga" es: abrir selector de archivos → seleccionar imagen → validar formato. Esto **es un paso, no un caso de uso**.
- Schach establece que un caso de uso debe tener **valor observable independiente**. Tener "una imagen cargada en el sistema" sin evaluarla no aporta valor al piscicultor; solo tiene sentido como **prerrequisito** del flujo de evaluación.
- Por la misma razón se descarta `«include»`: no hay reutilización potencial dentro del alcance actual (solo "Evaluar calidad" necesita cargar imagen), por lo que la separación introduciría complejidad sin beneficio.

**Resultado:** la carga de imagen se documenta como **primer paso del flujo básico** del caso de uso "Evaluar calidad de unidad".

---

### 4.6 Corrección del actor en "Evaluar calidad de unidad"

**Cambio:** En la iteración 1 el actor declarado de "Evaluar calidad IA" era "Sistema". En la iteración 2 se corrige a **Piscicultor como actor primario** y **Servicio CNN como actor secundario (sistema externo)**.

**Justificación metodológica (Schach):**

- Schach es explícito: **un actor es quien inicia el caso de uso o proporciona los datos**. El servicio CNN no inicia nada; es invocado por el sistema. Por tanto, no puede ser el actor primario.
- El piscicultor es quien dispara la evaluación al seleccionar una imagen para evaluar.
- Schach también indica que **los actores no tienen que ser humanos**: los sistemas externos también son actores. El servicio CNN es un **actor secundario externo** que el sistema invoca para obtener la clasificación. Esta distinción es importante para la fase de Diseño, donde el servicio CNN se modelará como un **puerto** (interfaz hacia un sistema externo) bajo Clean Architecture.

**Resultado:** el actor primario es ahora consistente con la definición de Schach, y queda explícita la dependencia del servicio CNN externo.

---

## 5. Modelo de casos de uso refinado

### 5.1 Caso de uso 1 — Registrar lote

| Campo | Contenido |
|---|---|
| **Nombre** | Registrar lote |
| **Actor primario** | Piscicultor |
| **Actores secundarios** | — |
| **Objetivo** | Crear un nuevo lote en el sistema con los datos de origen, especie y volumen estimado, para asociar posteriormente las unidades evaluadas. |
| **Resumen** | El piscicultor registra un nuevo lote indicando estación de origen, fecha de captura, especie (tilapia), peso estimado y unidades aproximadas. El sistema calcula automáticamente la cantidad de unidades a fotografiar para validez estadística y persiste el lote. |
| **Precondiciones** | El piscicultor está autenticado en la app. |
| **Postcondiciones** | Existe un lote registrado, identificado de forma única, con estado "Abierto" y muestra estadística calculada. |

---

### 5.2 Caso de uso 2 — Evaluar calidad de unidad

| Campo | Contenido |
|---|---|
| **Nombre** | Evaluar calidad de unidad |
| **Actor primario** | Piscicultor |
| **Actores secundarios** | Servicio CNN (sistema externo) |
| **Objetivo** | Obtener una clasificación de calidad NTC 1443 para una unidad específica del lote, a partir de una imagen cargada por el piscicultor. |
| **Resumen** | El piscicultor selecciona el lote en curso y carga una imagen de la unidad a evaluar. El sistema preprocesa la imagen (eliminación de fondo, normalización de luminosidad, ajuste de contraste), invoca el servicio CNN externo para clasificar la unidad según la NTC 1443 (con alcance actual centrado en evaluación de ojos), recibe la clasificación, la asocia a la unidad dentro del lote y la muestra al piscicultor. |
| **Precondiciones** | Existe un lote registrado en estado "Abierto" asociado al piscicultor. La imagen está disponible en el dispositivo del piscicultor. |
| **Postcondiciones** | La unidad evaluada queda registrada en el lote con su clasificación de calidad asociada. |
| **Notas sobre pasos integrados** | Los pasos de "cargar imagen" y "preprocesar imagen" se documentan como parte del flujo básico de este caso de uso, no como casos de uso independientes ni como `«include»` (ver justificaciones 4.4 y 4.5). |

> **Nota de asignación:** este caso de uso será desarrollado mediante **pair analysing** por dos integrantes del equipo, dada su mayor complejidad respecto a los otros dos.

---

### 5.3 Caso de uso 3 — Generar reporte de lote

| Campo | Contenido |
|---|---|
| **Nombre** | Generar reporte de lote |
| **Actor primario** | Piscicultor |
| **Actores secundarios** | — |
| **Objetivo** | Consolidar los resultados de las unidades evaluadas en un lote y emitir un reporte para trazabilidad y toma de decisiones. |
| **Resumen** | El piscicultor solicita el reporte de un lote evaluado. El sistema consolida las clasificaciones de todas las unidades evaluadas del lote, calcula los indicadores agregados de calidad según NTC 1443 y emite el reporte. |
| **Precondiciones** | Existe un lote con al menos una unidad evaluada. |
| **Postcondiciones** | El reporte queda generado y disponible para el piscicultor; el lote queda asociado a su reporte histórico. |

---

## 6. Cuadro comparativo: Iteración 1 → Iteración 2

| CU Iteración 1 | Estado en Iteración 2 | Razón |
|---|---|---|
| Registrar lote | Se mantiene | Objetivo claro del actor con valor observable. |
| Preprocesar imagen | Eliminado | Es responsabilidad técnica interna, no objetivo del actor. Integrado como paso del flujo básico de "Evaluar calidad de unidad". |
| Evaluar calidad IA | Renombrado a "Evaluar calidad de unidad". Actor corregido. | Actor primario debe ser el piscicultor (quien inicia), no el sistema. El servicio CNN queda como actor secundario externo. |
| Generar reporte de lote | Se mantiene | Objetivo claro del actor con valor observable. |
| Gestionar sincronización offline | Eliminado | Fuera de alcance de la entrega. No es objetivo de negocio del actor. |

| Concepto nuevo evaluado | Decisión | Razón |
|---|---|---|
| Capturar imagen | Descartado de raíz | Decisión de alcance: el software no captura imágenes. |
| Cargar imagen | Integrado como paso del flujo básico de "Evaluar calidad de unidad" | Sin flujos alternativos ricos ni valor independiente. No amerita ser CU ni `«include»`. |

---

## 7. Trazabilidad de actores

| Actor | Tipo | Casos de uso en los que participa |
|---|---|---|
| Piscicultor | Humano, primario | Registrar lote, Evaluar calidad de unidad, Generar reporte de lote |
| Servicio CNN | Sistema externo, secundario | Evaluar calidad de unidad |

---

## 8. Reparto de trabajo del equipo

| Integrante | Caso de uso asignado | Modalidad |
|---|---|---|
| Integrante 1 | Registrar lote | Individual |
| Integrante 2 + Integrante 3 | Evaluar calidad de unidad | Pair analysing |
| Integrante 4 | Generar reporte de lote | Individual |

---

## 9. Conclusión

Esta iteración deja la línea base de Inception lista para iniciar el **Flujo de Análisis**. Los 3 casos de uso refinados:

- Tienen actor primario consistente con la definición de Schach.
- Tienen objetivo observable desde la perspectiva del usuario.
- No contienen responsabilidades técnicas internas disfrazadas de casos de uso.
- Quedan dentro del alcance de la entrega.
- Son repartibles entre los 4 integrantes sin solapamientos.

A partir de aquí, el equipo procede con la **Etapa 1 de Noun Extraction** sobre la descripción consolidada del sistema, en el marco del Flujo de Análisis.
