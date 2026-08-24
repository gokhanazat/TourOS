-- Migration: 20260824_009_country_dedicated_search_rpc.sql
-- Description: Ülke Detay Sayfası İçin Bağımsız Nokta Atışı Arama RPC Fonksiyonu

-- 1. Ülke Bazlı İzole Tur & Otel Arama Fonksiyonu
CREATE OR REPLACE FUNCTION public.search_country_dedicated_packages(
    p_country_code VARCHAR(10),
    p_sub_region VARCHAR(100) DEFAULT NULL,
    p_hotel_query VARCHAR(150) DEFAULT NULL,
    p_min_stars INT DEFAULT 3,
    p_meal_type VARCHAR(100) DEFAULT NULL,
    p_limit INT DEFAULT 50,
    p_offset INT DEFAULT 0
)
RETURNS TABLE (
    id UUID,
    hotel_name VARCHAR(255),
    location VARCHAR(255),
    country_code VARCHAR(10),
    stars INT,
    description TEXT,
    min_price NUMERIC(12, 2),
    max_price NUMERIC(12, 2),
    image_url TEXT,
    operator_name VARCHAR(100),
    meal_type VARCHAR(100),
    room_type VARCHAR(100),
    nights INT,
    flight_code VARCHAR(100),
    is_instant_confirmation BOOLEAN,
    is_last_minute BOOLEAN,
    discount_percent INT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        mp.id,
        mp.hotel_name,
        mp.location,
        mp.country_code,
        COALESCE(mp.stars, 4) as stars,
        mp.description,
        COALESCE(mp.min_price, 500.00) as min_price,
        COALESCE(mp.max_price, 1200.00) as max_price,
        COALESCE(mp.image_url, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800') as image_url,
        COALESCE(mp.operator_name, 'TourOS Direkt') as operator_name,
        COALESCE(mp.meal_type, 'Her Şey Dahil') as meal_type,
        COALESCE(mp.room_type, 'Standart Oda') as room_type,
        COALESCE(mp.nights, 7) as nights,
        COALESCE(mp.flight_code, 'Direkt Uçuş') as flight_code,
        COALESCE(mp.is_instant_confirmation, TRUE) as is_instant_confirmation,
        COALESCE(mp.is_last_minute, FALSE) as is_last_minute,
        COALESCE(mp.discount_percent, 0) as discount_percent
    FROM public.marketplace_products mp
    WHERE mp.is_active = TRUE
      AND (p_country_code IS NULL OR p_country_code = 'ALL' OR mp.country_code = p_country_code)
      AND (
          p_sub_region IS NULL 
          OR p_sub_region = 'Tümü' 
          OR mp.location ILIKE '%' || p_sub_region || '%' 
          OR mp.hotel_name ILIKE '%' || p_sub_region || '%'
      )
      AND (
          p_hotel_query IS NULL 
          OR p_hotel_query = '' 
          OR mp.hotel_name ILIKE '%' || p_hotel_query || '%'
      )
      AND (COALESCE(mp.stars, 4) >= p_min_stars)
      AND (
          p_meal_type IS NULL 
          OR p_meal_type = 'Tümü' 
          OR mp.meal_type ILIKE '%' || p_meal_type || '%'
      )
    ORDER BY mp.is_last_minute DESC, mp.min_price ASC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
