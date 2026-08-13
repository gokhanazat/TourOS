-- ==============================================================================
-- Migration: 20260813_004_ensure_public_storage_policies.sql
-- Description: company-logos Storage Bucket İzinlerini ve Erişim Politikalarını Güncelleme
-- ==============================================================================

-- 1. company-logos bucket'ının public olduğunu garanti et
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'company-logos', 
    'company-logos', 
    true, 
    52428800, -- 50 MB
    ARRAY['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif', 'image/svg+xml']
)
ON CONFLICT (id) DO UPDATE SET 
    public = true,
    file_size_limit = 52428800;

-- 2. company-logos bucket'ı için Herkese Açık Okuma (Public Select) İzni
DROP POLICY IF EXISTS "Public Read Access for company-logos" ON storage.objects;
CREATE POLICY "Public Read Access for company-logos" ON storage.objects
    FOR SELECT
    USING (bucket_id = 'company-logos');

-- 3. company-logos bucket'ı için Yükleme (Insert/Upsert) İzni
DROP POLICY IF EXISTS "Public Insert Access for company-logos" ON storage.objects;
CREATE POLICY "Public Insert Access for company-logos" ON storage.objects
    FOR INSERT
    WITH CHECK (bucket_id = 'company-logos');

DROP POLICY IF EXISTS "Public Update Access for company-logos" ON storage.objects;
CREATE POLICY "Public Update Access for company-logos" ON storage.objects
    FOR UPDATE
    USING (bucket_id = 'company-logos');
