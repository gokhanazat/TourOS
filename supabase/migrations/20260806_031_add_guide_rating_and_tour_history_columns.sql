-- ============================================================
-- TourOS 2.5.1 Guides Table Rating and Tour History Migration
-- ============================================================

ALTER TABLE public.guides
    ADD COLUMN IF NOT EXISTS rating DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    ADD COLUMN IF NOT EXISTS total_tours_completed INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS notes TEXT;
