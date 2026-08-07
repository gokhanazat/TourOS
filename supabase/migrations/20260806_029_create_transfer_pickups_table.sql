-- ============================================================
-- TourOS 2.4.3 Driver Pickup List & Google Maps Migration
-- ============================================================

CREATE TABLE IF NOT EXISTS public.transfer_pickups (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transfer_id       UUID NOT NULL REFERENCES public.transfers(id) ON DELETE CASCADE,
    passenger_name    TEXT NOT NULL,
    passenger_phone   TEXT,
    hotel_name        TEXT NOT NULL,
    location_name     TEXT NOT NULL,
    latitude          DOUBLE PRECISION NOT NULL DEFAULT 41.0082,
    longitude         DOUBLE PRECISION NOT NULL DEFAULT 28.9784,
    scheduled_time    TEXT NOT NULL,
    status            TEXT NOT NULL DEFAULT 'pending', -- 'pending' | 'picked_up' | 'no_show'
    pax_count         INT NOT NULL DEFAULT 1,
    room_number       TEXT,
    notes             TEXT,

    -- audit
    tenant_id         UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by        UUID
);

CREATE INDEX IF NOT EXISTS idx_transfer_pickups_transfer ON public.transfer_pickups(transfer_id);
CREATE INDEX IF NOT EXISTS idx_transfer_pickups_tenant   ON public.transfer_pickups(tenant_id);
CREATE INDEX IF NOT EXISTS idx_transfer_pickups_status   ON public.transfer_pickups(status);

-- UPDATED_AT TRIGGER
DROP TRIGGER IF EXISTS trg_transfer_pickups_updated_at ON public.transfer_pickups;
CREATE TRIGGER trg_transfer_pickups_updated_at
    BEFORE UPDATE ON public.transfer_pickups
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.transfer_pickups ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "transfer_pickups_select" ON public.transfer_pickups;
DROP POLICY IF EXISTS "transfer_pickups_insert" ON public.transfer_pickups;
DROP POLICY IF EXISTS "transfer_pickups_update" ON public.transfer_pickups;
DROP POLICY IF EXISTS "transfer_pickups_delete" ON public.transfer_pickups;

CREATE POLICY "transfer_pickups_select" ON public.transfer_pickups FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "transfer_pickups_insert" ON public.transfer_pickups FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "transfer_pickups_update" ON public.transfer_pickups FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "transfer_pickups_delete" ON public.transfer_pickups FOR DELETE
    USING (tenant_id = public.current_tenant_id());
