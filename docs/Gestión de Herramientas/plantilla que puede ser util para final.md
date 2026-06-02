# SECCT — Documento de Proyecto

## Sistema de Escritorio para Evaluar la Calidad de Carne de Tilapia

**Universidad de los Llanos — Ingeniería de Sistemas**

---

## 1. REQUERIMIENTOS

### 1.1 Descripción general

SECCT (Sistema de Escritorio para Evaluar la Calidad de Carne de Tilapia) es una aplicación de escritorio que permite al piscicultor registrar lotes de tilapia roja (*Oreochromis spp.*) y evaluar su calidad sensorial conforme a la norma colombiana NTC 1443. El flujo principal consiste en: registrar un lote capturando sus datos de procedencia y características físicas, seleccionar ese lote para evaluación, cargar imágenes de unidades individuales para que el sistema las clasifique mediante un servicio de red neuronal convolucional (CNN), y finalmente cerrar la evaluación para obtener la clasificación global del lote. El resultado de cada unidad evaluada es una categoría NTC entre 1 y 5; la clasificación final del lote es el promedio aritmético de todas las categorías registradas.

El sistema está orientado a la cadena de trazabilidad: el piscicultor puede evaluar en cuatro puntos posibles — estación piscícola, centro de acopio, distribuidor mayorista o plaza de mercado.

---

### 1.2 Diagrama de casos de uso general

> *[Insertar diagrama de casos de uso aquí]*

---

### 1.3 Casos de uso principales

---

#### CU-001 — Registrar Lote

##### Diagrama específico
> *[Insertar diagrama de CU-001 aquí]*

##### Descripción
Permite al piscicultor crear el registro inicial de un lote de tilapia en el sistema. El sistema genera automáticamente un código único de trazabilidad con formato `[PREFIJO]-[AAAAMMDD]-[NNN]`. El piscicultor completa los datos del lote: estación de origen, fecha de captura, peso total estimado, número de unidades de muestra comprometidas, punto de evaluación en la cadena y observaciones opcionales. El lote queda registrado con estado `ABIERTO`, disponible para que se inicien evaluaciones de unidades individuales. Este registro no implica ninguna evaluación de calidad; es exclusivamente un acto de captura de datos e identidad del lote.

##### Paso a paso
1. El piscicultor solicita registrar un nuevo lote.
2. El sistema genera el código único del lote y lo presenta en el formulario.
3. El piscicultor completa los campos: estación de origen, fecha de captura, peso total, número de unidades de muestra, punto de evaluación y observaciones (opcional).
4. El piscicultor confirma el registro pulsando "Guardar lote".
5. El sistema valida los campos ingresados.
6. El sistema persiste el lote con estado `ABIERTO`.
7. El sistema confirma el registro indicando el código asignado.

##### Escenario normal
1. El piscicultor solicita registrar un nuevo lote.
2. El sistema genera el código `SECCT-20260531-001` y presenta el formulario con los campos: estación de origen, fecha de captura, peso total, número de unidades de muestra, punto de evaluación y observaciones.
3. El piscicultor ingresa: estación de origen "Estación Piscícola Meta", fecha de captura 24/05/2025, peso total 120.50 kg, 15 unidades de muestra, punto de evaluación "Estación Piscícola".
4. El piscicultor pulsa "Guardar lote".
5. El sistema valida cada campo sin encontrar infracciones.
6. El sistema persiste el lote con estado `ABIERTO`.
7. El sistema muestra: "Lote 'SECCT-20260531-001' registrado exitosamente. Estado: ABIERTO. Listo para evaluación de unidades de muestra."

##### Escenario alterno A — Dato obligatorio ausente o inválido
*Variación del paso 5 del escenario normal.*

1. El piscicultor pulsa "Guardar lote" con la estación de origen vacía.
2. El sistema detecta que la estación de origen es obligatoria.
3. El sistema muestra el mensaje de error correspondiente.
4. El formulario permanece abierto con los datos ingresados para que el piscicultor los corrija.

