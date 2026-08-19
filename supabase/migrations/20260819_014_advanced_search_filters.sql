-- ============================================================
-- TourOS Migration: 20260819_014_advanced_search_filters.sql
-- Sletat.ru Stili Detaylı Arama Filtre Kolonları & İndeksleri
-- ============================================================

-- 1. Detaylı Arama Filtre Kolonlarını Ekleme
ALTER TABLE public.marketplace_products 
    ADD COLUMN IF NOT EXISTS hotel_rating NUMERIC(3,1) DEFAULT 8.0,
    ADD COLUMN IF NOT EXISTS beach_line INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS is_instant_confirmation BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS has_transfer BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS is_direct_flight BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS amenities TEXT[] DEFAULT '{}';

-- 2. Filtreleme Performans İndeksleri
CREATE INDEX IF NOT EXISTS idx_marketplace_rating ON public.marketplace_products(hotel_rating);
CREATE INDEX IF NOT EXISTS idx_marketplace_beach_line ON public.marketplace_products(beach_line);
CREATE INDEX IF NOT EXISTS idx_marketplace_amenities ON public.marketplace_products USING GIN(amenities);
