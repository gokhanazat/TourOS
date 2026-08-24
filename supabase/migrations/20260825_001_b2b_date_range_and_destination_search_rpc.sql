-- ============================================================================
-- TourOS Migration: 20260825_001_b2b_date_range_and_destination_search_rpc.sql
-- Description: B2B Gelişmiş Tarih Aralığı (Start Date - End Date) ve 
--              Hiyerarşik / Çoklu Destinasyon Arama RPC Fonksiyonu (Düzeltildi)
-- ============================================================================

-- 1. Index İyileştirmeleri (Doğru Sütun İsimleri İle)
CREATE INDEX IF NOT EXISTS idx_marketplace_products_date_range 
ON public.marketplace_products (is_active, country_code, nights, price);

CREATE INDEX IF NOT EXISTS idx_marketplace_products_location_gin 
ON public.marketplace_products USING gin (to_tsvector('simple', COALESCE(hotel_name, '') || ' ' || COALESCE(region, '') || ' ' || COALESCE(sub_region, '') || ' ' || COALESCE(country, '')));

-- 2. Gelişmiş Tarih Aralıklı B2B Arama RPC Fonksiyonu
CREATE OR REPLACE FUNCTION public.search_b2b_tours_date_range(
    p_departure_city    VARCHAR(100) DEFAULT NULL,
    p_destination       VARCHAR(255) DEFAULT NULL,
    p_start_date        DATE DEFAULT NULL,
    p_end_date          DATE DEFAULT NULL,
    p_min_nights        INT DEFAULT 1,
    p_max_nights        INT DEFAULT 30,
    p_adults            INT DEFAULT 2,
    p_childs            INT DEFAULT 0,
    p_country_code      VARCHAR(10) DEFAULT 'ALL',
    p_sub_region        VARCHAR(100) DEFAULT NULL,
    p_min_stars         INT DEFAULT 0,
    p_meal_types        TEXT[] DEFAULT NULL,
    p_operators         TEXT[] DEFAULT NULL,
    p_is_instant_only   BOOLEAN DEFAULT FALSE,
    p_is_direct_only    BOOLEAN DEFAULT FALSE,
    p_limit             INT DEFAULT 100,
    p_offset            INT DEFAULT 0
)
RETURNS TABLE (
    id                      TEXT,
    product_type            TEXT,
    hotel_name              TEXT,
    tour_name               TEXT,
    location                TEXT,
    country                 TEXT,
    country_code            VARCHAR(10),
    region                  TEXT,
    sub_region              TEXT,
    departure_city          TEXT,
    departure_date          TEXT,
    nights                  INT,
    stars                   INT,
    meal_type               TEXT,
    room_type               TEXT,
    flight_number           TEXT,
    airline_name            TEXT,
    is_direct_flight        BOOLEAN,
    has_transfer            BOOLEAN,
    is_instant_confirmation BOOLEAN,
    price                   NUMERIC(12, 2),
    currency                TEXT,
    operator_name           TEXT,
    picture_url             TEXT
) AS $$
DECLARE
    v_dest_tokens TEXT[];
