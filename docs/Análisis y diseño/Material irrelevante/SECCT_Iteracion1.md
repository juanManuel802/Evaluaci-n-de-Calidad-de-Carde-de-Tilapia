# Iteración 1 — Flujo de Análisis (Analysis Workflow)
## Etapa 1 de Noun Extraction y Glosario del Dominio

---

## 1. Descripción consolidada del sistema en un solo párrafo

El Sistema de Evaluación de Calidad de Carne de Tilapia (SECCT) es una aplicación móvil que permite a un piscicultor evaluar la calidad de unidades de tilapia de un lote, según los criterios de la Norma Técnica Colombiana NTC 1443. El piscicultor registra un lote indicando su estación de origen, fecha de captura, especie, peso estimado y unidades aproximadas; el sistema calcula automáticamente el tamaño de la muestra estadística a evaluar. Para cada unidad de la muestra, el piscicultor carga una imagen previamente tomada con su dispositivo; el sistema preprocesa la imagen aplicando eliminación de fondo, normalización de luminosidad y ajuste de contraste, e invoca un servicio externo de red neuronal convolucional (CNN) que analiza la región ocular de la unidad y retorna una clasificación de calidad según los criterios sensoriales definidos en la NTC 1443. El sistema asocia cada clasificación a la unidad correspondiente dentro del lote. Una vez evaluadas las unidades de la muestra, el piscicultor solicita la generación de un reporte que consolida las clasificaciones obtenidas, calcula los indicadores agregados de calidad del lote según los criterios de aceptación de la NTC 1443, y queda almacenado en el histórico del piscicultor para trazabilidad.

> **Nota metodológica:** este párrafo cumple la Etapa 1 de Noun Extraction de Schach. Es la única descripción sobre la cual se aplica la identificación de sustantivos en la Etapa 2.

---

## 2. Lista preliminar de clases entidad candidatas

Resultado consolidado del filtrado:

| Clase candidata | Tipo | Razón |
|-----------------|------|-------|
| Lote | Fuerte candidato | Identidad propia, estado (abierto/cerrado/reportado), agrega unidades, tiene atributos propios. |
| Unidad | Fuerte candidato | Identidad propia, pertenece a un lote, tiene una clasificación asociada. |
| Imagen | Fuerte candidato | Identidad propia, atributos de archivo, ciclo de vida (cargada → preprocesada → evaluada). |
| Clasificación | Fuerte candidato | Identidad propia, resultado del servicio CNN, atributos propios (categoría, confianza, fecha). |
| Reporte | Fuerte candidato | Identidad propia, asociado a un lote, contiene indicadores agregados. |
| Estación | Candidato dudoso | Podría ser clase entidad o atributo simple del lote. Decidir en próxima iteración. |
| Muestra | Candidato dudoso | Podría ser clase entidad (subconjunto del lote) o un cálculo derivado. Decidir en próxima iteración. |

**Actor identificado:** Piscicultor (humano, primario).  
**Actor secundario externo identificado:** Servicio CNN (sistema externo, será puerto en Diseño).

---

## 3. Glosario del dominio (Postura A — vocabulario NTC 1443 completo)

El glosario se divide en dos secciones para evitar confusión entre el vocabulario del negocio y el vocabulario del software.

### 3.1 Sección A — Términos del dominio NTC 1443 (vocabulario del negocio)

