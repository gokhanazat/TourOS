-- Migration: 20260808_003_add_base_price_to_tours.sql
-- Description: tours tablosuna base_price kolonunun eklenmesi

ALTER TABLE public.tours 
ADD COLUMN IF NOT EXISTS base_price NUMERIC(12,2) DEFAULT 0.00;

COMMENT ON COLUMN public.tours.base_price IS 'Yetişkin / Temel tur fiyatı (₺)';