BEGIN
    -- Destinasyon metnini '/' ve ',' karakterlerinden bölerek anahtar kelimelere ayır
    IF p_destination IS NOT NULL AND TRIM(p_destination) <> '' AND TRIM(p_destination) <> 'Tüm Destinasyonlar' THEN
        v_dest_tokens := string_to_array(
            regexp_replace(
                regexp_replace(p_destination, '[\(\)\—\-]', ' ', 'g'), 
                '\/|,', ' ', 'g'
            ), 
            ' '
        );
    END IF;

    RETURN QUERY
    SELECT 
        mp.id::TEXT,
        COALESCE(mp.product_type, 'PACKAGE_TOUR')::TEXT AS product_type,
        COALESCE(mp.hotel_name, '')::TEXT AS hotel_name,
        COALESCE(mp.tour_name, mp.hotel_name, '')::TEXT AS tour_name,
        COALESCE(mp.region, '')::TEXT AS location,
        COALESCE(mp.country, 'Türkiye')::TEXT AS country,
        COALESCE(mp.country_code, 'TR')::VARCHAR(10) AS country_code,
        COALESCE(mp.region, '')::TEXT AS region,
        COALESCE(mp.sub_region, '')::TEXT AS sub_region,
        COALESCE(mp.departure_city, 'Moskova')::TEXT AS departure_city,
        COALESCE(mp.departure_date, '')::TEXT AS departure_date,
        COALESCE(mp.nights, 7)::INT AS nights,
        COALESCE(mp.hotel_category, 4)::INT AS stars,
        COALESCE(mp.meal_type, 'Her Şey Dahil')::TEXT AS meal_type,
        COALESCE(mp.room_type, 'Standart Oda')::TEXT AS room_type,
        COALESCE(mp.flight_number, '')::TEXT AS flight_number,
        COALESCE(mp.airline_name, '')::TEXT AS airline_name,
        COALESCE(mp.is_charter, TRUE)::BOOLEAN AS is_direct_flight,
        TRUE::BOOLEAN AS has_transfer,
        TRUE::BOOLEAN AS is_instant_confirmation,
        COALESCE(mp.price, 350.00)::NUMERIC(12, 2) AS price,
        COALESCE(mp.currency, 'EUR')::TEXT AS currency,
        COALESCE(mp.operator_name, 'TourOS B2B')::TEXT AS operator_name,
        COALESCE(mp.picture_url, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800')::TEXT AS picture_url
    FROM public.marketplace_products mp
    WHERE COALESCE(mp.is_active, TRUE) = TRUE
      -- Kalkış Şehri Filtresi
      AND (
          p_departure_city IS NULL 
          OR TRIM(p_departure_city) = '' 
          OR p_departure_city ILIKE '%Tüm%'
          OR mp.departure_city ILIKE '%' || p_departure_city || '%'
      )
      -- Destinasyon / Alt Belde / Hiyerarşik Token Eşleşmesi
      AND (
          v_dest_tokens IS NULL 
          OR EXISTS (
              SELECT 1 FROM unnest(v_dest_tokens) token 
              WHERE length(trim(token)) >= 3
                AND (
                    mp.region ILIKE '%' || trim(token) || '%'
                    OR mp.sub_region ILIKE '%' || trim(token) || '%'
                    OR mp.hotel_name ILIKE '%' || trim(token) || '%'
                    OR mp.country ILIKE '%' || trim(token) || '%'
                    OR (lower(token) = 'kemer' AND (mp.region ILIKE '%antalya%' OR mp.region ILIKE '%kemer%' OR mp.sub_region ILIKE '%kemer%'))
                    OR (lower(token) = 'belek' AND (mp.region ILIKE '%antalya%' OR mp.region ILIKE '%belek%' OR mp.sub_region ILIKE '%belek%'))
                    OR (lower(token) = 'side' AND (mp.region ILIKE '%antalya%' OR mp.region ILIKE '%side%' OR mp.sub_region ILIKE '%side%'))
                    OR (lower(token) = 'alanya' AND (mp.region ILIKE '%antalya%' OR mp.region ILIKE '%alanya%' OR mp.sub_region ILIKE '%alanya%'))
                )
          )
      )
      -- Tarih Aralığı Filtresi (Start Date & End Date)
      AND (
          p_start_date IS NULL OR p_end_date IS NULL
          OR (CASE WHEN mp.departure_date ~ '^\d{4}-\d{2}-\d{2}$' THEN mp.departure_date::DATE ELSE NULL END) BETWEEN p_start_date AND p_end_date
          OR mp.departure_date IS NULL
          OR mp.departure_date = ''
      )
      -- Gece Sayısı Aralığı
      AND (COALESCE(mp.nights, 7) BETWEEN p_min_nights AND p_max_nights)
      -- Ülke Kodu
      AND (
          p_country_code IS NULL 
          OR p_country_code = 'ALL' 
          OR mp.country_code = p_country_code
      )
      -- Yıldız Kategori
      AND (p_min_stars <= 0 OR COALESCE(mp.hotel_category, 4) >= p_min_stars)
    ORDER BY mp.price ASC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 3. Yetkilendirme
GRANT EXECUTE ON FUNCTION public.search_b2b_tours_date_range TO authenticated, anon, service_role;