| Término | Definición |
|---------|------------|
| NTC 1443 | Norma Técnica Colombiana que establece los requisitos del pescado entero, medallones y trozos, refrigerados o congelados aptos para consumo humano. Es la regla de negocio principal del sistema. |
| Pescado entero | Presentación de un producto pesquero eviscerado o no, con o sin escamas, cabeza, cola, desangrado. Es la presentación que evalúa el SECCT. |
| Medallones / rodajas / postas | Porciones obtenidas por cortes transversales a la espina dorsal del pescado eviscerado sin cabeza. Fuera del alcance del SECCT en su iteración actual; documentado para coherencia con la NTC. |
| Trozos | Porciones obtenidas por cortes irregulares transversales o longitudinales a la espina dorsal del pescado eviscerado sin cabeza. Fuera del alcance del SECCT en su iteración actual. |
| Fresco | Pescado entero recién capturado, mantenido a temperatura entre 0 °C y 4 °C. |
| Refrigerado | Pescado entero mantenido a temperatura entre 0 °C y 4 °C usando hielo triturado (proporción 2:1) u otros medios de enfriamiento. |
| Congelado | Pescado entero llevado y mantenido a -18 °C o menos en el centro térmico. |
| Análisis sensorial | Evaluación de calidad mediante criterios visuales (piel, carne, ojos, branquias, color, textura) realizada históricamente por experto humano; el SECCT automatiza parcialmente esta evaluación enfocándose en la región ocular. |
| Defecto | Característica que hace que una unidad de muestra sea considerada no conforme. La NTC 1443 define defectos en: deshidratación profunda, materias extrañas, olor/sabor/color, textura, desgarramiento del abdomen. |
| Deshidratación profunda | Defecto: pérdida excesiva de humedad en más del 10 % de la superficie o peso de la unidad de muestra, manifiesta en color blanco o amarillo que no se elimina raspando. |
| Materias extrañas | Defecto: cualquier materia ajena al pescado presente en la unidad de muestra que constituya peligro para la salud humana. |
| Alteración de textura | Defecto: estructura demasiado blanda o pastosa del músculo, separación de la carne de las espinas, o carne gelatinosa con humedad superior al 86 %. |
| Desgarramiento del abdomen | Defecto en pescados no eviscerados: presencia de desgarramiento indica descomposición. |
| Plan de muestreo | Procedimiento estadístico definido por la NTC 1443 (basado en Codex Alimentarius CAC/GL 50-2004, AQL-6.5) para determinar cuántas unidades evaluar de un lote. |
| AQL (Acceptance Quality Limit) | Nivel aceptable de calidad usado en los planes de muestreo. El plan referenciado por la NTC 1443 usa AQL 6.5. |
| Unidad de muestra | Empaque primario o, cuando el producto se presente a granel, un ejemplar de pescado evaluado individualmente. |
| Aceptación del lote | Decisión final sobre si un lote cumple los requisitos de la NTC 1443, basada en el número de unidades defectuosas y los criterios físico-químicos, microbiológicos y de contaminantes. |
| Bases volátiles totales (NBV) | Indicador físico-químico de frescura. Límite NTC 1443: ≤ 30 mg/100 g. Fuera del alcance del SECCT visual; se documenta porque es parte del dominio. |
| Histamina | Indicador físico-químico. Límite NTC 1443: ≤ 10 mg/100 g en especies de familias Clupeidae, Scombridae, Scombresocidae, Pomatomidae, Coryphaenedae (no aplica directamente a tilapia, pero forma parte de la norma). |
| pH | Indicador físico-químico. Rango NTC 1443: 5,8 a 6,8 a 20 °C. |
| Cloruro de sodio | Indicador físico-químico. Límite NTC 1443: ≤ 2 % en fracción de masa. |
| Ácido sulfhídrico | Indicador físico-químico. NTC 1443: debe dar resultado negativo. |
| Coliformes / E. coli / Salmonella / Vibrio cholerae / Staphylococcus aureus | Indicadores microbiológicos con límites definidos en la Tabla 2 de la NTC 1443. Fuera del alcance del SECCT visual. |
| Metales pesados (Cadmio, Mercurio, Plomo) | Contaminantes con límites máximos definidos en la Tabla 3 de la NTC 1443. Fuera del alcance del SECCT visual. |

### 3.2 Sección B — Términos del sistema SECCT (vocabulario del software)