*El flujo se reconecta en el paso 3 del escenario normal.*

---

#### CU-002 — Seleccionar Lote

##### Diagrama específico
> *[Insertar diagrama de CU-002 aquí]*

##### Descripción
El piscicultor consulta los lotes disponibles para evaluación y selecciona uno. El sistema presenta únicamente los lotes con estado `ABIERTO` o `EN_EVALUACION`, mostrando el progreso de evaluación de cada uno (unidades evaluadas sobre el total comprometido). Al seleccionar, el sistema verifica que el lote no haya agotado su cuota de unidades de muestra.

##### Paso a paso
1. El piscicultor ingresa a la sección de evaluación.
2. El sistema presenta la lista de lotes en estado `ABIERTO` o `EN_EVALUACION`, indicando el progreso de evaluación de cada uno.
3. El piscicultor selecciona un lote de la lista.
4. El sistema verifica que el lote esté disponible: estado válido y cuota de unidades no agotada.
5. El sistema confirma la selección e informa el progreso actual.

##### Escenario normal
1. El piscicultor ingresa a la sección de evaluación.
2. El sistema muestra: "SECCT-20260531-001 (3/15)", "SECCT-20260531-002 (0/10)".
3. El piscicultor selecciona "SECCT-20260531-001".
4. El sistema verifica que el lote está en estado `EN_EVALUACION` y que 3 es menor que 15.
5. El sistema muestra: "Lote 'SECCT-20260531-001' seleccionado. 3/15 unidades evaluadas."

##### Escenario alterno A — No hay lotes disponibles
*Variación del paso 2 del escenario normal.*

1. El piscicultor ingresa a la sección de evaluación.
2. El sistema no presenta ningún lote en la lista.

##### Escenario alterno B — Lote con cuota de muestra completa
*Variación del paso 4 del escenario normal.*

1. El piscicultor ingresa a la sección de evaluación.
2. El sistema muestra: "SECCT-20260531-001 (15/15)".
3. El piscicultor selecciona "SECCT-20260531-001".
4. El sistema verifica que el lote ha alcanzado la cantidad comprometida de unidades de muestra o no admite más evaluaciones en su estado actual.
5. El sistema muestra el mensaje de error indicando la causa.

---

#### CU-003 — Evaluar Unidad

##### Diagrama específico
> *[Insertar diagrama de CU-003 aquí]*

##### Descripción
Permite al piscicultor obtener la clasificación de calidad NTC 1443 de cada unidad individual del lote seleccionado. El piscicultor carga el archivo de imagen de una unidad, el sistema invoca el servicio CNN que analiza la imagen y devuelve la categoría NTC (1 a 5) junto con un puntaje de confianza. La evaluación queda registrada en el lote. El proceso puede repetirse hasta que se alcance el número de unidades de muestra comprometidas al registrar el lote.

##### Paso a paso
1. Con un lote seleccionado, el piscicultor elige el archivo de imagen de una unidad.
2. El piscicultor confirma la evaluación pulsando "Evaluar unidad".
3. El sistema verifica que el lote esté disponible para nuevas evaluaciones.
4. El sistema envía la imagen al servicio CNN.
5. El servicio CNN devuelve la categoría NTC y el puntaje de confianza.
6. El sistema registra la evaluación en el lote y persiste el resultado.
7. El sistema muestra la categoría obtenida y el progreso actualizado.

##### Escenario normal
1. El piscicultor tiene seleccionado el lote "SECCT-20260531-001" (3/15 unidades evaluadas).
2. El piscicultor pulsa "Examinar…" y selecciona el archivo `tilapia_004.jpg`.
3. El piscicultor pulsa "Evaluar unidad".
4. El sistema verifica que el lote está disponible (estado `EN_EVALUACION`, 3 < 15).
5. El sistema envía la imagen al servicio CNN.
6. El servicio CNN devuelve: categoría NTC 3, puntaje de confianza 0.90.
7. El sistema registra la evaluación y persiste el lote (progreso: 4/15).
8. El sistema muestra: "Unidad evaluada. Imagen: tilapia_004.jpg. Categoría NTC: 3. Confianza: 0.9. Evaluadas: 4/15."

