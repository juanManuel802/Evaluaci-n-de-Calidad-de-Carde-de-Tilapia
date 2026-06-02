# CONTEXTO DEL PROYECTO — INGENIERÍA DE SOFTWARE
## Sistema de Evaluación de Calidad de Carne de Tilapia mediante Redes Neuronales

> **Instrucciones de uso:** Este documento es el contexto base del proyecto semestral.
> Al inicio de cada conversación en este Proyecto de Claude, Claude ya tiene acceso a este contexto de forma persistente.
> Si abres una conversación fuera del Proyecto, adjunta este archivo al inicio.

---

## 1. IDENTIDAD DEL PROYECTO

| Campo | Valor |
|---|---|
| **Nombre del sistema** | Sistema de Evaluación de Calidad de Carne de Tilapia (SECCT) |
| **Materia** | Ingeniería de Software |
| **Docente** | Olga Lucero Vega-Márquez |
| **Metodología** | Proceso Unificado (UP / RUP) — Schach, 8va edición |
| **Fase actual** | Inicio (Inception) |
| **Tecnología de dominio** | Redes neuronales para clasificación/evaluación de calidad |
| **Componente de software** | Software real + documentación RUP completa |

---

## 2. MARCO METODOLÓGICO: PROCESO UNIFICADO (Schach, Cap. 11 y 13)

### 2.1 Flujo de Requisitos (Requirements Workflow)

**Objetivo:** Responder *¿Qué debe poder hacer el producto?*

**Secuencia obligatoria:**
1. Comprender el dominio de la aplicación → construir **glosario**
2. Construir el **modelo de negocio** (procesos del cliente)
3. Derivar los **requisitos iniciales** desde el modelo de negocio
4. **Iterar** los tres pasos anteriores

**Tipos de requisitos:**
- **Funcionales:** acción que el sistema debe poder realizar (entradas/salidas)
- **No funcionales:** restricciones de plataforma, tiempos de respuesta, confiabilidad

**Técnicas de elicitación:**
- Entrevistas (estructuradas: preguntas cerradas; no estructuradas: preguntas abiertas)
- Cuestionarios (cuando se necesita la opinión de muchos individuos)
- Examen de formularios de negocio
- Observación directa

**Artefactos del flujo de requisitos:**
- Glosario del dominio
- Modelo de negocio (descripción de procesos)
- Diagrama de casos de uso (iterado)
- Descripciones de casos de uso con escenarios
- Lista de requisitos funcionales y no funcionales

**Regla sobre actores (Schach):**
- Un actor es quien *inicia* el caso de uso o *proporciona los datos*, no necesariamente quien opera el sistema directamente.
- Un usuario puede jugar múltiples roles; un actor puede participar en múltiples casos de uso.
- Los actores no tienen que ser humanos (sistemas externos también son actores).

**Regla sobre iteración de casos de uso:**
- Los casos de uso se refinan iterativamente a medida que se entiende mejor el dominio.
- Usar relación `«include»` cuando un caso de uso es parte de otro.

---

### 2.2 Flujo de Análisis (Analysis Workflow) — OOA

**Objetivo:** Obtener comprensión profunda de los requisitos y describirlos de forma que produzca diseño e implementación mantenibles.

**Tres tipos de clases de análisis:**

| Tipo | Modela | Notación UML |
|---|---|---|
| **Entity class** | Información de larga vida (datos persistentes del negocio) | Estereotipo `«entity»` |
| **Boundary class** | Interacción entre el producto y el entorno (I/O, pantallas, reportes) | Estereotipo `«boundary»` |
| **Control class** | Cómputos complejos y algoritmos | Estereotipo `«control»` |

**Pasos para extraer clases entidad (iterativo e incremental):**
1. **Modelado funcional:** presentar escenarios de todos los casos de uso
2. **Modelado de clases:** determinar clases entidad, atributos, interrelaciones → diagrama de clases
3. **Modelado dinámico:** determinar operaciones por/hacia cada clase entidad → statechart

**Técnicas para identificar clases entidad:**
- **Extracción de sustantivos (Noun Extraction):**
  - Etapa 1: describir el sistema en un solo párrafo conciso
  - Etapa 2: identificar sustantivos → candidatos a clases
  - Excluir: sustantivos fuera del límite del sistema, sustantivos abstractos (pueden convertirse en atributos)
