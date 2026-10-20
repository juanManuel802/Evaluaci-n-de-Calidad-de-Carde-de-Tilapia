# Registro de Dudas y Homologaciones Técnicas (`dudas.md`) — Actualizado

## A. Dudas Resueltas por los Nuevos Artefactos (`_2.md`)
- **[D-01] Stack y Versión de Spring Boot**: **RESUELTA**. `Infraestructura del proyecto web_2.md` confirma **Spring Boot 3.4.1 (Java 21)** y **React 18 + Vite + TypeScript + Tailwind CSS**[cite: 22].
- **[D-02] Nomenclatura de Paquetes y Clases en Inglés**: **RESUELTA**. `Infraestructura del proyecto web_2.md` adopta la estructura Clean Architecture en inglés (`entities`, `usecases`, `adapters`, `infrastructure`) con `User`, `Batch`, `BatchService`, `EvaluationService`, `LogService`, `JwtService`, `JwtAuthenticationFilter` y `GlobalExceptionHandler`[cite: 22].
- **[D-09] Manejo de Sesión JWT Stateless**: **RESUELTA**. `Infraestructura del proyecto web_2.md` confirma `JwtService` (HS256) y `authStorage.ts` en `localStorage`[cite: 22].

---

## B. Homologaciones Activas (Prioridad Diagramas UML + Plan de Pruebas IEEE 829)

### [H-01] Endpoint de Registro en HU-01 (`POST /api/users` vs. `POST /api/auth/registro`)
- **Artefactos en conflicto:** Tabla 2 de `Infraestructura del proyecto web_2.md` (`POST /api/auth/registro`)[cite: 22] vs. `DS-HU1.png`, `Sprint 1_2.md` y `Plan de pruebas IEEE829.docx_2.md` (`PI-01`, `CB-12`: **`POST /api/users`**)[cite: 8, 23, 24].
- **Regla aplicada en código:** En `RegistrationController` se expone **`POST /api/users`** como ruta principal (y opcionalmente `@PostMapping({"/api/users", "/api/auth/registro"})` como alias en el método y en `SecurityConfig`) para garantizar compatibilidad total tanto con `PP-001` como con `Infraestructura del proyecto web_2.md`[cite: 8, 22, 23].

### [H-02] Nombre de la Entidad de Auditoría (`Log.java` vs. `LogEntry.java`)
- **Artefactos en conflicto:** Árbol de `Infraestructura del proyecto web_2.md` (`LogEntry.java`)[cite: 22] vs. `DC-Log-transversal.png` y `Plan de pruebas IEEE829.docx_2.md` (`PI-12`: **`Log`**)[cite: 7, 23].
- **Regla aplicada en código:** Se nombra la entidad JPA **`Log.java`** mapeada a `@Table(name = "log")` conforme a `DC-Log-transversal.png` y al caso `PI-12`[cite: 4, 7, 23].

### [H-03] Endpoints de HU-04 y HU-05 en Tabla 2 de `Infraestructura del proyecto web_2.md` (Preventivo para siguientes ramas)
- **Artefactos en conflicto:** Tabla 2 de `Infraestructura del proyecto web_2.md` (`GET /api/batches`, `POST /api/evaluations/unit`, `POST /api/evaluations/close`, `GET /api/batches?estado=EVALUADO`)[cite: 22] vs. Diagramas `DS-HU4-*.png`, `DS-HU5.png`, `Sprint 1_2.md` y `Plan de pruebas IEEE829.docx_2.md` (`GET /api/batches/available`, `POST /api/evaluations/batches/{code}/units`, `POST /api/evaluations/batches/{code}/close`, `GET /api/batches/evaluated`)[cite: 16, 17, 18, 20, 23, 24].
- **Pregunta para el equipo antes de iniciar HU-04 y HU-05:** ¿Mantenemos en los controladores de lotes y evaluación las rutas exactas de los diagramas de secuencia y del Plan de Pruebas (`PP-001`)[cite: 16, 17, 18, 20, 23]?