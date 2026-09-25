-- ==============================================================================
-- TourOS - Yandex Cloud PostgreSQL: Hızlı Fırsatlar
-- Kural: Bugünden 1 hafta sonrasından başlayan, Rusya hariç, her ülkeden en ucuz 1 teklif (Toplam 4)
-- Migration: 20260925_002_fast_deals_by_country.sql
-- ==============================================================================

-- 1. Performans İndeksi (Yandex Cloud PostgreSQL için milisaniyelik yanıt)
CREATE INDEX IF NOT EXISTS idx_marketplace_fast_deals_country_price
ON public.marketplace_products (country, price ASC)
WHERE is_published = true 
  AND country NOT IN ('Rusya', 'Россия', 'Russia')
  AND country_code NOT IN ('RU', 'RUS');

-- 2. Her ülkeden en ucuz 1 teklifi seçip (bugünden 1 hafta sonra başlayan), en ucuz ilk 4 ülkeyi getiren View
CREATE OR REPLACE VIEW public.view_fast_deals_distinct_countries AS
WITH ranked_deals AS (
    SELECT 
        id, product_type, tour_name, operator_name, price, currency,
        hotel_name, hotel_category, hotel_rating, country, country_code, country_name,
        region, departure_city, departure_date, nights, adults, picture_url,
        is_direct_flight, has_transfer,
        ROW_NUMBER() OVER (
            PARTITION BY country 
            ORDER BY price ASC, hotel_rating DESC
        ) AS rn
    FROM public.marketplace_products
    WHERE is_published = true
      AND product_type = 'PACKAGE_TOUR'
      AND NOT (tour_name ILIKE 'Uçuş:%' OR hotel_name ILIKE 'Uçuş:%' OR hotel_name ILIKE '✈️%')
      AND country NOT IN ('Rusya', 'Россия', 'Russia')
      AND COALESCE(country_code, '') NOT IN ('RU', 'RUS')
      AND operator_name IN (
          'Bibloglobus', 'Anex', 'Coral Travel', 'Sunmar', 
          'Fun&Sun (Ru)', 'Kazunion', 'Loti', 'Pegas Touristik', 'Интурист'
      )
      AND nights = 7
      AND departure_date >= (CURRENT_DATE + INTERVAL '7 days')::date
)
SELECT 
    id, product_type, tour_name, operator_name, price, currency,
    hotel_name, hotel_category, hotel_rating, country, country_code, country_name,
    region, departure_city, departure_date, nights, adults, picture_url,
    is_direct_flight, has_transfer
FROM ranked_deals
WHERE rn = 1
ORDER BY price ASC
LIMIT 4;

-- 3. PostgREST ve Kullanıcı Yetkileri
GRANT ALL ON public.view_fast_deals_distinct_countries TO postgres, service_role;
GRANT SELECT ON public.view_fast_deals_distinct_countries TO anon, authenticated;
NOTIFY pgrst, 'reload schema';