| Término | Definición |
|---------|------------|
| SECCT | Sistema de Evaluación de Calidad de Carne de Tilapia. Nombre del software. |
| Piscicultor | Actor humano primario del sistema. Persona responsable del cultivo y manejo de tilapia que utiliza el SECCT para evaluar lotes. |
| Lote | Conjunto de unidades de tilapia de una misma especie y procedencia capturadas en una sesión, sobre el cual se realiza una evaluación de calidad. Tiene identidad propia en el sistema. |
| Estación de origen | Instalación piscícola de la cual proviene un lote. Identifica el origen del producto para trazabilidad. |
| Especie | Característica del lote. En el alcance actual del SECCT su valor es siempre "tilapia". |
| Unidad | Cada pescado individual perteneciente a un lote que es evaluado por el sistema. Tiene una clasificación asociada. |
| Muestra | Subconjunto de unidades de un lote seleccionadas para evaluación, según el tamaño calculado por el plan de muestreo. |
| Tamaño de muestra estadística | Cantidad de unidades del lote que deben evaluarse para que la evaluación sea estadísticamente válida según el plan de muestreo de la NTC 1443. |
| Imagen | Archivo fotográfico de una unidad de la muestra, cargado por el piscicultor desde su dispositivo. Es entrada del proceso de evaluación. |
| Preprocesamiento | Conjunto de operaciones automáticas aplicadas por el sistema a una imagen antes de enviarla al servicio CNN: eliminación de fondo, normalización de luminosidad y ajuste de contraste. No es caso de uso, es paso del flujo. |
| Servicio CNN | Sistema externo basado en una red neuronal convolucional que recibe una imagen preprocesada y retorna una clasificación de calidad. Es actor secundario del sistema y se modelará como puerto en la fase de Diseño. |
| Clasificación | Resultado de la evaluación de una unidad. Asigna a la unidad una categoría de calidad según los criterios sensoriales de la NTC 1443. |
| Región ocular | Aspecto visual de la unidad que la CNN analiza en el alcance actual del SECCT. La NTC 1443 contempla otros criterios sensoriales (piel, carne, branquias, textura, color, olor) que quedan fuera del alcance actual. |
| Reporte | Documento generado por el sistema que consolida las clasificaciones de todas las unidades evaluadas de un lote y calcula indicadores agregados de calidad según los criterios de aceptación de la NTC 1443. |
| Indicador agregado de calidad | Métrica calculada a partir del conjunto de clasificaciones de un lote (ej: porcentaje de unidades aceptables, porcentaje de defectuosas). Forma parte del reporte. |
| Histórico | Colección de reportes asociados a un piscicultor. Soporta la trazabilidad del producto. |
| Trazabilidad | Capacidad del sistema de mostrar el origen, evaluación e histórico de un lote, alineada con la guía GTC 157 referenciada en la NTC 1443. |

---

## 4. Cierre de la iteración 1

### ✅ Lo que queda cerrado en esta iteración

- Descripción del sistema en un solo párrafo.
- Sustantivos identificados y clasificados según los filtros de Schach.
- Lista preliminar de 5 clases entidad fuertes (**Lote, Unidad, Imagen, Clasificación, Reporte**) + 2 candidatos dudosos (**Estación, Muestra**) que se resolverán en próxima iteración.
- Glosario del dominio dividido en vocabulario del negocio (NTC 1443) y vocabulario del software (SECCT).

### ⏳ Lo que queda explícitamente pendiente para próximas iteraciones

- Decidir si **Estación** es clase entidad o atributo del lote.
- Decidir si **Muestra** es clase entidad o cálculo derivado del lote.
- Construir las **CRC Cards** de las clases entidad confirmadas.
- Identificar **clases boundary** (una por cada pantalla y reporte).
- Identificar **clases control** (una por cada cómputo no trivial), cuidando de no caer en una God Class.
- Iniciar el **modelado dinámico** (statecharts) de las clases entidad.
