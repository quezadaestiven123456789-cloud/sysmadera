-- ============================================================
-- Migration: V002__create_number_sequences.sql
-- Descripción: Reemplaza count()+1 por secuencias atómicas
--              para generar order_number e invoice_number.
--
-- Uso en producción (spring.jpa.hibernate.ddl-auto=validate):
--   1. Ejecutar este script contra la base de datos MySQL
--   2. Ajustar los valores iniciales según registros existentes:
--      - ORDER_SEQ  → (SELECT COUNT(*) FROM orders) + 1
--      - INVOICE_SEQ → (SELECT COUNT(*) FROM invoices) + 1
-- ============================================================

CREATE TABLE IF NOT EXISTS number_sequences (
    seq_key  VARCHAR(30) NOT NULL,
    next_val BIGINT      NOT NULL,
    PRIMARY KEY (seq_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Valores iniciales (ajustar según producción):
INSERT INTO number_sequences (seq_key, next_val)
VALUES ('ORDER_SEQ', 1)
ON DUPLICATE KEY UPDATE next_val = next_val;

INSERT INTO number_sequences (seq_key, next_val)
VALUES ('INVOICE_SEQ', 1)
ON DUPLICATE KEY UPDATE next_val = next_val;
