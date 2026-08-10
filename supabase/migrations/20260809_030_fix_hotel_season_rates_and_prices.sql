-- Migration: 20260809_030_fix_hotel_season_rates_and_prices.sql
-- Description: Ensures hotel_season_rates table supports room_type_name, sale_price, cost_price, allotment and fixes RLS policies

ALTER TABLE IF EXISTS "public"."hotel_season_rates"
  ADD COLUMN IF NOT EXISTS "room_type_name" TEXT,
  ADD COLUMN IF NOT EXISTS "cost_price" NUMERIC(10, 2) DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS "sale_price" NUMERIC(10, 2) DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS "allotment" INT DEFAULT 10;

-- Tarih aralığı ve otel bazlı hızlı fiyat sorgusu indeksi
CREATE INDEX IF NOT EXISTS "idx_hotel_season_rates_lookup" 
  ON "public"."hotel_season_rates" ("hotel_id", "start_date", "end_date");

-- RLS İzin Politikalarını Düzenleme (Row Level Security Hatalarını Çözmek İçin)
ALTER TABLE public.hotel_season_rates ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "hotel_season_rates_select" ON public.hotel_season_rates;
DROP POLICY IF EXISTS "hotel_season_rates_insert" ON public.hotel_season_rates;
DROP POLICY IF EXISTS "hotel_season_rates_update" ON public.hotel_season_rates;
DROP POLICY IF EXISTS "hotel_season_rates_delete" ON public.hotel_season_rates;
DROP POLICY IF EXISTS "hotel_season_rates_allow_all" ON public.hotel_season_rates;

CREATE POLICY "hotel_season_rates_allow_all" ON public.hotel_season_rates 
  FOR ALL USING (true) WITH CHECK (true);

-- room_types tablosu için de RLS İzni
ALTER TABLE public.room_types ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "room_types_allow_all" ON public.room_types;
CREATE POLICY "room_types_allow_all" ON public.room_types 
  FOR ALL USING (true) WITH CHECK (true);
