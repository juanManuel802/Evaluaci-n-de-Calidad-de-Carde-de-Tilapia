# Reglas de Orquestación de Agentes — Proyecto SECCT (Sprint 1)

## 1. Stack y Arquitectura Obligatoria
- **Backend**: Java 21 + Spring Boot 3.4.1 + Maven + Spring Data JPA + Bean Validation + Flyway + PostgreSQL (Supabase).
- **Frontend**: React 18 + Vite + TypeScript + Tailwind CSS + HTML5 + API nativa `fetch` (prohibido usar Axios)[cite: 6, 22].
- **Paquete base Backend**: `co.unillanos.secct` estructurado en `entities`, `usecases.{dto,exceptions,ports,services}`, `adapters.{controllers,security}` e `infrastructure.{config,classifier,repositories,security}`[cite: 22].
- **Nomenclatura**: `camelCase` estricto en código y apego exacto a los nombres de clases, métodos y firmas de los diagramas `DC-HU1`, `DC-HU2`, `DC-Log-transversal`, `DS-HU1` y `DS-HU2`[cite: 6, 7, 8, 9, 10, 11, 12, 23].

## 2. Reglas de Seguridad, Persistencia y RNF (Inquebrantables)
1. **Cero secretos en código (`CB-05`, `DoD-04`)**: Prohibido escribir contraseñas de BD, URLs con credenciales o llaves de firma JWT en el código fuente. Todo debe leerse por variables de entorno en `application.yml`[cite: 23, 24].
2. **Hashing de contraseñas (`RNF-SEC-01`, `CB-07`)**: `PasswordService` debe usar exclusivamente `SHA-256` con un salt aleatorio de al menos 16 bytes generado con `java.security.SecureRandom`[cite: 23, 24].
3. **Sanitización de Logs (`RNF-SEC-04`, `CB-13`, `PE-06`, `PE-07`)**: Nunca registrar contraseñas, hashes, salts, tokens JWT ni PII (correos o teléfonos) en texto claro en `LogService` ni en la salida de consola/archivos de Logback[cite: 21, 23, 24].
4. **Transaccionalidad y Soft Delete (`RNF-REL-01`, `CB-08`, `CB-09`)**: Nivel de aislamiento `READ_COMMITTED`. Prohibido ejecutar borrados físicos (`DELETE`); usar siempre `isDeleted`, `deletedAt` y `deletedBy`[cite: 4, 23, 24].
5. **Consultas Seguras (`CB-04`)**: Prohibido concatenar cadenas en consultas SQL/JPQL; usar métodos derivados de `JpaRepository` o parámetros con `@Param`[cite: 23].
6. **Manejo de Errores (`PQ-15`)**: `GlobalExceptionHandler` debe interceptar cualquier excepción y devolver mensajes claros sin exponer stack traces ni detalles internos de SQL[cite: 22, 23].

## 3. Criterio de Aceptación y Calidad (IEEE 829)
- Toda clase de la capa `usecases/services` e `infrastructure/security` debe contar con pruebas unitarias JUnit 5 + Mockito que alcancen **>= 80% de cobertura en JaCoCo (`CB-03`)** y pasen el 100% de los casos `PU-*` asignados en el Plan de Pruebas `PP-001`[cite: 23, 24].