##### Escenario alterno A — No hay lotes disponibles
*Variación del paso 2 del escenario normal.*

1. El piscicultor intenta evaluar una unidad.
2. El sistema no muestra ningún lote en la lista.

##### Escenario alterno B — Última unidad de muestra
*Variación del paso 7 del escenario normal.*

1. El piscicultor tiene seleccionado el lote "SECCT-20260531-001" con 14/15 unidades evaluadas.
2. El piscicultor carga la imagen de la unidad y pulsa "Evaluar unidad".
3. El sistema evalúa la unidad, registra la evaluación y persiste el lote (progreso: 15/15).
4. El sistema muestra el resultado de la clasificación.
5. El sistema informa que el lote ha alcanzado la cantidad comprometida de unidades; el botón "Evaluar unidad" queda deshabilitado.

---

#### CU-004 — Evaluar Lote

##### Diagrama específico
> *[Insertar diagrama de CU-004 aquí]*

##### Descripción
Permite al piscicultor cerrar la evaluación de un lote que se encuentra en estado `EN_EVALUACION`. El sistema calcula la clasificación final del lote como el promedio aritmético de las categorías NTC de todas sus evaluaciones individuales, persiste el resultado y transita el lote al estado `EVALUADO`. Es una acción atómica e irreversible en el alcance actual: el lote no admite nuevas evaluaciones una vez en estado `EVALUADO`.

##### Paso a paso
1. Con un lote seleccionado en estado `EN_EVALUACION`, el piscicultor solicita cerrar la evaluación.
2. El sistema verifica que el lote está en estado `EN_EVALUACION`.
3. El sistema verifica que el lote tiene al menos una evaluación registrada.
4. El sistema calcula el promedio aritmético de las categorías NTC de todas las evaluaciones.
5. El sistema persiste la clasificación final y transita el lote a estado `EVALUADO`.
6. El sistema notifica al piscicultor la clasificación obtenida.

##### Escenario normal
1. El piscicultor tiene seleccionado el lote "SECCT-20260531-001" con 15 evaluaciones registradas (categorías: 3, 4, 3, 2, 4, 3, 3, 4, 3, 3, 2, 3, 4, 3, 3).
2. El piscicultor pulsa "Cerrar evaluación del lote".
3. El sistema verifica que el lote está en estado `EN_EVALUACION`.
4. El sistema verifica que hay 15 evaluaciones registradas.
5. El sistema calcula el promedio: 47 / 15 = 3.13.
6. El sistema persiste la clasificación final 3.13 y transita el lote a `EVALUADO`.
7. El sistema muestra: "Lote 'SECCT-20260531-001' evaluado. Clasificación: 3.13."

##### Escenario alterno A — Lote sin evaluaciones
*Variación del paso 3 del escenario normal.*

1. El piscicultor pulsa "Cerrar evaluación del lote" sobre un lote sin evaluaciones registradas.
2. El sistema detecta que el lote no tiene ninguna evaluación.
3. El sistema muestra: "No es posible evaluar el lote. Debe tener al menos una unidad evaluada."

##### Escenario alterno B — Lote en estado no evaluable
*Variación del paso 2 del escenario normal.*

1. El piscicultor pulsa "Cerrar evaluación del lote" sobre un lote en estado diferente a `EN_EVALUACION`.
2. El sistema detecta que el estado actual no admite el cierre.
3. El sistema muestra: "El lote no se encuentra en estado de evaluación (estado actual: [estado])."

---

## 2. ANÁLISIS

### 2.1 Diagrama de clases entidad

> *[Insertar diagrama de clases entidad aquí]*

Las entidades del dominio son `Lote` y `Evaluacion`. Los atributos se expresan con tipos de datos comunes. Los enumeradores `EstadoLote` y `PuntoEvaluacion` se representan como listas constantes asociadas a la entidad.