- **CRC Cards:** Nombre de clase / Responsabilidad / Colaboración

**Cómo extraer clases boundary:**
- Cada pantalla de entrada, pantalla de salida y reporte → su propia clase boundary

**Cómo extraer clases control:**
- Cada cómputo no trivial → clase control

**Anti-patrón a evitar: "God Class"**
- Una clase que concentra demasiada información y demasiado control
- Solución: distribuir control con arquitectura descentralizada

**Realización de casos de uso:**
- Cada caso de uso se realiza como una colaboración de clases de análisis
- El diagrama de colaboración muestra quién habla con quién

---

### 2.3 Flujo de Diseño — Transición a Clean Architecture

**Idea central (Vega-Márquez):**
> "En análisis describimos *qué* hace el sistema. En diseño organizamos *cómo* se implementa sin mezclar responsabilidades."

**Ruta de análisis → diseño:**

| Artefacto de análisis | Resultado en diseño |
|---|---|
| Casos de uso | Use cases / interactors |
| Escenarios | Reglas de negocio |
| Máquina de estados | Comportamiento de entidades |
| Diagrama de colaboración | Puertos y adaptadores |
| Diagrama de secuencia | Algoritmos de aplicación |

**Mapeo Boundary/Control/Entity → Clean Architecture:**

| Análisis (Schach) | Clean Architecture (Vega-Márquez) |
|---|---|
| Boundary | Adaptador de interfaz / Controlador / UI |
| Control | Caso de uso de aplicación / Interactor |
| Entity | Entidad de dominio |

**Regla de dependencias (Clean Architecture):**
```
UI → Use Cases → Entities
Infraestructura implementa interfaces definidas hacia adentro
```

**Regla sobre entidades:**
- No son solo contenedores de datos con getters/setters
- Deben **proteger invariantes** y **expresar comportamiento**

**Regla sobre casos de uso de aplicación:**
- Coordinan entidades
- Aplican reglas de negocio que involucran múltiples objetos
- Dependen de **repositorios/servicios abstractos (puertos)**, no de tecnologías concretas
- No conocen JavaFX, no conocen detalles de base de datos

**Regla sobre puertos:**
- Todo lo que el caso de uso necesita del mundo exterior → puerto (interfaz)
- Los casos de uso deben depender de interfaces, no de MySQL, frameworks, archivos, REST

**5 Transformaciones de análisis a diseño:**
1. Extraer responsabilidades (tabla: elemento → responsabilidad → ubicación en CA)
2. Identificar entidades de dominio
3. Identificar casos de uso de aplicación
4. Derivar puertos
5. Definir adaptadores (entrada y salida)

**Errores comunes a evitar:**
- Toda la lógica en un servicio gigante → mover reglas a entidades, orquestación a Use Cases
- Entidades solo con atributos y getters/setters → las entidades protegen invariantes
- Mezclar lógica de negocio con la UI → el controlador solo captura entrada e invoca el caso de uso
- La UI accediendo directamente a repositorios → pasar siempre por casos de uso
- Olvidar la regla de dependencia → infraestructura depende de puertos, no al revés

**Estructura de paquetes sugerida:**
```
src/
  entities/
  usecases/
    ports/
    services/
    dto/
  adapters/
    ui/
  infrastructure/
    repositories/
```

---

## 3. DESCRIPCIÓN DEL DOMINIO DEL PROYECTO

### 3.1 Contexto del dominio
El sistema evalúa la **calidad de carne de tilapia** utilizando **redes neuronales**. El dominio combina:
- Procesamiento de imágenes o señales de la muestra de tilapia
- Clasificación/evaluación mediante modelo de IA entrenado
- Presentación de resultados al usuario (calidad, categoría, recomendación)

### 3.2 Glosario inicial del dominio (a expandir durante Inception)

