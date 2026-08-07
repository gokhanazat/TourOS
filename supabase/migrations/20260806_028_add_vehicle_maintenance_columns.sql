-- ============================================================
-- TourOS 2.4.1 Vehicles Table Maintenance Columns Migration
-- ============================================================

ALTER TABLE public.vehicles
    ADD COLUMN IF NOT EXISTS last_maintenance_date DATE,
    ADD COLUMN IF NOT EXISTS next_maintenance_date DATE,
    ADD COLUMN IF NOT EXISTS maintenance_notes TEXT;
