-- ============================================================
-- TourOS Migration: 20260810_032_fix_marketplace_products_empty_id.sql
-- marketplace_products tablosundaki boş ID kayıtlarının temizlenmesi, sütun kısıtları ve RLS politikaları
-- ============================================================

-- 1. Varsa hatalı boş id kayıtlarını sil
DELETE FROM public.marketplace_products WHERE id IS NULL OR trim(id) = '';

-- 2. Yayınlama ve özel fiyat varsayılan sütunlarının varlığını garanti et
ALTER TABLE public.marketplace_products 
    ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS custom_price_override NUMERIC(12,2) DEFAULT NULL,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ DEFAULT now();

-- 3. id sütununun NOT NULL kalmasını sağla
ALTER TABLE public.marketplace_products 
    ALTER COLUMN id SET NOT NULL;

-- 4. RLS politikalarını yenile (UPSERT/SELECT/UPDATE/INSERT tam erişim)
ALTER TABLE public.marketplace_products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "marketplace_products_select_policy" ON public.marketplace_products;
DROP POLICY IF EXISTS "marketplace_products_all_policy" ON public.marketplace_products;

CREATE POLICY "marketplace_products_all_policy" ON public.marketplace_products
    FOR ALL 
    USING (true) 
    WITH CHECK (true);

GRANT ALL ON TABLE public.marketplace_products TO anon, authenticated, service_role;
