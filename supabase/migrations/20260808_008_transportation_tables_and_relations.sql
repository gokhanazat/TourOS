-- ============================================================
-- TourOS Migration: 20260808_008_transportation_tables_and_relations.sql
-- Prompt 0.2.5: Ulaşım Tabloları (vehicles, drivers, guides, transfers)
-- transfers tablosunun vehicles, drivers, guides ve bookings ile FK ilişkileri ve RLS.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. VEHICLES TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.vehicles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate_number    TEXT NOT NULL,
    brand           TEXT NOT NULL,
    model           TEXT NOT NULL,
    capacity        INT NOT NULL DEFAULT 16,
    year            INT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    CONSTRAINT uq_vehicles_tenant_plate UNIQUE (tenant_id, plate_number)
);

CREATE INDEX IF NOT EXISTS idx_vehicles_tenant ON public.vehicles(tenant_id);

-- ===================  2. DRIVERS TABLOSU  ==================
CREATE TABLE IF NOT EXISTS public.drivers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       TEXT NOT NULL,
    phone           TEXT NOT NULL,
    license_number  TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX IF NOT EXISTS idx_drivers_tenant ON public.drivers(tenant_id);

-- ===================  3. GUIDES TABLOSU  ===================
CREATE TABLE IF NOT EXISTS public.guides (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       TEXT NOT NULL,
    phone           TEXT NOT NULL,
    license_number  TEXT,
    languages       TEXT[] DEFAULT ARRAY['tr', 'en'],
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX IF NOT EXISTS idx_guides_tenant ON public.guides(tenant_id);

-- ===================  4. TRANSFERS TABLOSU  ================
-- FK İlişkileri: vehicles, drivers, guides, bookings
CREATE TABLE IF NOT EXISTS public.transfers (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id          UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    vehicle_id          UUID REFERENCES public.vehicles(id) ON DELETE SET NULL,
    driver_id           UUID REFERENCES public.drivers(id) ON DELETE SET NULL,
    guide_id            UUID REFERENCES public.guides(id) ON DELETE SET NULL,
    transfer_type       TEXT NOT NULL DEFAULT 'AIRPORT_ARRIVAL', -- AIRPORT_ARRIVAL, AIRPORT_DEPARTURE, HOTEL_INTER, CUSTOM
    pickup_location     TEXT NOT NULL,
    dropoff_location    TEXT NOT NULL,
    pickup_datetime     TIMESTAMPTZ NOT NULL,
    flight_number       TEXT,
    passenger_count     INT NOT NULL DEFAULT 1,
    status              TEXT NOT NULL DEFAULT 'PLANNED', -- PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    notes               TEXT,

    -- Audit & Multi-tenant
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID
);

-- Ensure columns exist if table was already created in earlier migration
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS booking_id UUID REFERENCES public.bookings(id) ON DELETE SET NULL;
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS vehicle_id UUID REFERENCES public.vehicles(id) ON DELETE SET NULL;
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS driver_id UUID REFERENCES public.drivers(id) ON DELETE SET NULL;
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS guide_id UUID REFERENCES public.guides(id) ON DELETE SET NULL;
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS transfer_type TEXT NOT NULL DEFAULT 'AIRPORT_ARRIVAL';
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS pickup_location TEXT NOT NULL DEFAULT '';
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS dropoff_location TEXT NOT NULL DEFAULT '';
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS pickup_datetime TIMESTAMPTZ NOT NULL DEFAULT now();
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS flight_number TEXT;
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS passenger_count INT NOT NULL DEFAULT 1;
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS status TEXT NOT NULL DEFAULT 'PLANNED';
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE public.transfers ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_transfers_tenant ON public.transfers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_transfers_booking ON public.transfers(booking_id);
CREATE INDEX IF NOT EXISTS idx_transfers_vehicle ON public.transfers(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_transfers_driver ON public.transfers(driver_id);
CREATE INDEX IF NOT EXISTS idx_transfers_guide ON public.transfers(guide_id);
CREATE INDEX IF NOT EXISTS idx_transfers_pickup_time ON public.transfers(pickup_datetime);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DROP TRIGGER IF EXISTS trg_vehicles_updated_at ON public.vehicles;
CREATE TRIGGER trg_vehicles_updated_at
    BEFORE UPDATE ON public.vehicles
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_drivers_updated_at ON public.drivers;
CREATE TRIGGER trg_drivers_updated_at
    BEFORE UPDATE ON public.drivers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_guides_updated_at ON public.guides;
CREATE TRIGGER trg_guides_updated_at
    BEFORE UPDATE ON public.guides
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_transfers_updated_at ON public.transfers;
CREATE TRIGGER trg_transfers_updated_at
    BEFORE UPDATE ON public.transfers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- HELPER: Tenant Kontrol Fonksiyonu
-- ============================================================
CREATE OR REPLACE FUNCTION public.is_valid_tenant(check_tenant_id UUID)
RETURNS BOOLEAN AS $$
    SELECT check_tenant_id = public.current_tenant_id() 
        OR check_tenant_id = '00000000-0000-0000-0000-000000000001'::uuid;
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public;

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı RLS)
-- ============================================================
ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.drivers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.guides ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transfers ENABLE ROW LEVEL SECURITY;

-- VEHICLES RLS
DROP POLICY IF EXISTS "vehicles_select" ON public.vehicles;
DROP POLICY IF EXISTS "vehicles_insert" ON public.vehicles;
DROP POLICY IF EXISTS "vehicles_update" ON public.vehicles;
DROP POLICY IF EXISTS "vehicles_delete" ON public.vehicles;

CREATE POLICY "vehicles_select" ON public.vehicles FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "vehicles_insert" ON public.vehicles FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "vehicles_update" ON public.vehicles FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "vehicles_delete" ON public.vehicles FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- DRIVERS RLS
DROP POLICY IF EXISTS "drivers_select" ON public.drivers;
DROP POLICY IF EXISTS "drivers_insert" ON public.drivers;
DROP POLICY IF EXISTS "drivers_update" ON public.drivers;
DROP POLICY IF EXISTS "drivers_delete" ON public.drivers;

CREATE POLICY "drivers_select" ON public.drivers FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "drivers_insert" ON public.drivers FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "drivers_update" ON public.drivers FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "drivers_delete" ON public.drivers FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- GUIDES RLS
DROP POLICY IF EXISTS "guides_select" ON public.guides;
DROP POLICY IF EXISTS "guides_insert" ON public.guides;
DROP POLICY IF EXISTS "guides_update" ON public.guides;
DROP POLICY IF EXISTS "guides_delete" ON public.guides;

CREATE POLICY "guides_select" ON public.guides FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "guides_insert" ON public.guides FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "guides_update" ON public.guides FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "guides_delete" ON public.guides FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- TRANSFERS RLS
DROP POLICY IF EXISTS "transfers_select" ON public.transfers;
DROP POLICY IF EXISTS "transfers_insert" ON public.transfers;
DROP POLICY IF EXISTS "transfers_update" ON public.transfers;
DROP POLICY IF EXISTS "transfers_delete" ON public.transfers;

CREATE POLICY "transfers_select" ON public.transfers FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "transfers_insert" ON public.transfers FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "transfers_update" ON public.transfers FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "transfers_delete" ON public.transfers FOR DELETE USING (public.is_valid_tenant(tenant_id));

