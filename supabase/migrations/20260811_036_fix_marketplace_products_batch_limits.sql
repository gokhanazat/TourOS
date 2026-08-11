-- ============================================================
-- TourOS Migration: 20260811_036_fix_marketplace_products_batch_limits.sql
-- DESCRIPTION: marketplace_products tablosunun 1000+ toplu veri yüklemeleri
-- ve hızlı sorgulamaları için RLS ve İndeks optimizasyonları.
-- ============================================================

-- 1. Tablonun RLS politikasını 20.000+ toplu veri için aç
ALTER TABLE public.marketplace_products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "marketplace_products_select_policy" ON public.marketplace_products;
CREATE POLICY "marketplace_products_select_policy" ON public.marketplace_products
    FOR SELECT USING (TRUE);

DROP POLICY IF EXISTS "marketplace_products_all_policy" ON public.marketplace_products;
CREATE POLICY "marketplace_products_all_policy" ON public.marketplace_products
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- 2. Rol Yetkilerini Tanımla (Toplu UPSERT İzinleri)
GRANT ALL ON TABLE public.marketplace_products TO anon;
GRANT ALL ON TABLE public.marketplace_products TO authenticated;
GRANT ALL ON TABLE public.marketplace_products TO service_role;

-- 3. Hızlı Arama ve Toplu Veri Çekimi İndeksleri
CREATE INDEX IF NOT EXISTS idx_marketplace_products_product_type ON public.marketplace_products(product_type);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_operator_name ON public.marketplace_products(operator_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_hotel_name ON public.marketplace_products(hotel_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_is_published ON public.marketplace_products(is_published);