**Clase `Lote`**

| Atributo | Tipo | Restricción |
|---|---|---|
| codigo | String | Formato `[PREFIJO]-[AAAAMMDD]-[NNN]`, máx 30 caracteres, único en el sistema |
| estacionOrigen | String | Obligatorio, máx 100 caracteres |
| fechaCaptura | String | Obligatorio, no posterior a la fecha actual |
| pesoTotal | double | Obligatorio, mayor o igual a 0.01, dos decimales |
| numeroUnidadesMuestra | int | Obligatorio, mayor o igual a 1 |
| puntoEvaluacion | PuntoEvaluacion | Obligatorio; uno de los cuatro valores del enumerador |
| estado | EstadoLote | Valor inicial siempre `ABIERTO` |
| observaciones | String | Opcional, máx 500 caracteres |
| clasificacionFinal | double | Calculado al cerrar la evaluación; 0.0 hasta ese momento |

**Clase `Evaluacion`**

| Atributo | Tipo | Restricción |
|---|---|---|
| idImagen | String | Nombre del archivo de imagen; obligatorio, no vacío |
| clasificacion | int | Categoría NTC 1443; valor entre 1 y 5 inclusive |

`Evaluacion` está asociada a su `Lote` de forma inmutable desde la creación.

**Enumerador `EstadoLote`**

`ABIERTO` | `EN_EVALUACION` | `EVALUADO` | `REPORTADO`

**Enumerador `PuntoEvaluacion`**

`ESTACION_PISCICOLA` | `CENTRO_ACOPIO` | `DISTRIBUIDOR_MAYORISTA` | `PLAZA_MERCADO`

---

### 2.2 Tarjetas CRC

---

**CRC — Lote**

| Responsabilidades | Colaboradores |
|---|---|
| Mantener la identidad única del lote mediante su código | `Evaluacion` |
| Nacer siempre con estado `ABIERTO` al ser registrado | |
| Rechazar la construcción si algún atributo obligatorio está ausente o viola una regla de dominio | |
| Aceptar nuevas evaluaciones mientras esté en estado `ABIERTO` o `EN_EVALUACION` | |
| Transitar de `ABIERTO` a `EN_EVALUACION` al recibir la primera evaluación | |
| Rechazar nuevas evaluaciones cuando esté en estado `EVALUADO` o `REPORTADO` | |
| Informar si está disponible para nuevas evaluaciones (estado válido y cuota no agotada) | |
| Calcular la clasificación final como promedio aritmético de las clasificaciones al cerrar | |
| Transitar de `EN_EVALUACION` a `EVALUADO` al cerrar la evaluación | |
| Rechazar el cierre si no está en `EN_EVALUACION` o si no tiene ninguna evaluación registrada | |

---

**CRC — Evaluacion**

| Responsabilidades | Colaboradores |
|---|---|
| Almacenar el identificador del archivo de imagen de la unidad evaluada | `Lote` |
| Almacenar la clasificación NTC 1443 recibida del servicio de análisis (valor 1 a 5) | |
| Mantener inmutable su asociación con el lote al que pertenece | |

---

### 2.3 Diagramas de colaboración

> *[Insertar diagrama de colaboración CU-001 — Registrar Lote aquí]*

> *[Insertar diagrama de colaboración CU-002 — Seleccionar Lote aquí]*

> *[Insertar diagrama de colaboración CU-003 — Evaluar Unidad aquí]*

> *[Insertar diagrama de colaboración CU-004 — Evaluar Lote aquí]*

---

## 3. DISEÑO

### 3.1 Maquetas de interfaz

> *[Insertar maquetas de interfaz aquí]*

---

### 3.2 Diagrama de arquitectura

> *[Insertar diagrama de arquitectura aquí]*

