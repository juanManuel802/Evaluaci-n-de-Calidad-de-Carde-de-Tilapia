-- HU-03 Register batch: tables used by the batch registration flow.
-- All statements are idempotent so they can coexist with tables created by other stories.

CREATE TABLE IF NOT EXISTS usuario (
    id      SERIAL       PRIMARY KEY,
    email   VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS estado_lote (
    id      SMALLINT     PRIMARY KEY,
    codigo  VARCHAR(30)  NOT NULL UNIQUE
);

INSERT INTO estado_lote (id, codigo) VALUES
    (1, 'ABIERTO'),
    (2, 'EN_EVALUACION'),
    (3, 'EVALUADO')
ON CONFLICT (codigo) DO NOTHING;

CREATE TABLE IF NOT EXISTS lote (
    id                      SERIAL        PRIMARY KEY,
    codigo                  VARCHAR(30)   NOT NULL UNIQUE,
    fecha_captura           DATE          NOT NULL,
    peso_kg                 NUMERIC(7,2)  NOT NULL CHECK (peso_kg >= 0.01 AND peso_kg <= 10000.00),
    numero_unidades_muestra INTEGER       NOT NULL CHECK (numero_unidades_muestra BETWEEN 1 AND 500),
    observaciones           VARCHAR(500),
    clasificacion_final     NUMERIC(4,2),
    id_usuario              INTEGER       NOT NULL REFERENCES usuario (id),
    id_estado_lote          SMALLINT      NOT NULL REFERENCES estado_lote (id),
    eliminado               BOOLEAN       NOT NULL DEFAULT FALSE,
    eliminado_en            TIMESTAMPTZ,
    eliminado_por           INTEGER       REFERENCES usuario (id),
    creado_en               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    actualizado_en          TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_lote_fecha_captura ON lote (fecha_captura);
CREATE INDEX IF NOT EXISTS idx_lote_usuario       ON lote (id_usuario);

CREATE TABLE IF NOT EXISTS log (
    id             BIGSERIAL    PRIMARY KEY,
    codigo_evento  VARCHAR(50)  NOT NULL,
    id_usuario     INTEGER      REFERENCES usuario (id),
    id_lote        INTEGER      REFERENCES lote (id),
    id_evaluacion  INTEGER,
    descripcion    VARCHAR(500),
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_log_codigo_evento ON log (codigo_evento);
