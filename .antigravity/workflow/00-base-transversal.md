# Capa Transversal de Auditoría (DC-Log-transversal)

## 1. Resumen de Implementación
Se implementó la capa transversal de auditoría (`DC-Log-transversal`) conforme al estándar de seguridad y privacidad `RNF-SEC-04` y al Plan de Pruebas `PP-001`.

## 2. Componentes Implementados

### 2.1 Entidades JPA (`co.unillanos.secct.entities`)
- [`EventType`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/entities/EventType.java): Tabla `tipo_evento` con `id: Short`, `code: String ("codigo")`, `description: String ("descripcion")`.
- [`Log`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/entities/Log.java): Tabla `log` con `id: Long`, `eventType: EventType ("tipo_evento_id")`, `userId: Integer ("usuario_id")`, `batchId: Integer ("lote_id")`, `evaluationId: Integer ("evaluacion_id")`, `occurredAt: OffsetDateTime ("fecha_hora")`, `description: String ("descripcion")`.

### 2.2 Repositorios JPA (`co.unillanos.secct.infrastructure.repositories`)
- [`EventTypeRepository`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/infrastructure/repositories/EventTypeRepository.java): `Optional<EventType> findByCode(String code)`.
- [`LogRepository`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/infrastructure/repositories/LogRepository.java): Extiende `JpaRepository<Log, Long>`.

### 2.3 Servicio de Auditoría (`co.unillanos.secct.usecases.services`)
- [`LogService`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/usecases/services/LogService.java):
  - Método `recordEvent(String eventCode, Integer userId, Integer batchId, Integer evaluationId, String description): void`.
  - Resuelve `EventType` dinámicamente mediante `eventTypeRepository.findByCode(eventCode)`.
  - Sanitiza y enmascara datos sensibles PII (correo electrónico y teléfonos) según `RNF-SEC-04` mediante `maskSensitiveData(description)`.
  - Registra fecha y hora actual en formato `OffsetDateTime.now()`.

### 2.4 Pruebas Unitarias
- [`LogServiceTest`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/test/java/co/unillanos/secct/usecases/services/LogServiceTest.java):
  - **PU-35**: Sanitización y enmascaramiento de correos y teléfonos en description (`RNF-SEC-04`). Cobertura: 100%.