La arquitectura sigue el modelo **Clean Architecture (Vega-Márquez)** con separación estricta en capas. Las dependencias apuntan siempre hacia adentro: la UI no conoce la infraestructura; la infraestructura implementa interfaces definidas en los casos de uso.

En esta fase de diseño los atributos primitivos del análisis se transforman en **objetos con lógica de validación autónoma** (Value Objects) y en enumeraciones:

- **`CodigoLote`** — encapsula el código del lote; valida el formato `[PREFIJO]-[AAAAMMDD]-[NNN]` (solo mayúsculas, dígitos y guiones) y la longitud máxima de 30 caracteres. Soporta comparación por valor.
- **`FechaCaptura`** — encapsula la fecha de captura; rechaza valores nulos y fechas posteriores a la fecha actual del sistema.
- **`PesoLote`** — encapsula el peso total del lote; normaliza a dos decimales y rechaza valores menores a 0.01 kg.
- **`«enumeration» PuntoEvaluacion`** — cuatro constantes: `ESTACION_PISCICOLA`, `CENTRO_ACOPIO`, `DISTRIBUIDOR_MAYORISTA`, `PLAZA_MERCADO`.
- **`«enumeration» EstadoLote`** — cuatro constantes: `ABIERTO`, `EN_EVALUACION`, `EVALUADO`, `REPORTADO`.

**Máquina de estados de `Lote`:**

```
[ABIERTO] ──(primera evaluación registrada)──▶ [EN_EVALUACION] ──(cerrarEvaluacion)──▶ [EVALUADO]
    │                                                  │
    └──────── acepta nuevas evaluaciones ◀─────────────┘
```

El estado `REPORTADO` existe en el dominio pero corresponde a un caso de uso fuera del alcance actual.

---

### 3.3 Diagramas de secuencia

> *[Insertar diagrama de secuencia CU-001 — Registrar Lote aquí]*

> *[Insertar diagrama de secuencia CU-002 — Seleccionar Lote aquí]*

> *[Insertar diagrama de secuencia CU-003 — Evaluar Unidad aquí]*

> *[Insertar diagrama de secuencia CU-004 — Evaluar Lote aquí]*

---

## 4. IMPLEMENTACIÓN

### 4.1 Estructura de Arquitectura Limpia

El código fuente se organiza bajo el paquete raíz `co.unillanos.secct` en las siguientes capas, con dependencias estrictamente hacia adentro:

| Capa | Paquete | Componentes |
|---|---|---|
| Entidades | `entities` | `Lote`, `Evaluacion`, `CodigoLote`, `FechaCaptura`, `PesoLote`, `PuntoEvaluacion`, `EstadoLote` |
| Puertos | `usecases/ports` | `LoteRepository`, `GeneradorCodigoLotePort`, `ClasificadorCnnPort` |
| DTOs | `usecases/dto` | `DatosNuevoLote`, `OperationResult`, `ResultadoClasificacion` |
| Casos de uso | `usecases/services` | `RegistrarLoteUseCase`, `SeleccionarLoteUseCase`, `EvaluarUnidadUseCase`, `EvaluarLoteUseCase`, `SecctApp` |
| Adaptadores UI | `adapters/ui` | `PantallaRegistrarLote`, `PantallaEvaluarCalidad`, `Main`, `InicializadorDatos` |
| Infraestructura | `infrastructure/repositories` | `InMemoryLoteRepository`, `GeneradorCodigoLoteSecuencial`, `FakeClasificadorCnn` |

La UI invoca exclusivamente métodos de la fachada `SecctApp`. Los controladores de pantalla nunca acceden directamente a repositorios, entidades mutables ni al clasificador CNN.

---

### 4.2 Casos de uso separados

Cada caso de uso es una clase independiente que recibe sus dependencias por constructor (puertos abstractos) y expone un método principal que retorna `OperationResult`:

