-- ============================================================
-- TourOS Migration: 20260809_028_fix_room_types_and_season_rates.sql
-- Otel Periyot Fiyatları ve Oda Tipleri Kolon Güncellemesi
-- ============================================================

-- 1. hotel_season_rates tablosuna eksik alanlar ekleniyor
ALTER TABLE public.hotel_season_rates
    ADD COLUMN IF NOT EXISTS cost_price NUMERIC(12,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS sale_price NUMERIC(12,2) DEFAULT 0.00,
    ADD COLUMN IF NOT EXISTS allotment INT DEFAULT 10,
    ADD COLUMN IF NOT EXISTS room_type_name TEXT;

-- 2. room_types tablosuna base_price_per_night kolonu uyumluluğu ekleniyor
ALTER TABLE public.room_types
    ADD COLUMN IF NOT EXISTS base_price_per_night NUMERIC(12,2) DEFAULT 0.00;
