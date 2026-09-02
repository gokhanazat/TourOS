-- ==============================================================================
-- TourOS Migration: 20260902_001_ensure_marketplace_products_and_sync_permissions.sql
-- YANDEX DB -> SUPABASE MARKETPLACE & CORE DATA SYNC & PERMISSIONS
-- ==============================================================================

-- 1. Tabloyu ve Eksik Olabilecek Tüm Sütunları Garantiye Al
CREATE TABLE IF NOT EXISTS public.marketplace_products (
    id TEXT PRIMARY KEY,
    product_type TEXT NOT NULL DEFAULT 'PACKAGE_TOUR',
    tour_name TEXT DEFAULT '',
    operator_id INT NOT NULL DEFAULT 0,
    operator_name TEXT NOT NULL DEFAULT '',
    operator_link TEXT DEFAULT '',
    price NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    fuel_charge NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    currency TEXT NOT NULL DEFAULT 'RUB',
    hotel_id INT DEFAULT 0,
    hotel_name TEXT NOT NULL DEFAULT '',
    hotel_category INT DEFAULT 5,
    country TEXT DEFAULT '',
    country_code TEXT DEFAULT '',
    country_name TEXT DEFAULT '',
    region TEXT DEFAULT '',
    sub_region TEXT DEFAULT '',
    room_type TEXT DEFAULT '',
    meal_type TEXT DEFAULT '',
    departure_city TEXT DEFAULT '',
    departure_date TEXT DEFAULT '',
    nights INT DEFAULT 7,
    adults INT DEFAULT 2,
    childs INT DEFAULT 0,
    is_charter BOOLEAN DEFAULT TRUE,
    is_promo BOOLEAN DEFAULT FALSE,
    airline_name TEXT DEFAULT '',
    flight_number TEXT DEFAULT '',
    baggage_kg INT DEFAULT 20,
    picture_url TEXT DEFAULT '',
    picture TEXT DEFAULT '',
    latitude NUMERIC(10,6),
    longitude NUMERIC(10,6),
    hotel_rating NUMERIC(4,2) DEFAULT 8.0,
    beach_line INT DEFAULT 1,
    is_instant_confirmation BOOLEAN DEFAULT TRUE,
    has_transfer BOOLEAN DEFAULT TRUE,
    is_direct_flight BOOLEAN DEFAULT TRUE,
    amenities JSONB DEFAULT '[]'::jsonb,
    is_published BOOLEAN DEFAULT TRUE,
    custom_price_override NUMERIC(12,2),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Var olan tabloda eksik olabilecek kolonları ekle
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS country_code TEXT DEFAULT '';
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS country_name TEXT DEFAULT '';
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS picture TEXT DEFAULT '';
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS hotel_rating NUMERIC(4,2) DEFAULT 8.0;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS beach_line INT DEFAULT 1;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS is_instant_confirmation BOOLEAN DEFAULT TRUE;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS has_transfer BOOLEAN DEFAULT TRUE;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS is_direct_flight BOOLEAN DEFAULT TRUE;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS amenities JSONB DEFAULT '[]'::jsonb;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS is_published BOOLEAN DEFAULT TRUE;
ALTER TABLE public.marketplace_products ADD COLUMN IF NOT EXISTS custom_price_override NUMERIC(12,2);
ALTER TABLE public.marketplace_products ALTER COLUMN departure_date TYPE TEXT;

-- 3. RLS ve Rol İzinleri
ALTER TABLE public.marketplace_products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "marketplace_products_select_policy" ON public.marketplace_products;
CREATE POLICY "marketplace_products_select_policy" ON public.marketplace_products
    FOR SELECT USING (TRUE);

DROP POLICY IF EXISTS "marketplace_products_all_policy" ON public.marketplace_products;
CREATE POLICY "marketplace_products_all_policy" ON public.marketplace_products
    FOR ALL USING (TRUE) WITH CHECK (TRUE);

GRANT ALL ON TABLE public.marketplace_products TO anon;
GRANT ALL ON TABLE public.marketplace_products TO authenticated;
GRANT ALL ON TABLE public.marketplace_products TO service_role;

-- 4. Hızlı Arama & Filtreleme İndeksleri
CREATE INDEX IF NOT EXISTS idx_marketplace_products_hotel ON public.marketplace_products(hotel_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_tour_name ON public.marketplace_products(tour_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_operator ON public.marketplace_products(operator_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_departure ON public.marketplace_products(departure_city);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_is_published ON public.marketplace_products(is_published);
