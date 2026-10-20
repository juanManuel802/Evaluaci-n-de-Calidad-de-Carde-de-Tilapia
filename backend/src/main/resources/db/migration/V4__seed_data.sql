-- 1. Roles del sistema (Comentario SQL: ADMINISTRADOR, PISCICULTOR)
INSERT INTO "rol" ("codigo") VALUES
  ('ADMINISTRADOR'),
  ('PISCICULTOR')
ON CONFLICT ("codigo") DO NOTHING;

-- 2. Estados de lote con IDs fijos (Comentario SQL: 1 ABIERTO, 2 EN_EVALUACION, 3 EVALUADO, 4 REPORTADO)
INSERT INTO "estado_lote" ("id", "codigo") OVERRIDING SYSTEM VALUE VALUES
  (1, 'ABIERTO'),
  (2, 'EN_EVALUACION'),
  (3, 'EVALUADO'),
  (4, 'REPORTADO')
ON CONFLICT ("codigo") DO NOTHING;

-- 3. Partes del pez (Comentario SQL: OJO, PIEL)
INSERT INTO "parte_pez" ("codigo") VALUES
  ('OJO'),
  ('PIEL')
ON CONFLICT ("codigo") DO NOTHING;

-- 4. Tipos de evento de auditoría (Sección 2 de Plan de pruebas IEEE 829)
INSERT INTO "tipo_evento" ("codigo", "descripcion") VALUES
  ('USER_REGISTERED', 'Registro de nuevo usuario piscicultor'),
  ('LOGIN_SUCCESS', 'Inicio de sesión exitoso'),
  ('LOGIN_FAILED', 'Intento fallido de inicio de sesión'),
  ('LOGOUT', 'Cierre de sesión de usuario'),
  ('BATCH_REGISTERED', 'Registro de nuevo lote de tilapia'),
  ('UNIT_EVALUATED', 'Evaluación de unidad de muestra de tilapia'),
  ('EVALUATION_CLOSED', 'Cierre de evaluación de lote'),
  ('EVALUATED_BATCHES_VIEWED', 'Consulta de lotes evaluados')
ON CONFLICT ("codigo") DO NOTHING;
