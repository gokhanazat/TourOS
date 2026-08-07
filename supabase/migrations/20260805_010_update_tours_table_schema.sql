-- ============================================================
-- TourOS 1.3.4 Tur Listesi Ekranı Schema Güncellemesi
-- ============================================================

ALTER TABLE public.tours
    ADD COLUMN IF NOT EXISTS code TEXT,
    ADD COLUMN IF NOT EXISTS category TEXT,
    ADD COLUMN IF NOT EXISTS country TEXT,
    ADD COLUMN IF NOT EXISTS city TEXT,
    ADD COLUMN IF NOT EXISTS capacity INT DEFAULT 20,
    ADD COLUMN IF NOT EXISTS min_participants INT DEFAULT 1,
    ADD COLUMN IF NOT EXISTS max_participants INT DEFAULT 30,
    ADD COLUMN IF NOT EXISTS cancellation_policy TEXT,
    ADD COLUMN IF NOT EXISTS insurance_details TEXT;

CREATE INDEX IF NOT EXISTS idx_tours_code ON public.tours(code);
CREATE INDEX IF NOT EXISTS idx_tours_category_str ON public.tours(category);
CREATE INDEX IF NOT EXISTS idx_tours_is_active ON public.tours(is_active);
