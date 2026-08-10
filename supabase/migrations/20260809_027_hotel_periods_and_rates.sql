-- ============================================================
-- TourOS Migration: 20260809_027_hotel_periods_and_rates.sql
-- Otel Periyot ve Oda Tipi Maliyet/Satış/Kontenjan Geliştirmesi
-- ============================================================

ALTER TABLE public.hotel_season_rates
    ADD COLUMN IF NOT EXISTS cost_price NUMERIC(12,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS sale_price NUMERIC(12,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS allotment INT DEFAULT 10,
    ADD COLUMN IF NOT EXISTS room_type_name TEXT;
