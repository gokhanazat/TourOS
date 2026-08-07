-- ============================================================
-- TourOS 2.3.4 Sezon Fiyat Matrisi SQL Şeması
-- ============================================================

CREATE TABLE IF NOT EXISTS public.hotel_season_rates (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id          UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    room_type_id      UUID REFERENCES public.room_types(id) ON DELETE CASCADE,
    season_name       TEXT NOT NULL,                 -- Yüksek Sezon, Orta Sezon, Düşük Sezon, Bayram vb.
    start_date        DATE NOT NULL,
    end_date          DATE NOT NULL,
    single_price      NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    double_price      NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    triple_price      NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    extra_bed_price   NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    child_price       NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    currency          TEXT NOT NULL DEFAULT 'TRY',
    meal_plan         TEXT NOT NULL DEFAULT 'BB',    -- BB | HB | FB | AI | RO
    min_stay_days     INT NOT NULL DEFAULT 1,       -- Min konaklama gecesi
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id         UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by        UUID,

    CONSTRAINT chk_season_rate_dates CHECK (end_date >= start_date)
);

CREATE INDEX IF NOT EXISTS idx_hotel_season_rates_hotel  ON public.hotel_season_rates(hotel_id);
CREATE INDEX IF NOT EXISTS idx_hotel_season_rates_room   ON public.hotel_season_rates(room_type_id);
CREATE INDEX IF NOT EXISTS idx_hotel_season_rates_dates  ON public.hotel_season_rates(start_date, end_date);
CREATE INDEX IF NOT EXISTS idx_hotel_season_rates_tenant ON public.hotel_season_rates(tenant_id);

-- UPDATED_AT TRIGGER
CREATE TRIGGER trg_hotel_season_rates_updated_at
    BEFORE UPDATE ON public.hotel_season_rates
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.hotel_season_rates ENABLE ROW LEVEL SECURITY;

CREATE POLICY "hotel_season_rates_select" ON public.hotel_season_rates FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_season_rates_insert" ON public.hotel_season_rates FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_season_rates_update" ON public.hotel_season_rates FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_season_rates_delete" ON public.hotel_season_rates FOR DELETE
    USING (tenant_id = public.current_tenant_id());
