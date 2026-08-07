-- ============================================================
-- TourOS 1.4.5 Booking Status History & Flexible Metadata SQL
-- ============================================================

-- 1. REZERVASYON DURUM GEÇMİŞİ (AUDIT LOGS) TABLOSU
CREATE TABLE IF NOT EXISTS public.booking_status_logs (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id    UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    from_status   TEXT,
    to_status     TEXT NOT NULL,
    changed_by    UUID REFERENCES public.users(id) ON DELETE SET NULL,
    notes         TEXT,
    tenant_id     UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_booking_status_logs_booking ON public.booking_status_logs(booking_id);

-- 2. FAZ 2 GENİŞLETİLEBİLİRLİK İÇİN FAZLA METADATA (JSONB) KOLONLARI
ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb,
    ADD COLUMN IF NOT EXISTS payment_status TEXT DEFAULT 'Unpaid',
    ADD COLUMN IF NOT EXISTS paid_amount NUMERIC(12,2) DEFAULT 0;

ALTER TABLE public.booking_items
    ADD COLUMN IF NOT EXISTS metadata JSONB DEFAULT '{}'::jsonb;

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.booking_status_logs ENABLE ROW LEVEL SECURITY;
