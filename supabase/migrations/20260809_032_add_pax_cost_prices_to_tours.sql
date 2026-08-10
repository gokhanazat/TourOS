-- Migration: 20260809_032_add_pax_cost_prices_to_tours.sql
-- Description: Adds adult_cost_price, child_cost_price_0_6, child_cost_price_7_12 columns to tours table for pax margin profitability analysis

ALTER TABLE IF EXISTS "public"."tours"
  ADD COLUMN IF NOT EXISTS "adult_cost_price" NUMERIC(10, 2) DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS "child_cost_price_0_6" NUMERIC(10, 2) DEFAULT 0.00,
  ADD COLUMN IF NOT EXISTS "child_cost_price_7_12" NUMERIC(10, 2) DEFAULT 0.00;
