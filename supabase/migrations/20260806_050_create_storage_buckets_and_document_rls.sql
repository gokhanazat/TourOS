-- ============================================================
-- TourOS 3.4.1 Storage Buckets & Document Management RLS SQL
-- ============================================================

-- Storage Bucket Oluşturma (Supabase Storage)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'documents', 
    'documents', 
    true, 
    52428800, -- 50 MB Limit
    ARRAY['application/pdf', 'image/jpeg', 'image/jpg', 'image/png', 'image/webp']
)
ON CONFLICT (id) DO UPDATE SET 
    public = true,
    file_size_limit = 52428800,
    allowed_mime_types = ARRAY['application/pdf', 'image/jpeg', 'image/jpg', 'image/png', 'image/webp'];

-- Storage RLS Güvenlik Politikaları
DROP POLICY IF EXISTS "Tenant Isolated Storage Access" ON storage.objects;
CREATE POLICY "Tenant Isolated Storage Access" ON storage.objects
    FOR ALL
    USING (bucket_id = 'documents' AND (storage.foldername(name))[1] = public.current_tenant_id()::text)
    WITH CHECK (bucket_id = 'documents' AND (storage.foldername(name))[1] = public.current_tenant_id()::text);