| Término | Definición |
|---|---|
| **Tilapia** | Pez de agua dulce ampliamente cultivado; objeto de evaluación del sistema |
| **Calidad de carne** | Conjunto de atributos físicos, químicos u organolépticos que determinan la aptitud de la carne para consumo |
| **Red neuronal** | Modelo computacional de aprendizaje automático usado para clasificación |
| **Muestra** | Unidad de carne de tilapia sometida a evaluación |
| **Clasificación** | Resultado del modelo: categoría de calidad asignada a una muestra |
| **Evaluación** | Proceso completo desde captura de datos hasta emisión de resultado |
| **Modelo entrenado** | Red neuronal con pesos ajustados tras proceso de entrenamiento |
| **Dataset** | Conjunto de muestras etiquetadas usado para entrenar/validar el modelo |

> **Nota:** Este glosario debe expandirse durante el flujo de requisitos mediante entrevistas con expertos del dominio (biólogos, ingenieros de alimentos, productores).

---

## 4. ARTEFACTOS REQUERIDOS POR FASE

### Fase de Inicio (Inception) — actual
- [ ] Glosario del dominio (completo)
- [ ] Modelo de negocio inicial
- [ ] Diagrama de casos de uso inicial
- [ ] Descripción de casos de uso con escenarios (flujo básico y alternativo)
- [ ] Lista de requisitos funcionales y no funcionales
- [ ] Identificación de actores

### Fase de Elaboración
- [ ] Diagrama de clases de análisis (entity, boundary, control)
- [ ] Statecharts de entidades con ciclo de vida
- [ ] Diagramas de colaboración por caso de uso
- [ ] Diagramas de secuencia por caso de uso
- [ ] Tabla de responsabilidades (análisis → Clean Architecture)
- [ ] Arquitectura base (diagrama de paquetes)

### Fase de Construcción
- [ ] Diagrama de clases de diseño
- [ ] Implementación de entidades de dominio
- [ ] Implementación de casos de uso (interactors)
- [ ] Implementación de puertos e interfaces
- [ ] Adaptadores (UI + repositorios en memoria o reales)
- [ ] Integración del modelo de red neuronal

### Fase de Transición
- [ ] Pruebas del sistema
- [ ] Documentación de usuario
- [ ] Despliegue

---

## 5. COMPORTAMIENTO ESPERADO DE CLAUDE EN ESTE PROYECTO

### Modo de trabajo
- Seguir estrictamente la secuencia del Proceso Unificado tal como la enseña la docente Olga Lucero Vega-Márquez (Schach 8va edición)
- Aplicar el mapeo Boundary/Control/Entity → Clean Architecture en la transición análisis-diseño
- Respetar la regla de dependencias de Clean Architecture en todo artefacto de diseño

### Terminología
- Usar los términos exactos del libro de Schach y las presentaciones de la docente
- Entity class, Boundary class, Control class (no "modelo", "vista", "controlador" genérico)
- Use case / interactor, port, adapter (en la capa de diseño)
- Noun extraction, CRC cards (para identificación de clases)

### Evaluación de artefactos
- Al revisar cualquier artefacto (diagrama de clases, casos de uso, escenarios), señalar explícitamente:
  - Si viola la regla de dependencias de Clean Architecture
  - Si hay "God Classes"
  - Si las entidades son anémicas (solo getters/setters sin comportamiento)
  - Si la UI accede directamente a repositorios
  - Si los casos de uso conocen tecnologías concretas

### Restricciones de diseño
- La interfaz de usuario puede ser: web, desktop, o móvil (definir en Inception)
- El modelo de red neuronal es un **servicio externo** → se representa como puerto en Clean Architecture
- Los repositorios de muestras y resultados → puertos con implementación separada

---

## 6. FUENTES DE REFERENCIA

| Archivo | Contenido |
|---|---|
| `Requirements Schach's Slides.pdf` | Cap. 11 — Flujo de Requisitos: dominio, modelo de negocio, casos de uso, requisitos funcionales/no funcionales, iteración |
| `Analysis Workflow.pdf` | Cap. 13 — Flujo de Análisis OOA: entity/boundary/control, noun extraction, CRC cards, statecharts, realización de casos de uso |
| `De_análisis_a_diseño_con_Clean_Architecture.pdf` | Guía de la docente: transformación de artefactos de análisis a Clean Architecture, 5 transformaciones, errores comunes, estructura de paquetes |

---

*Generado a partir de los materiales de la materia. Actualizar cuando la docente proporcione nuevos materiales o cuando el proyecto avance de fase.*
