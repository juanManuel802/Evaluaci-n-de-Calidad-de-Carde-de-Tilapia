-- HU-04 Evaluate batch: tables used to store the evaluated units and their NTC 1443 results.
-- All statements are idempotent so they can coexist with tables created by other stories.

CREATE TABLE IF NOT EXISTS parte_pez (
    id      SMALLINT     PRIMARY KEY,
    codigo  VARCHAR(30)  NOT NULL UNIQUE
);

INSERT INTO parte_pez (id, codigo) VALUES
    (1, 'OJO'),
    (2, 'PIEL')
ON CONFLICT (codigo) DO NOTHING;

CREATE TABLE IF NOT EXISTS evaluacion (
    id                 SERIAL        PRIMARY KEY,
    id_lote            INTEGER       NOT NULL REFERENCES lote (id),
    referencia_imagen  VARCHAR(100)  NOT NULL,
    evaluado_en        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_evaluacion_lote_imagen UNIQUE (id_lote, referencia_imagen)
);

CREATE INDEX IF NOT EXISTS idx_evaluacion_lote ON evaluacion (id_lote);

CREATE TABLE IF NOT EXISTS punto_evaluacion (
    id             SERIAL        PRIMARY KEY,
    id_evaluacion  INTEGER       NOT NULL REFERENCES evaluacion (id),
    id_parte_pez   SMALLINT      NOT NULL REFERENCES parte_pez (id),
    categoria_ntc  SMALLINT      NOT NULL CHECK (categoria_ntc BETWEEN 1 AND 5),
    confianza      NUMERIC(5,4)  NOT NULL CHECK (confianza >= 0 AND confianza <= 1),
    CONSTRAINT uq_punto_evaluacion_parte UNIQUE (id_evaluacion, id_parte_pez)
);

CREATE INDEX IF NOT EXISTS idx_punto_evaluacion_evaluacion ON punto_evaluacion (id_evaluacion);
