-- ========================================================================================
-- TourOS - Uçuş Seferleri ve Tarih Bazlı Arama İndeksleri / SQL Migration
-- Versiyon: 20260902_002
-- Açıklama: Uçuş aramalarında seçilen tarihe göre (departure_date) hızlı indeksleme ve
--           marketplace_products tablosunda uçuş / tarih eşleşmesini destekleyen optimizasyon.
-- ========================================================================================

-- 1. marketplace_products Tablosu Tarih ve Uçuş İndeksleri
CREATE INDEX IF NOT EXISTS idx_marketplace_products_departure_date 
    ON public.marketplace_products (departure_date);

CREATE INDEX IF NOT EXISTS idx_marketplace_products_flight_search 
    ON public.marketplace_products (product_type, departure_city, region, departure_date);

CREATE INDEX IF NOT EXISTS idx_marketplace_products_airline 
    ON public.marketplace_products (airline_name, flight_number);

-- 2. Uçuş Seferleri Tablosu (Opsiyonel Canlı Sefer Havuzu & Yandex DB Senkronizasyonu)
CREATE TABLE IF NOT EXISTS public.flight_schedules (
    id TEXT PRIMARY KEY,
    airline_name TEXT NOT NULL,
    flight_number TEXT NOT NULL,
    departure_city TEXT NOT NULL,
    arrival_city TEXT NOT NULL,
    departure_airport_code VARCHAR(10) DEFAULT '',
    arrival_airport_code VARCHAR(10) DEFAULT '',
    departure_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time TIME NOT NULL,
    duration_minutes INT DEFAULT 240,
    is_charter BOOLEAN DEFAULT FALSE,
    is_direct BOOLEAN DEFAULT TRUE,
    baggage_kg INT DEFAULT 20,
    hand_baggage_kg INT DEFAULT 8,
    price NUMERIC(10, 2) NOT NULL DEFAULT 150.00,
    currency VARCHAR(10) DEFAULT 'EUR',
    operator_name TEXT DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS Güvenlik Politikası
ALTER TABLE public.flight_schedules ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Public read access for flight_schedules" ON public.flight_schedules;
CREATE POLICY "Public read access for flight_schedules" 
    ON public.flight_schedules FOR SELECT 
    USING (true);

-- 3. Tarih Bazlı Uçuş Arama RPC Fonksiyonu (Yandex / Supabase Entegrasyonu)
CREATE OR REPLACE FUNCTION public.search_flights_by_date(
    p_departure_city TEXT DEFAULT '',
    p_arrival_city TEXT DEFAULT '',
    p_departure_date TEXT DEFAULT '',
    p_is_charter_only BOOLEAN DEFAULT FALSE
)
RETURNS SETOF public.marketplace_products
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    RETURN QUERY
    SELECT *
    FROM public.marketplace_products mp
    WHERE mp.product_type = 'FLIGHT'
      AND (p_departure_city = '' OR mp.departure_city ILIKE '%' || p_departure_city || '%')
      AND (p_arrival_city = '' OR mp.region ILIKE '%' || p_arrival_city || '%' OR mp.country ILIKE '%' || p_arrival_city || '%')
      AND (p_departure_date = '' OR mp.departure_date = p_departure_date OR mp.departure_date IS NULL)
      AND (NOT p_is_charter_only OR mp.is_charter = TRUE)
    ORDER BY mp.price ASC;
END;
$$;
