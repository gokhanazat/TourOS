-- ============================================================
-- TourOS Migration: 20260810_025_create_marketplace_products_table.sql
-- Operatör API/SOAP verilerinin toplanacağı Ortak Ürünler Tablosu
-- ============================================================

CREATE TABLE IF NOT EXISTS public.marketplace_products (
    id                   TEXT PRIMARY KEY,
    product_type         TEXT NOT NULL DEFAULT 'PACKAGE_TOUR',
    tour_name            TEXT DEFAULT '',
    operator_id          INT NOT NULL DEFAULT 0,
    operator_name        TEXT NOT NULL DEFAULT '',
    operator_link        TEXT DEFAULT '',
    price                NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    fuel_charge          NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    currency             TEXT NOT NULL DEFAULT 'RUB',
    hotel_id             INT DEFAULT 0,
    hotel_name           TEXT NOT NULL DEFAULT '',
    hotel_category       INT DEFAULT 5,
    country              TEXT DEFAULT '',
    region               TEXT DEFAULT '',
    sub_region           TEXT DEFAULT '',
    room_type            TEXT DEFAULT '',
    meal_type            TEXT DEFAULT '',
    departure_city       TEXT DEFAULT '',
    departure_date       TEXT DEFAULT '',
    nights               INT DEFAULT 7,
    adults               INT DEFAULT 2,
    childs               INT DEFAULT 0,
    is_charter           BOOLEAN DEFAULT TRUE,
    is_promo             BOOLEAN DEFAULT FALSE,
    airline_name         TEXT DEFAULT '',
    flight_number        TEXT DEFAULT '',
    baggage_kg           INT DEFAULT 20,
    picture_url          TEXT DEFAULT '',
    latitude             NUMERIC(10,6),
    longitude            NUMERIC(10,6),
    created_at           TIMESTAMPTZ DEFAULT now()
);

-- Var olan tabloda departure_date sütununu TEXT türüne çevir:
ALTER TABLE public.marketplace_products ALTER COLUMN departure_date TYPE TEXT;

-- Endexler (Hızlı Arama & Filtreleme)
CREATE INDEX IF NOT EXISTS idx_marketplace_products_hotel ON public.marketplace_products(hotel_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_tour_name ON public.marketplace_products(tour_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_operator ON public.marketplace_products(operator_name);
CREATE INDEX IF NOT EXISTS idx_marketplace_products_departure ON public.marketplace_products(departure_city);
