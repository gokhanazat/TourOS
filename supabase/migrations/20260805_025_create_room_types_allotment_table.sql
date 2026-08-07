-- ============================================================
-- TourOS 2.3.2 Room Types & Allotment Tracking SQL Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS public.room_types (
    id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id              UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    name                  TEXT NOT NULL,
    description           TEXT,
    base_price_per_night  NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    currency              TEXT NOT NULL DEFAULT 'TRY',
    max_occupancy         INT NOT NULL DEFAULT 2,
    total_rooms           INT NOT NULL DEFAULT 10,
    allotment             INT NOT NULL DEFAULT 5,
    booked_rooms          INT NOT NULL DEFAULT 0,
    is_active             BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id             UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by            UUID
);

CREATE INDEX IF NOT EXISTS idx_room_types_hotel ON public.room_types(hotel_id);
CREATE INDEX IF NOT EXISTS idx_room_types_tenant ON public.room_types(tenant_id);

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.room_types ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Room types tenant isolation" ON public.room_types
    FOR ALL USING (tenant_id = public.current_tenant_id());
