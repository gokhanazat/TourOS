-- Migration: 20260808_001_add_child_pricing_to_tours.sql
-- Description: Turlar tablosuna 0-6 yaş ve 7-12 yaş çocuk fiyatlandırma kolonlarının eklenmesi

ALTER TABLE tours 
ADD COLUMN IF NOT EXISTS child_price_0_6 NUMERIC(12,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS child_price_7_12 NUMERIC(12,2) DEFAULT 0.00;

COMMENT ON COLUMN tours.child_price_0_6 IS '0-6 yaş arası çocuk kişi başı fiyatı';
COMMENT ON COLUMN tours.child_price_7_12 IS '7-12 yaş arası çocuk kişi başı fiyatı';
