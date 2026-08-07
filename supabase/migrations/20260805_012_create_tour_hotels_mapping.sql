-- ============================================================
-- TourOS 1.4.2 Tur ve Otel Eşleşme İndeksleri & Yardımcı Tablo
-- ============================================================

CREATE TABLE IF NOT EXISTS public.tour_hotels (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tour_id     UUID NOT NULL REFERENCES public.tours(id) ON DELETE CASCADE,
    hotel_id    UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

    UNIQUE (tour_id, hotel_id)
);

CREATE INDEX IF NOT EXISTS idx_tour_hotels_tour  ON public.tour_hotels(tour_id);
CREATE INDEX IF NOT EXISTS idx_tour_hotels_hotel ON public.tour_hotels(hotel_id);

ALTER TABLE public.tour_hotels ENABLE ROW LEVEL SECURITY;

CREATE POLICY "tour_hotels_select" ON public.tour_hotels FOR SELECT USING (tenant_id = public.current_tenant_id());
CREATE POLICY "tour_hotels_insert" ON public.tour_hotels FOR INSERT WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tour_hotels_delete" ON public.tour_hotels FOR DELETE USING (tenant_id = public.current_tenant_id());
