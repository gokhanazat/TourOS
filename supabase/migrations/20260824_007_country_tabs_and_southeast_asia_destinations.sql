-- ============================================================
-- TourOS Migration: 20260824_007_country_tabs_and_southeast_asia_destinations.sql
-- Ülke Sekmeleri & Güneydoğu Asya (Tayland, Vietnam), Mısır, BAE Destinasyonları
-- ============================================================

-- 1. TAYLAND VE VİETNAM HAVALİMANLARI
INSERT INTO public.airports (iata_code, airport_name, city_name, country_code, country_name, is_departure_hub, is_arrival_hub, display_order)
VALUES 
    ('HKT', 'Phuket Uluslararası Havalimanı', 'Phuket', 'TH', 'Tayland', false, true, 20),
    ('BKK', 'Suvarnabhumi Uluslararası Havalimanı', 'Bangkok', 'TH', 'Tayland', false, true, 21),
    ('UTP', 'U-Tapao Uluslararası Havalimanı', 'Pattaya', 'TH', 'Tayland', false, true, 22),
    ('USM', 'Samui Uluslararası Havalimanı', 'Koh Samui', 'TH', 'Tayland', false, true, 23),
    ('DAD', 'Da Nang Uluslararası Havalimanı', 'Da Nang', 'VN', 'Vietnam', false, true, 25),
    ('PQC', 'Phu Quoc Uluslararası Havalimanı', 'Phu Quoc', 'VN', 'Vietnam', false, true, 26),
    ('CXR', 'Cam Ranh Uluslararası Havalimanı', 'Nha Trang', 'VN', 'Vietnam', false, true, 27)
ON CONFLICT (iata_code) DO NOTHING;

-- 2. TAYLAND, VİETNAM, MISIR VE TÜRKİYE HİYERARŞİK DESTİNASYONLARI
INSERT INTO public.destinations (country_code, country_name, city_name, region_name, sub_region_name, primary_airport, display_order)
VALUES 
    -- Tayland
    ('TH', 'Tayland', 'Phuket', 'Güney Tayland', 'Patong & Karon Beach', 'HKT', 10),
    ('TH', 'Tayland', 'Phuket', 'Güney Tayland', 'Kata & Bang Tao', 'HKT', 11),
    ('TH', 'Tayland', 'Pattaya', 'Chonburi', 'Jomtien & Naklua', 'UTP', 12),
    ('TH', 'Tayland', 'Bangkok', 'Merkez', 'Sukhumvit & Silom', 'BKK', 13),
    ('TH', 'Tayland', 'Koh Samui', 'Surat Thani', 'Chaweng Beach', 'USM', 14),
    
    -- Vietnam
    ('VN', 'Vietnam', 'Da Nang', 'Merkez Sahil', 'My Khe Beach & Hoi An', 'DAD', 20),
    ('VN', 'Vietnam', 'Phu Quoc', 'Kien Giang', 'Long Beach & Sunset Town', 'PQC', 21),
    ('VN', 'Vietnam', 'Nha Trang', 'Khanh Hoa', 'Tran Phu Beach', 'CXR', 22),

    -- Mısır
    ('EG', 'Mısır', 'Şarm El-Şeyh', 'Güney Sina', 'Naama Bay & Nabq Bay', 'SSH', 30),
    ('EG', 'Mısır', 'Hurgada', 'Kızıldeniz', 'El Gouna & Makadi Bay', 'HRG', 31)
ON CONFLICT DO NOTHING;

-- 3. ÜLKE BAZLI SEKME VE OTEL SAYILARI FONKSİYONU
CREATE OR REPLACE FUNCTION public.get_country_tabs_with_counts()
RETURNS TABLE (
    country_code VARCHAR(2),
    country_name TEXT,
    country_flag TEXT,
    hotel_count  BIGINT,
    display_order INT
)
LANGUAGE plpgsql
STABLE
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(p.country_code, 'TR')::VARCHAR(2) AS country_code,
        COALESCE(p.country_name, 'Türkiye') AS country_name,
        CASE 
            WHEN p.country_name ILIKE '%Mısır%' THEN '🇪🇬'
            WHEN p.country_name ILIKE '%Tayland%' OR p.country_name ILIKE '%Thailand%' THEN '🇹🇭'
            WHEN p.country_name ILIKE '%Vietnam%' THEN '🇻🇳'
            WHEN p.country_name ILIKE '%BAE%' OR p.country_name ILIKE '%Dubai%' THEN '🇦🇪'
            WHEN p.country_name ILIKE '%Rusya%' THEN '🇷🇺'
            ELSE '🇹🇷'
        END AS country_flag,
        COUNT(p.id) AS hotel_count,
        CASE 
            WHEN p.country_name ILIKE '%Türkiye%' THEN 1
            WHEN p.country_name ILIKE '%Mısır%' THEN 2
            WHEN p.country_name ILIKE '%Tayland%' OR p.country_name ILIKE '%Thailand%' THEN 3
            WHEN p.country_name ILIKE '%Vietnam%' THEN 4
            WHEN p.country_name ILIKE '%BAE%' OR p.country_name ILIKE '%Dubai%' THEN 5
            ELSE 6
        END AS display_order
    FROM public.marketplace_products p
    WHERE p.is_active = TRUE
    GROUP BY p.country_code, p.country_name
    ORDER BY display_order ASC;
END;
$$;

GRANT EXECUTE ON FUNCTION public.get_country_tabs_with_counts TO authenticated, anon, service_role;
