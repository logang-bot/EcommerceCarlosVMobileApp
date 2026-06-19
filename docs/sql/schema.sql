-- ============================================================
-- CarlosVCommerce — Supabase Database Schema
-- Run these statements in the Supabase SQL editor.
-- Execute in order: extensions → tables (dependency order).
-- RLS is enabled inline so Supabase does not prompt about it.
-- Run rls.sql afterwards to add the actual access policies.
-- ============================================================

-- Required extension for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "pgcrypto";


-- ────────────────────────────────────────────────────────────
-- updated_at trigger function
-- Sets updated_at to the current time (epoch ms) on every UPDATE.
-- Attached to mercados, clientes, productos, pedidos below.
-- detalle_pedido is intentionally excluded — lines are always
-- deleted and re-inserted as a block when their parent pedido changes.
-- ────────────────────────────────────────────────────────────
CREATE OR REPLACE FUNCTION set_updated_at_ms()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at := (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT;
    RETURN NEW;
END;
$$;


-- ────────────────────────────────────────────────────────────
-- users
-- Populated by the app via the admin API when a SUPERUSUARIO
-- creates a new user. The `id` matches the Supabase Auth UID.
-- `biometric_enabled_at` is intentionally absent — device-local only.
-- ────────────────────────────────────────────────────────────
CREATE TABLE users (
    id              uuid        PRIMARY KEY,                 -- matches auth.users.id
    email           text        NOT NULL UNIQUE,
    name            text        NOT NULL,
    role            text        NOT NULL DEFAULT 'USUARIO',  -- 'SUPERUSUARIO' | 'USUARIO'
    phone           text,
    photo_url       text,
    is_active       boolean     NOT NULL DEFAULT true,
    created_at      bigint      NOT NULL,                    -- epoch ms
    last_seen_at    bigint                                   -- epoch ms; updated on login
);

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE INDEX idx_users_email ON users (email);


-- ────────────────────────────────────────────────────────────
-- mercados
-- Shared resource; all authenticated users can read and write.
-- ────────────────────────────────────────────────────────────
CREATE TABLE mercados (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        text        NOT NULL,
    address     text        NOT NULL DEFAULT '',
    photo_url   text,
    maps_url    text,
    latitude    float8,
    longitude   float8,
    created_at  bigint      NOT NULL,
    updated_at  bigint      NOT NULL DEFAULT 0,  -- epoch ms; auto-set by trigger on UPDATE
    is_deleted  boolean     NOT NULL DEFAULT false
);

ALTER TABLE mercados ENABLE ROW LEVEL SECURITY;
CREATE INDEX idx_mercados_name       ON mercados (name);
CREATE INDEX idx_mercados_updated_at ON mercados (updated_at);

CREATE TRIGGER trg_mercados_updated_at
    BEFORE UPDATE ON mercados
    FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();


-- ────────────────────────────────────────────────────────────
-- clientes
-- Belongs to a mercados row. Represents a customer stall.
-- ────────────────────────────────────────────────────────────
CREATE TABLE clientes (
    id                         uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    mercado_id                 uuid        NOT NULL REFERENCES mercados (id) ON DELETE CASCADE,
    name                       text        NOT NULL,
    description                text        NOT NULL DEFAULT '',
    photo_url                  text,
    phones                     text        NOT NULL DEFAULT '',  -- pipe-separated: "0414-123|0424-456"
    primary_phone_index        int4        NOT NULL DEFAULT 0,
    maps_url                   text,
    is_blacklisted             boolean     NOT NULL DEFAULT false,
    blacklist_reason           text,
    blacklisted_at             bigint,                           -- epoch ms
    blacklist_balance          float8      NOT NULL DEFAULT 0.0,
    blacklist_is_manual_amount boolean     NOT NULL DEFAULT false,
    created_at                 bigint      NOT NULL,
    updated_at                 bigint      NOT NULL DEFAULT 0,  -- epoch ms; auto-set by trigger on UPDATE
    is_deleted                 boolean     NOT NULL DEFAULT false
);

ALTER TABLE clientes ENABLE ROW LEVEL SECURITY;
CREATE INDEX idx_clientes_mercado_id   ON clientes (mercado_id);
CREATE INDEX idx_clientes_name         ON clientes (name);
CREATE INDEX idx_clientes_blacklisted  ON clientes (is_blacklisted);
CREATE INDEX idx_clientes_updated_at   ON clientes (updated_at);

CREATE TRIGGER trg_clientes_updated_at
    BEFORE UPDATE ON clientes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();


-- ────────────────────────────────────────────────────────────
-- productos
-- Global product catalogue shared across all users.
-- ────────────────────────────────────────────────────────────
CREATE TABLE productos (
    id          uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    name        text        NOT NULL,
    description text,
    price       float8      NOT NULL,
    photo_url   text,
    is_active   boolean     NOT NULL DEFAULT true,
    created_at  bigint      NOT NULL,
    updated_at  bigint      NOT NULL DEFAULT 0,  -- epoch ms; auto-set by trigger on UPDATE
    is_deleted  boolean     NOT NULL DEFAULT false
);

ALTER TABLE productos ENABLE ROW LEVEL SECURITY;
CREATE INDEX idx_productos_name       ON productos (name);
CREATE INDEX idx_productos_updated_at ON productos (updated_at);

CREATE TRIGGER trg_productos_updated_at
    BEFORE UPDATE ON productos
    FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();


-- ────────────────────────────────────────────────────────────
-- pedidos
-- A delivery order. Belongs to a clientes row.
-- Saldo Extra entries are stored here with is_saldo_extra = true.
-- ────────────────────────────────────────────────────────────
CREATE TABLE pedidos (
    id             uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    cliente_id     uuid        NOT NULL REFERENCES clientes (id) ON DELETE CASCADE,
    status         text        NOT NULL DEFAULT 'pending',   -- 'pending' | 'partial' | 'paid'
    total          float8      NOT NULL DEFAULT 0,
    paid           float8      NOT NULL DEFAULT 0,
    notes          text,
    is_saldo_extra boolean     NOT NULL DEFAULT false,
    created_at     bigint      NOT NULL,
    paid_at        bigint,                                   -- epoch ms; updated on each payment
    updated_at     bigint      NOT NULL DEFAULT 0,           -- epoch ms; auto-set by trigger on UPDATE
    is_deleted     boolean     NOT NULL DEFAULT false
);

ALTER TABLE pedidos ENABLE ROW LEVEL SECURITY;
CREATE INDEX idx_pedidos_cliente_id ON pedidos (cliente_id);
CREATE INDEX idx_pedidos_status     ON pedidos (status);
CREATE INDEX idx_pedidos_created_at ON pedidos (created_at DESC);
CREATE INDEX idx_pedidos_updated_at ON pedidos (updated_at);

CREATE TRIGGER trg_pedidos_updated_at
    BEFORE UPDATE ON pedidos
    FOR EACH ROW EXECUTE FUNCTION set_updated_at_ms();


-- ────────────────────────────────────────────────────────────
-- detalle_pedido
-- Line items for a pedido. Denormalized product_name and unit_price
-- preserve historical accuracy after a product is edited.
-- ────────────────────────────────────────────────────────────
CREATE TABLE detalle_pedido (
    id            uuid    PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id     uuid    NOT NULL REFERENCES pedidos (id) ON DELETE CASCADE,
    producto_id   uuid    NOT NULL REFERENCES productos (id) ON DELETE RESTRICT,
    product_name  text    NOT NULL,   -- snapshot at time of order
    quantity      int4    NOT NULL,
    unit_price    float8  NOT NULL,   -- snapshot at time of order
    catalog_price float8  NOT NULL,   -- catalogue price at time of order
    notes         text
);

ALTER TABLE detalle_pedido ENABLE ROW LEVEL SECURITY;
CREATE INDEX idx_detalle_pedido_pedido_id ON detalle_pedido (pedido_id);
