-- ============================================================
-- TourOS Migration: 20260810_030_add_publishing_columns_to_marketplace_products.sql
-- marketplace_products tablosuna yayınlama ve özel fiyat varsayılan sütunlarının eklenmesi
-- ============================================================

ALTER TABLE public.marketplace_products 
    ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS custom_price_override NUMERIC(12,2) DEFAULT NULL;

-- İzinleri Yenile
GRANT ALL ON TABLE public.marketplace_products TO anon;
GRANT ALL ON TABLE public.marketplace_products TO authenticated;
GRANT ALL ON TABLE public.marketplace_products TO service_role;
