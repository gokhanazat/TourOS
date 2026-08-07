-- ============================================================
-- TourOS 2.5.3 Guide Mobile & Passenger Check-in Migration SQL
-- ============================================================

ALTER TABLE public.passengers
    ADD COLUMN IF NOT EXISTS departure_id UUID REFERENCES public.departures(id) ON DELETE CASCADE,
    ADD COLUMN IF NOT EXISTS tc_passport TEXT,
    ADD COLUMN IF NOT EXISTS pickup_hotel TEXT,
    ADD COLUMN IF NOT EXISTS seat_number TEXT,
    ADD COLUMN IF NOT EXISTS is_check_in BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS special_notes TEXT;

-- Performans İndeksleri
CREATE INDEX IF NOT EXISTS idx_passengers_departure ON public.passengers(departure_id);
CREATE INDEX IF NOT EXISTS idx_passengers_tenant ON public.passengers(tenant_id);

-- RLS Güvenlik Politikası
ALTER TABLE public.passengers ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "passengers_tenant_isolation_policy" ON public.passengers;
CREATE POLICY "passengers_tenant_isolation_policy" ON public.passengers
    FOR ALL
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