- **`RegistrarLoteUseCase`** — construye y persiste un nuevo `Lote` a partir de un `DatosNuevoLote`. Valida secuencialmente cada campo y retorna el primer error encontrado. Depende de: `LoteRepository`, `GeneradorCodigoLotePort`. Expone adicionalmente `obtenerCodigoNuevoLote()` para precarga del formulario.
- **`SeleccionarLoteUseCase`** — lista lotes disponibles (`ABIERTO` o `EN_EVALUACION`) y valida la disponibilidad del lote seleccionado por el piscicultor. También lista lotes evaluados (`EVALUADO` o `REPORTADO`). Depende de: `LoteRepository`.
- **`EvaluarUnidadUseCase`** — verifica disponibilidad del lote, invoca el servicio CNN con la ruta de imagen, construye la `Evaluacion` con el resultado y la registra en el lote. Depende de: `LoteRepository`, `ClasificadorCnnPort`.
- **`EvaluarLoteUseCase`** — verifica que el lote esté en `EN_EVALUACION` y tenga al menos una evaluación, delega el cierre y cálculo del promedio al propio lote, y persiste el estado `EVALUADO`. Depende de: `LoteRepository`.

---

### 4.3 Fachada de aplicación — `SecctApp`

`SecctApp` es el único punto de entrada entre la interfaz de usuario y los casos de uso. Centraliza la creación de los cuatro interactores en su constructor y expone los siguientes métodos:

| Método | Caso de uso delegado |
|---|---|
| `obtenerCodigoNuevoLote()` | `RegistrarLoteUseCase` |
| `registrarLote(DatosNuevoLote)` | `RegistrarLoteUseCase` |
| `listarLotesDisponibles()` | `SeleccionarLoteUseCase` |
| `listarLotesEvaluados()` | `SeleccionarLoteUseCase` |
| `seleccionarLote(String loteId)` | `SeleccionarLoteUseCase` |
| `evaluarUnidad(String loteId, Path imagen)` | `EvaluarUnidadUseCase` |
| `evaluarLote(String loteId)` | `EvaluarLoteUseCase` |

---

### 4.4 Repositorios e interfaces de abstracción (puertos)

Los tres puertos definen el contrato en términos del dominio, sin acoplarse a ninguna tecnología concreta:

- **`LoteRepository`** — `findById`, `findByEstadoIn`, `save`, `existsByCodigo`.
- **`GeneradorCodigoLotePort`** — `generarCodigoLote()`.
- **`ClasificadorCnnPort`** — `clasificar(imagen)` → `ResultadoClasificacion`.

Las implementaciones actuales son adaptadores intermediarios para desarrollo y pruebas:

- **`InMemoryLoteRepository`** — almacén en memoria con mapa ordenado por inserción. Implementa `LoteRepository`.
- **`GeneradorCodigoLoteSecuencial`** — genera códigos con prefijo `SECCT` y secuencial de tres dígitos sobre la fecha del día. Implementa `GeneradorCodigoLotePort`.
- **`FakeClasificadorCnn`** — retorna siempre categoría NTC 3 con puntaje de confianza 0.90. Implementa `ClasificadorCnnPort`.

---

## 5. DIFERENCIAS ENTRE EL PLAN DE IMPLEMENTACIÓN Y EL CÓDIGO FINAL

Durante el desarrollo se presentaron ajustes respecto a los documentos de planeación originales. Se listan a continuación:

---

### 5.1 Reporte de errores de validación en CU-001

**Plan:** el escenario alternativo A de CU-001 describía que el sistema detecta y notifica *todas* las infracciones encontradas en un mismo envío del formulario (ej. campo vacío y fecha futura al mismo tiempo).

**Código:** la validación es secuencial — el sistema reporta el *primer* error encontrado y detiene el proceso. El piscicultor debe corregir y volver a intentar para ver el siguiente error, si existiera.

---

### 5.2 Prefijo del código de lote generado automáticamente

**Plan:** los documentos de CU usaban `ESTMETA` como prefijo de ejemplo en los códigos de lote (ej. `ESTMETA-20250524-001`), sugiriendo un prefijo basado en la ubicación de la estación.

