-- ============================================================
-- TourOS Migration: 20260824_003_flexible_nights_search_rpc.sql
-- Esnek Geceleme Aralığı (Min - Max Nights) Arama Fonksiyonu & İndeksi
-- ============================================================

-- 1. GECELEME İNDEKSİ (Hızlı Aralık Sorguları İçin)
CREATE INDEX IF NOT EXISTS idx_marketplace_products_nights ON public.marketplace_products(nights);

-- 2. ESNEK GECELEME VE HİYERARŞİK FİLTRELEME RPC FONKSİYONU
CREATE OR REPLACE FUNCTION public.search_marketplace_products_flexible(
    p_departure_city  TEXT DEFAULT NULL,
    p_country         TEXT DEFAULT NULL,
    p_region          TEXT DEFAULT NULL,
    p_sub_region      TEXT DEFAULT NULL,
    p_min_nights      INT DEFAULT 1,
    p_max_nights      INT DEFAULT 30,
    p_min_price       NUMERIC DEFAULT 0,
    p_max_price       NUMERIC DEFAULT 500000,
    p_category_tab    TEXT DEFAULT 'PACKAGE_TOUR'
)
RETURNS SETOF public.marketplace_products
LANGUAGE sql
STABLE
SECURITY DEFINER
AS $$
    SELECT *
    FROM public.marketplace_products
    WHERE
        (p_departure_city IS NULL OR p_departure_city = '' OR departure_city ILIKE '%' || p_departure_city || '%')
        AND (p_country IS NULL OR p_country = '' OR country ILIKE '%' || p_country || '%')
        AND (p_region IS NULL OR p_region = '' OR region ILIKE '%' || p_region || '%')
        AND (p_sub_region IS NULL OR p_sub_region = '' OR sub_region ILIKE '%' || p_sub_region || '%')
        AND (p_category_tab IS NULL OR p_category_tab = '' OR product_type ILIKE '%' || p_category_tab || '%')
        -- ESNEK GECELEME ARALIĞI (BETWEEN MIN AND MAX)
        AND (nights >= p_min_nights AND nights <= p_max_nights)
        AND (price >= p_min_price AND price <= p_max_price)
    ORDER BY price ASC;
$$;

GRANT EXECUTE ON FUNCTION public.search_marketplace_products_flexible TO authenticated, anon, service_role;
