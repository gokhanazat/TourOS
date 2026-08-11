-- ============================================================
-- 📜 TourOS 5 Sekmeli B2B Arama Motoru Birleşik Görünümü (View)
-- Düzeltildi: tenant_id::text ve id::text dönüşümleri UNION veri tipi uyumu için yapıldı
-- ============================================================

DROP VIEW IF EXISTS public.v_b2b_search_products;

CREATE OR REPLACE VIEW public.v_b2b_search_products AS

-- Sekme 1: Operatör Paket Turları
SELECT 
    id::text AS id,
    'TOURS' AS search_tab,
    tour_name AS title,
    hotel_name,
    region,
    departure_city,
    operator_name,
    hotel_category AS stars,
    meal_type,
    nights,
    price,
    currency,
    picture_url AS image_url,
    tenant_id::text AS tenant_id
FROM public.marketplace_products
WHERE product_type IN ('TOUR', 'PACKAGE') OR product_type IS NULL

UNION ALL

-- Sekme 2: Operatör Otelleri
SELECT 
    id::text AS id,
    'HOTELS' AS search_tab,
    hotel_name AS title,
    hotel_name,
    region,
    departure_city,
    operator_name,
    hotel_category AS stars,
    meal_type,
    nights,
    price,
    currency,
    picture_url AS image_url,
    tenant_id::text AS tenant_id
FROM public.marketplace_products
WHERE product_type = 'HOTEL'

UNION ALL

-- Sekme 3: Hava Yolculuğu / Charter Uçuşlar
SELECT 
    id::text AS id,
    'FLIGHTS' AS search_tab,
    airline_name || ' (' || flight_number || ')' AS title,
    hotel_name,
    region,
    departure_city,
    operator_name,
    0 AS stars,
    meal_type,
    nights,
    price,
    currency,
    picture_url AS image_url,
    tenant_id::text AS tenant_id
FROM public.marketplace_products
WHERE product_type IN ('FLIGHT', 'CHARTER')

UNION ALL

-- Sekme 4: Yerel Turlar (Acentanın Özel Turları)
SELECT 
    id::text AS id,
    'LOCAL_TOURS' AS search_tab,
    title,
    '' AS hotel_name,
    city AS region,
    'İstanbul' AS departure_city,
    'Yerel Acente' AS operator_name,
    5 AS stars,
    'Dahil' AS meal_type,
    duration_days AS nights,
    base_price AS price,
    'TRY' AS currency,
    cover_image_url AS image_url,
    tenant_id::text AS tenant_id
FROM public.tours

UNION ALL

-- Sekme 5: Yerel Oteller (Acentanın Özel Otelleri)
SELECT 
    id::text AS id,
    'LOCAL_HOTELS' AS search_tab,
    name AS title,
    name AS hotel_name,
    city AS region,
    'İstanbul' AS departure_city,
    'Yerel Otel' AS operator_name,
    COALESCE(star_rating, 5) AS stars,
    'BB' AS meal_type,
    1 AS nights,
    0.00 AS price,
    'TRY' AS currency,
    cover_image_url AS image_url,
    tenant_id::text AS tenant_id
FROM public.hotels;
