-- ============================================================
-- TourOS 2.3.1 Hotels CRUD Schema Migration
-- ============================================================

ALTER TABLE public.hotels
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS cover_image_url TEXT,
    ADD COLUMN IF NOT EXISTS star_rating INT DEFAULT 4;

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.hotels ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Hotels tenant isolation" ON public.hotels
    FOR ALL USING (tenant_id = public.current_tenant_id());
