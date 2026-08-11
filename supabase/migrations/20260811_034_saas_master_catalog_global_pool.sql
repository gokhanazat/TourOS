-- ============================================================
-- TourOS: SaaS Master Catalog & Global Pool Migration (FIXED)
-- Safe execution for marketplace_products
-- ============================================================

-- 1. marketplace_products tablosuna tenant_id ve is_global sütunlarını güvenle ekle
ALTER TABLE public.marketplace_products 
    ADD COLUMN IF NOT EXISTS tenant_id TEXT DEFAULT '00000000-0000-0000-0000-000000000001',
    ADD COLUMN IF NOT EXISTS is_global BOOLEAN DEFAULT TRUE;

-- 2. Kökten yüklenen tüm ürünleri varsayılan olarak Global yap
UPDATE public.marketplace_products 
SET is_global = TRUE, 
    tenant_id = '00000000-0000-0000-0000-000000000001'
WHERE tenant_id IS NULL OR tenant_id = '' OR tenant_id = '00000000-0000-0000-0000-000000000001';

-- 3. Supabase RLS Okuma Politikasını Güncelle (Tüm Acenteler Kök Veriyi Okur)
ALTER TABLE public.marketplace_products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "marketplace_products_select_policy" ON public.marketplace_products;

CREATE POLICY "marketplace_products_select_policy" 
ON public.marketplace_products
FOR SELECT 
USING (TRUE);
