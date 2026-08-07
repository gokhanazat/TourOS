-- ============================================================
-- TourOS 2.2.1 Departures Veri Modeli ve Çoklu Otel İlişkisi SQL
-- ============================================================

-- 1. DEPARTURES TABLOSUNA ÖZEL FİYAT VE KONTENJAN KOLONLARI
ALTER TABLE public.departures
    ADD COLUMN IF NOT EXISTS child_price_override NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS infant_price_override NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS currency TEXT DEFAULT 'TRY',
    ADD COLUMN IF NOT EXISTS option_deadline_days INT DEFAULT 7,
    ADD COLUMN IF NOT EXISTS is_guaranteed BOOLEAN DEFAULT FALSE;

-- 2. DEPARTURE_HOTELS (KALKIŞ TARİHİNE ÖZEL ATANMIŞ OTELLER) TABLOSU
CREATE TABLE IF NOT EXISTS public.departure_hotels (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    departure_id  UUID NOT NULL REFERENCES public.departures(id) ON DELETE CASCADE,
    hotel_id      UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    night_count   INT NOT NULL DEFAULT 1,
    sort_order    INT NOT NULL DEFAULT 1,
    tenant_id     UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (departure_id, hotel_id)
);

CREATE INDEX IF NOT EXISTS idx_departure_hotels_dep ON public.departure_hotels(departure_id);
CREATE INDEX IF NOT EXISTS idx_departure_hotels_tenant ON public.departure_hotels(tenant_id);

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.departure_hotels ENABLE ROW LEVEL SECURITY;
