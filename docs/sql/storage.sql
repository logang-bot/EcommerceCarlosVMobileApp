-- ============================================================
-- CarlosVCommerce — Supabase Storage Setup
-- Run in the Supabase SQL editor OR configure via the dashboard.
-- ============================================================

-- Create storage buckets (run via Supabase dashboard or SQL below).
-- Recommended: create via dashboard (Storage → New bucket) so you
-- can set public/private access in the UI.

-- Bucket: mercado-photos (public — photo URLs embedded in the app)
INSERT INTO storage.buckets (id, name, public)
VALUES ('mercado-photos', 'mercado-photos', true)
ON CONFLICT (id) DO NOTHING;

-- Bucket: cliente-photos (public)
INSERT INTO storage.buckets (id, name, public)
VALUES ('cliente-photos', 'cliente-photos', true)
ON CONFLICT (id) DO NOTHING;

-- Bucket: producto-photos (public)
INSERT INTO storage.buckets (id, name, public)
VALUES ('producto-photos', 'producto-photos', true)
ON CONFLICT (id) DO NOTHING;

-- Bucket: user-photos (public)
INSERT INTO storage.buckets (id, name, public)
VALUES ('user-photos', 'user-photos', true)
ON CONFLICT (id) DO NOTHING;


-- ─── Storage RLS Policies ────────────────────────────────────
-- Allow authenticated users to upload and read.

CREATE POLICY "mercado_photos_select" ON storage.objects
    FOR SELECT TO authenticated USING (bucket_id = 'mercado-photos');

CREATE POLICY "mercado_photos_insert" ON storage.objects
    FOR INSERT TO authenticated WITH CHECK (bucket_id = 'mercado-photos');

CREATE POLICY "mercado_photos_update" ON storage.objects
    FOR UPDATE TO authenticated USING (bucket_id = 'mercado-photos');

CREATE POLICY "mercado_photos_delete" ON storage.objects
    FOR DELETE TO authenticated USING (bucket_id = 'mercado-photos');


CREATE POLICY "cliente_photos_select" ON storage.objects
    FOR SELECT TO authenticated USING (bucket_id = 'cliente-photos');

CREATE POLICY "cliente_photos_insert" ON storage.objects
    FOR INSERT TO authenticated WITH CHECK (bucket_id = 'cliente-photos');

CREATE POLICY "cliente_photos_update" ON storage.objects
    FOR UPDATE TO authenticated USING (bucket_id = 'cliente-photos');

CREATE POLICY "cliente_photos_delete" ON storage.objects
    FOR DELETE TO authenticated USING (bucket_id = 'cliente-photos');


CREATE POLICY "producto_photos_select" ON storage.objects
    FOR SELECT TO authenticated USING (bucket_id = 'producto-photos');

CREATE POLICY "producto_photos_insert" ON storage.objects
    FOR INSERT TO authenticated WITH CHECK (bucket_id = 'producto-photos');

CREATE POLICY "producto_photos_update" ON storage.objects
    FOR UPDATE TO authenticated USING (bucket_id = 'producto-photos');

CREATE POLICY "producto_photos_delete" ON storage.objects
    FOR DELETE TO authenticated USING (bucket_id = 'producto-photos');


CREATE POLICY "user_photos_select" ON storage.objects
    FOR SELECT TO authenticated USING (bucket_id = 'user-photos');

CREATE POLICY "user_photos_insert" ON storage.objects
    FOR INSERT TO authenticated WITH CHECK (bucket_id = 'user-photos');

CREATE POLICY "user_photos_update" ON storage.objects
    FOR UPDATE TO authenticated USING (bucket_id = 'user-photos');

CREATE POLICY "user_photos_delete" ON storage.objects
    FOR DELETE TO authenticated USING (bucket_id = 'user-photos');
