-- ============================================================
-- TourOS Migration: 20260810_026_fix_marketplace_products_permissions.sql
-- marketplace_products tablosuna erişim ve RLS izinlerinin verilmesi,
-- yayınlanma ve fiyat override alanlarının eklenmesi.
-- ============================================================

-- 1. Yayınlanma durumu ve fiyat override sütunlarını ekle
ALTER TABLE public.marketplace_products 
ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS custom_price_override NUMERIC(12,2);

-- 2. RLS Politikalarını Aç ve Yetkilendir
ALTER TABLE public.marketplace_products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "marketplace_products_select_policy" ON public.marketplace_products;
CREATE POLICY "marketplace_products_select_policy" ON public.marketplace_products
    FOR SELECT USING (true);

DROP POLICY IF EXISTS "marketplace_products_all_policy" ON public.marketplace_products;
CREATE POLICY "marketplace_products_all_policy" ON public.marketplace_products
    FOR ALL USING (true) WITH CHECK (true);

-- 3. Rol Yetkilerini Tanımla
GRANT ALL ON TABLE public.marketplace_products TO anon;
GRANT ALL ON TABLE public.marketplace_products TO authenticated;
GRANT ALL ON TABLE public.marketplace_products TO service_role;
