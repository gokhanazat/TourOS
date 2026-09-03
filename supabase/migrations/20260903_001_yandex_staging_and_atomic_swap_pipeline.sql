-- ==============================================================================
-- TourOS Migration: 20260903_001_yandex_staging_and_atomic_swap_pipeline.sql
-- YANDEX CLOUD & SUPABASE SIFIR KESİNTİ (ZERO DOWNTIME) VERİ SENKRONİZASYON ALTYAPISI
-- ==============================================================================

-- 1. STAGING (GEÇİCİ ÇEKİM) TABLOSU OLUŞTURULMASI
CREATE TABLE IF NOT EXISTS public.marketplace_products_staging (
    id                      TEXT PRIMARY KEY,
    product_type            VARCHAR(50) NOT NULL DEFAULT 'PACKAGE_TOUR',
    tour_name               TEXT NOT NULL,
    operator_id             INT DEFAULT 0,
    operator_name           TEXT NOT NULL DEFAULT '',
    operator_link           TEXT DEFAULT '',
    price                   NUMERIC(12, 2) NOT NULL DEFAULT 0.0,
    fuel_charge             NUMERIC(12, 2) DEFAULT 0.0,
    currency                VARCHAR(10) NOT NULL DEFAULT 'RUB',
    hotel_id                INT DEFAULT 0,
    hotel_name              TEXT NOT NULL DEFAULT '',
    hotel_category          INT DEFAULT 3,
    hotel_rating            NUMERIC(3, 1) DEFAULT 0.0,
    country                 VARCHAR(100) NOT NULL DEFAULT '',
    country_code            VARCHAR(10) DEFAULT '',
    country_name            VARCHAR(100) DEFAULT '',
    region                  VARCHAR(100) NOT NULL DEFAULT '',
    sub_region              VARCHAR(100) DEFAULT '',
    room_type               TEXT DEFAULT '',
    meal_type               TEXT DEFAULT '',
    departure_city          VARCHAR(100) NOT NULL DEFAULT '',
    departure_date          DATE,
    nights                  INT NOT NULL DEFAULT 7,
    adults                  INT NOT NULL DEFAULT 2,
    childs                  INT NOT NULL DEFAULT 0,
    is_charter              BOOLEAN DEFAULT true,
    is_promo                BOOLEAN DEFAULT false,
    airline                 VARCHAR(100) DEFAULT '',
    flight_number           VARCHAR(50) DEFAULT '',
    baggage_kg              INT DEFAULT 20,
    picture_url             TEXT DEFAULT '',
    rating                  NUMERIC(3, 1) DEFAULT 0.0,
    stars                   INT DEFAULT 3,
    has_transfer            BOOLEAN DEFAULT true,
    is_direct_flight        BOOLEAN DEFAULT true,
    is_instant_confirmation BOOLEAN DEFAULT true,
    amenities               TEXT[] DEFAULT ARRAY[]::TEXT[],
    is_published            BOOLEAN DEFAULT true,
    is_active               BOOLEAN DEFAULT true,
    api_provider            VARCHAR(50) DEFAULT 'TOURVISOR',
    is_api_synced           BOOLEAN DEFAULT true,
    created_at              TIMESTAMPTZ DEFAULT NOW(),
    updated_at              TIMESTAMPTZ DEFAULT NOW(),
    last_synced_at          TIMESTAMPTZ DEFAULT NOW()
);

-- 2. STAGING TABLO İNDEKSLERİ
CREATE INDEX IF NOT EXISTS idx_staging_departure ON public.marketplace_products_staging(departure_city);
CREATE INDEX IF NOT EXISTS idx_staging_country ON public.marketplace_products_staging(country);
CREATE INDEX IF NOT EXISTS idx_staging_region ON public.marketplace_products_staging(region);
CREATE INDEX IF NOT EXISTS idx_staging_operator ON public.marketplace_products_staging(operator_name);
CREATE INDEX IF NOT EXISTS idx_staging_price ON public.marketplace_products_staging(price);

-- 3. ATOMIC SWAP (SIFIR KESİNTİ İLE TABLO DEĞİŞTİRME) FONKSİYONU
CREATE OR REPLACE FUNCTION public.atomic_swap_marketplace_products()
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    staging_count INT;
BEGIN
    -- Güvenlik Kontrolü: Staging tablosunda veri var mı?
    SELECT COUNT(*) INTO staging_count FROM public.marketplace_products_staging;
    IF staging_count = 0 THEN
        RAISE EXCEPTION 'Atomic Swap İptal Edildi: Staging tablosunda hiç veri yok!';
    END IF;

    -- 1 ms içinde atomic olarak tabloları swap yap
    DROP TABLE IF EXISTS public.marketplace_products_old CASCADE;
    
    ALTER TABLE public.marketplace_products RENAME TO marketplace_products_old;
    ALTER TABLE public.marketplace_products_staging RENAME TO marketplace_products;
    
    -- Yeni bir boş staging tablosu aç (sıradaki çekim için)
    CREATE TABLE public.marketplace_products_staging (LIKE public.marketplace_products INCLUDING ALL);
    
    -- Eski yedek tabloyu kaldır
    DROP TABLE IF EXISTS public.marketplace_products_old CASCADE;
END;
$$;

GRANT EXECUTE ON FUNCTION public.atomic_swap_marketplace_products() TO service_role, postgres;
