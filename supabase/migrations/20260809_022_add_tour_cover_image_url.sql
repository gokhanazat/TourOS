-- 1. tours tablosuna kapak görseli sütunu ekleme
ALTER TABLE public.tours ADD COLUMN IF NOT EXISTS cover_image_url TEXT;

-- 2. Storage Bucket 'tour-covers' tanımlama
INSERT INTO storage.buckets (id, name, public)
VALUES ('tour-covers', 'tour-covers', true)
ON CONFLICT (id) DO NOTHING;

-- 3. Storage RLS Politikaları
DROP POLICY IF EXISTS "tour_covers_public_read" ON storage.objects;
CREATE POLICY "tour_covers_public_read" ON storage.objects 
    FOR SELECT USING (bucket_id = 'tour-covers');

DROP POLICY IF EXISTS "tour_covers_auth_insert" ON storage.objects;
CREATE POLICY "tour_covers_auth_insert" ON storage.objects 
    FOR INSERT WITH CHECK (bucket_id = 'tour-covers');

DROP POLICY IF EXISTS "tour_covers_auth_update" ON storage.objects;
CREATE POLICY "tour_covers_auth_update" ON storage.objects 
    FOR UPDATE USING (bucket_id = 'tour-covers');

-- 4. Storefront Arama ve Liste RPC Güncellemesi
CREATE OR REPLACE FUNCTION public.search_agency_storefront_tours(
    p_agency_id UUID,
    p_country TEXT DEFAULT '',
    p_min_nights INT DEFAULT 0,
    p_max_nights INT DEFAULT 30,
    p_max_budget NUMERIC DEFAULT 100000.0
) RETURNS TABLE (
    tour_id TEXT,
    title TEXT,
    code TEXT,
    country TEXT,
    city TEXT,
    nights INT,
    base_price NUMERIC,
    final_price NUMERIC,
    operator_name TEXT,
    compared_operator_count INT,
    cover_image_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id::text AS tour_id,
        t.title,
        t.code,
        t.country,
        t.city,
        t.duration_days AS nights,
        COALESCE(t.base_price, 0.0) AS base_price,
        COALESCE(apt.custom_price_override, t.base_price) AS final_price,
        comp.name AS operator_name,
        1 AS compared_operator_count,
        t.cover_image_url
    FROM public.tours t
    JOIN public.companies comp ON comp.id = t.tenant_id
    LEFT JOIN public.agency_published_tours apt ON apt.agency_id = p_agency_id AND apt.tour_id = t.id
    WHERE (t.tenant_id = p_agency_id OR EXISTS (
        SELECT 1 FROM public.agency_operator_connections conn 
        WHERE conn.agency_id = p_agency_id AND conn.operator_company_id = t.tenant_id AND conn.status = 'ACTIVE'
    ))
      AND t.is_active = true
      AND COALESCE(apt.is_published, true) = true
      AND (p_country IS NULL OR p_country = '' OR LOWER(t.country) LIKE '%' || LOWER(p_country) || '%' OR LOWER(t.city) LIKE '%' || LOWER(p_country) || '%')
      AND (t.duration_days >= p_min_nights AND t.duration_days <= p_max_nights)
      AND COALESCE(apt.custom_price_override, t.base_price) <= p_max_budget;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