**Código:** `GeneradorCodigoLoteSecuencial` usa `SECCT` como prefijo fijo por defecto (ej. `SECCT-20260531-001`). Los códigos con prefijo `ESTMETA` del escenario normal solo aparecen en `InicializadorDatos`, donde se cargan manualmente como datos de prueba al iniciar la aplicación.

---

### 5.3 Generación del código como paso separado del registro

**Plan:** el flujo de CU-001 planteaba la generación del código como un paso interno del caso de uso `RegistrarLoteUseCase.execute()`, integrado al momento de guardar.

**Código:** `RegistrarLoteUseCase` expone un método independiente `obtenerCodigoNuevoLote()` que la UI invoca al abrir el formulario para pre-cargar el campo código. El código generado se pasa luego como parte del DTO `DatosNuevoLote` al guardar. Esto implica que es posible generar un código que nunca se use si el usuario no completa el registro.

---

### 5.4 La selección de lote no persiste estado

**Plan:** CU-002 describía la selección como una acción que "marca" o reserva el lote para evaluación, sugiriendo implícitamente un cambio de estado persistido.

**Código:** `SeleccionarLoteUseCase.execute()` solo valida la disponibilidad del lote y retorna un `OperationResult`. No persiste ningún cambio de estado. La referencia al lote seleccionado la mantiene la pantalla `PantallaEvaluarCalidad` en una variable local.

---

### 5.5 Método `listarLotesEvaluados()` no planificado

**Plan:** CU-002 únicamente planificaba listar lotes en estado `ABIERTO` o `EN_EVALUACION`.

**Código:** `SeleccionarLoteUseCase` incluye un método adicional `listarEvaluados()` que retorna lotes en estado `EVALUADO` o `REPORTADO`. Este método fue añadido durante la implementación de la fachada `SecctApp`, aunque no tiene uso activo en la UI actual.

---

### 5.6 Origen del `idImagen` en `Evaluacion`

**Plan:** los documentos de CU-003 describían `idImagen` como un identificador de la imagen "cargada por el piscicultor", sin precisar cómo se derivaba.

**Código:** `idImagen` se obtiene automáticamente del nombre del archivo seleccionado (`imagen.getFileName().toString()`). El piscicultor no ingresa ningún identificador manualmente; lo provee el sistema a partir de la ruta del archivo elegido.

---

### 5.7 El lote no recibe la imagen en CU-003

**Plan:** el diagrama de colaboración de CU-003 incluía pasos "Pasa imagen" y "Asociar imagen unidad" con mensajes dirigidos directamente a la entidad `Lote`, lo que resultaba ambiguo.

**Código:** `Lote` no recibe ni conoce la imagen. El flujo real es: el caso de uso invoca al `ClasificadorCnnPort` con la ruta de imagen, recibe un `ResultadoClasificacion`, construye con ese resultado una `Evaluacion`, y la pasa a `lote.registrarEvaluacion(evaluacion)`. El lote opera únicamente sobre objetos `Evaluacion`, nunca sobre rutas o archivos.

---

### 5.8 Cobertura del escenario alterno B de CU-004

**Plan:** el escenario alternativo B de CU-004 describía únicamente el caso en que el lote está en estado `ABIERTO` como condición no evaluable.

**Código:** la verificación compara contra cualquier estado distinto de `EN_EVALUACION`. Por lo tanto, un lote en estado `EVALUADO` o `REPORTADO` también activa este escenario, no solo `ABIERTO`. El mensaje de error incluye el estado actual para mayor claridad.

---

### 5.9 Carga de datos de prueba al iniciar

**Plan:** ningún documento de CU mencionaba la existencia de datos precargados al arrancar la aplicación.

**Código:** la clase `InicializadorDatos`, invocada desde `Main` al iniciar, registra dos lotes de prueba con códigos `ESTMETA-20250524-001` y `ESTMETA-20250523-001`. Esto permite probar el sistema sin necesidad de registrar lotes manualmente en cada sesión.

---
