-- Migración V7: Limpieza de columnas legadas de la tabla log
-- Elimina columnas obsoletas si existen, garantizando compatibilidad con Log.java y DC-Log-transversal

ALTER TABLE log DROP COLUMN IF EXISTS codigo_evento;
ALTER TABLE log DROP COLUMN IF EXISTS id_usuario;
ALTER TABLE log DROP COLUMN IF EXISTS id_lote;
ALTER TABLE log DROP COLUMN IF EXISTS id_evaluacion;
ALTER TABLE log DROP COLUMN IF EXISTS creado_en;
