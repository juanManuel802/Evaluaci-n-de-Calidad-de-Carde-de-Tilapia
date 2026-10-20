# HU-01: Registrar Usuarios (DC-HU1 y DS-HU1)

## 1. Resumen de Implementación
Se implementó el backend completo de la historia de usuario HU-01: Registrar usuarios, dando cumplimiento riguroso a los diagramas `DC-HU1`, `DS-HU1` y al Plan de Pruebas `PP-001`.

## 2. Componentes Implementados

### 2.1 Entidades JPA (`co.unillanos.secct.entities`)
- [`Role`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/entities/Role.java): Mapeada a `"rol"`, con `id: Short` y `code: String ("codigo")`.
- [`User`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/entities/User.java): Mapeada a `"usuario"`, con `id: Integer`, `role: Role ("rol_id")`, `firstName: String ("nombres")`, `lastName: String ("apellidos")`, `email: String ("correo")`, `phone: String ("telefono")`, `passwordHash: String ("contrasena_hash")`, `salt: String ("salt")`, `active: boolean ("activo")`, `isDeleted: boolean ("is_deleted")`, `deletedAt: OffsetDateTime ("deleted_at")`, `deletedBy: Integer ("deleted_by")`, `createdAt: OffsetDateTime ("created_at")`, `updatedAt: OffsetDateTime ("updated_at")`.

### 2.2 Repositorios JPA (`co.unillanos.secct.infrastructure.repositories`)
- [`RoleRepository`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/infrastructure/repositories/RoleRepository.java): `Optional<Role> findByCode(String code)`.
- [`UserRepository`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/infrastructure/repositories/UserRepository.java): `boolean existsByEmail(String email)` y `Optional<User> findByEmail(String email)`.

### 2.3 Servicios de Criptografía y Dominio (`co.unillanos.secct.usecases.services`)
- [`PasswordService`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/usecases/services/PasswordService.java):
  - `generateSalt(): String`: Generación criptográficamente segura de 16 bytes con `SecureRandom` codificado en Base64 (`PU-01`, `PU-02`).
  - `hashPassword(String password, String salt): String`: Digest SHA-256 combinando salt y password en UTF-8, con longitud fija de 64 caracteres hexadecimales (`PU-03`, `PU-04`, `PU-05`).
  - `verifyPassword(String password, String salt, String passwordHash): boolean`: Comparación en tiempo constante (`MessageDigest.isEqual`) para mitigar ataques de temporización (`PU-06`, `PU-07`).
- [`UserService`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/usecases/services/UserService.java):
  - `normalizeEmail(String email): String`: `email.trim().toLowerCase(Locale.ROOT)` (`PU-09`, `PL-1.17`).
  - `register(RegisterUserRequestDTO request): RegisterUserResponseDTO`: `@Transactional(isolation = Isolation.READ_COMMITTED)`. Valida reglas `PL-1.01` a `PL-1.24`, comprueba duplicados de correo lanzando `BusinessException("correo ya registrado", 409)` (`PL-1.18`, `CN-04`), asocia rol `"PISCICULTOR"` descartando inyecciones externas (`PQ-07`), persiste entidad `User` y registra evento de auditoría `USER_REGISTERED` (`PL-1.26`).

### 2.4 DTOs y Controlador (`co.unillanos.secct.usecases.dto` y `adapters.controllers`)
- [`RegisterUserRequestDTO`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/usecases/dto/RegisterUserRequestDTO.java): Validaciones Bean Validation, permite espacios exteriores en email para ser normalizados (`PL-1.17`), nombres/apellidos de 2 a 50 letras y espacios, teléfono `^3[0-9]{9}$`, contraseña de 8 a 20 caracteres no solo espacios. `toString()` nunca imprime la contraseña en texto claro (`RNF-SEC-04`).
- [`RegisterUserResponseDTO`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/usecases/dto/RegisterUserResponseDTO.java): Expone únicamente `id` y `email` (`PU-08`).
- [`RegistrationController`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/adapters/controllers/RegistrationController.java): Endpoint `POST /api/users` (con alias `POST /api/auth/registro`), responde `201 Created`.
- [`SecurityConfig`](file:///c:/Users/juand/OneDrive/Documentos/Universidad/Semestre%20VI/IS%202/Proyecto%20final/secct/backend/src/main/java/co/unillanos/secct/infrastructure/config/SecurityConfig.java): Permite acceso público (`permitAll()`) al endpoint de registro.

## 3. Matriz de Cobertura de Pruebas Unitarias (Plan de Pruebas PP-001)

| Caso de Prueba | Clase Evaluada | Descripción | Cobertura JaCoCo |
|---|---|---|---|
| **PU-01** | `PasswordService` | `generateSalt()` genera salt que al decodificar representa >= 16 bytes | 90.5% |
| **PU-02** | `PasswordService` | `generateSalt()` produce sales aleatorias y únicas entre llamadas | 90.5% |
| **PU-03** | `PasswordService` | `hashPassword()` genera hash determinista para misma contraseña y salt | 90.5% |
| **PU-04** | `PasswordService` | `hashPassword()` produce hashes distintos para diferentes sales | 90.5% |
| **PU-05** | `PasswordService` | `hashPassword()` retorna hash de longitud fija (64 hex SHA-256) | 90.5% |
| **PU-06** | `PasswordService` | `verifyPassword()` retorna true cuando coinciden credenciales | 90.5% |
| **PU-07** | `PasswordService` | `verifyPassword()` retorna false ante credenciales inválidas | 90.5% |
| **PU-08** | `RegisterUserResponseDTO` | Expone únicamente `id` y `email` sin credenciales | 100.0% |
| **PU-09** | `UserService` | `normalizeEmail()` aplica trim y minúsculas con `Locale.ROOT` | 100.0% |
| **PU-35** | `LogService` | Enmascara correos y teléfonos en description (`RNF-SEC-04`) | 100.0% |
| **PL-1.01..26** | `UserService` | Reglas de negocio completas de registro y auditoría | 100.0% |
| **Validaciones** | `RegisterUserRequestDTO` | Bean Validation y `toString()` seguro | 100.0% |
| **Endpoints** | `RegistrationController` | `POST /api/users` y alias respondiendo 201 Created | 100.0% |
