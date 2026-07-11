-- ============================================================
-- CarlosVCommerce — Row Level Security Policies
-- Run AFTER schema.sql. RLS is already enabled on every table
-- by schema.sql; this file only adds the access policies.
--
-- Three roles (stored in users.role):
--   SUPERUSUARIO  — full access to all tables, manages users
--   USUARIO       — full access to business tables, read/edit own profile only
--   INVITADO      — read-only access to business tables, read own profile only
--
-- NOTE: Most write operations go through the admin client (secret key)
-- which bypasses RLS. These policies are the safety net for the
-- publishable-key client and future direct DB access.
-- ============================================================


-- ─── Helper: role lookup without triggering RLS ───────────────
-- SECURITY DEFINER makes this run as its owner (postgres), bypassing
-- RLS on the users table. Without this, policies that reference
-- `users` to check the caller's role cause infinite recursion.
CREATE OR REPLACE FUNCTION get_my_role()
RETURNS TEXT
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT role FROM users WHERE id = auth.uid()
$$;


-- ─── users ───────────────────────────────────────────────────
-- SUPERUSUARIO sees and manages all users.
-- USUARIO and INVITADO can only see and edit their own profile.

CREATE POLICY "users_select"
    ON users FOR SELECT
    TO authenticated
    USING (
        get_my_role() = 'SUPERUSUARIO'
        OR id = auth.uid()
    );

CREATE POLICY "users_insert_superusuario"
    ON users FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() = 'SUPERUSUARIO');

-- SUPERUSUARIO can update any row; USUARIO can update only their own.
-- INVITADO cannot update at all (neither condition matches).
CREATE POLICY "users_update"
    ON users FOR UPDATE
    TO authenticated
    USING (
        get_my_role() = 'SUPERUSUARIO'
        OR (get_my_role() = 'USUARIO' AND id = auth.uid())
    )
    WITH CHECK (
        get_my_role() = 'SUPERUSUARIO'
        OR (get_my_role() = 'USUARIO' AND id = auth.uid())
    );

CREATE POLICY "users_delete_superusuario"
    ON users FOR DELETE
    TO authenticated
    USING (get_my_role() = 'SUPERUSUARIO');


-- ─── mercados ────────────────────────────────────────────────
-- All roles can read. Only SUPERUSUARIO and USUARIO can write.

CREATE POLICY "mercados_select_authenticated"
    ON mercados FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "mercados_insert_superusuario_usuario"
    ON mercados FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "mercados_update_superusuario_usuario"
    ON mercados FOR UPDATE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'))
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "mercados_delete_superusuario_usuario"
    ON mercados FOR DELETE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));


-- ─── clientes ────────────────────────────────────────────────

CREATE POLICY "clientes_select_authenticated"
    ON clientes FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "clientes_insert_superusuario_usuario"
    ON clientes FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "clientes_update_superusuario_usuario"
    ON clientes FOR UPDATE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'))
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "clientes_delete_superusuario_usuario"
    ON clientes FOR DELETE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));


-- ─── productos ───────────────────────────────────────────────

CREATE POLICY "productos_select_authenticated"
    ON productos FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "productos_insert_superusuario_usuario"
    ON productos FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "productos_update_superusuario_usuario"
    ON productos FOR UPDATE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'))
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "productos_delete_superusuario_usuario"
    ON productos FOR DELETE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));


-- ─── pedidos ─────────────────────────────────────────────────

CREATE POLICY "pedidos_select_authenticated"
    ON pedidos FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "pedidos_insert_superusuario_usuario"
    ON pedidos FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "pedidos_update_superusuario_usuario"
    ON pedidos FOR UPDATE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'))
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "pedidos_delete_superusuario_usuario"
    ON pedidos FOR DELETE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));


-- ─── detalle_pedido ──────────────────────────────────────────

CREATE POLICY "detalle_pedido_select_authenticated"
    ON detalle_pedido FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "detalle_pedido_insert_superusuario_usuario"
    ON detalle_pedido FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "detalle_pedido_update_superusuario_usuario"
    ON detalle_pedido FOR UPDATE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'))
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "detalle_pedido_delete_superusuario_usuario"
    ON detalle_pedido FOR DELETE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));


-- ─── pagos ───────────────────────────────────────────────────

CREATE POLICY "pagos_select_authenticated"
    ON pagos FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "pagos_insert_superusuario_usuario"
    ON pagos FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "pagos_update_superusuario_usuario"
    ON pagos FOR UPDATE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'))
    WITH CHECK (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));

CREATE POLICY "pagos_delete_superusuario_usuario"
    ON pagos FOR DELETE
    TO authenticated
    USING (get_my_role() IN ('SUPERUSUARIO', 'USUARIO'));


-- ─── umbrales ────────────────────────────────────────────────
-- Everyone reads (client status must compute identically for all
-- roles). Only SUPERUSUARIO can change the thresholds — matches
-- the in-app screen, which is hidden from USUARIO/INVITADO.

CREATE POLICY "umbrales_select_authenticated"
    ON umbrales FOR SELECT
    TO authenticated
    USING (true);

CREATE POLICY "umbrales_insert_superusuario"
    ON umbrales FOR INSERT
    TO authenticated
    WITH CHECK (get_my_role() = 'SUPERUSUARIO');

CREATE POLICY "umbrales_update_superusuario"
    ON umbrales FOR UPDATE
    TO authenticated
    USING (get_my_role() = 'SUPERUSUARIO')
    WITH CHECK (get_my_role() = 'SUPERUSUARIO');
