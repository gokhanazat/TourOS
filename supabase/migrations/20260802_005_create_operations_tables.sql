-- ============================================================
-- TourOS Operations Tables Migration
-- vehicles, drivers, guides → transfers
-- ============================================================

-- ===================  1. VEHICLES  ==========================
CREATE TABLE IF NOT EXISTS public.vehicles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate_number    TEXT NOT NULL,
    brand           TEXT,
    model           TEXT,
    year            INT,
    capacity        INT NOT NULL DEFAULT 0,
    vehicle_type    TEXT NOT NULL DEFAULT 'minibus',  -- car | minibus | midibus | bus
    color           TEXT,
    is_owned        BOOLEAN NOT NULL DEFAULT TRUE,    -- firma sahipliği mi, kiralık mı
    owner_info      TEXT,                             -- kiralık ise sahip bilgisi
    insurance_expiry DATE,
    inspection_expiry DATE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tenant_id, plate_number)
);

CREATE INDEX idx_vehicles_tenant ON public.vehicles(tenant_id);
CREATE INDEX idx_vehicles_type   ON public.vehicles(vehicle_type);

-- ===================  2. DRIVERS  ===========================
CREATE TABLE IF NOT EXISTS public.drivers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       TEXT NOT NULL,
    phone           TEXT,
    email           TEXT,
    license_class   TEXT,                             -- B, D1, D …
    license_expiry  DATE,
    tc_no           TEXT,
    birth_date      DATE,
    address         TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_drivers_tenant ON public.drivers(tenant_id);

-- ===================  3. GUIDES  ============================
CREATE TABLE IF NOT EXISTS public.guides (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       TEXT NOT NULL,
    phone           TEXT,
    email           TEXT,
    license_number  TEXT,                             -- rehber kokart no
    languages       TEXT[],                           -- {'en','de','ar'}
    specialization  TEXT,                             -- kültür, doğa, gastro …
    tc_no           TEXT,
    birth_date      DATE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_guides_tenant ON public.guides(tenant_id);

-- ===================  4. TRANSFERS  =========================
CREATE TABLE IF NOT EXISTS public.transfers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    departure_id    UUID REFERENCES public.departures(id) ON DELETE SET NULL,
    vehicle_id      UUID REFERENCES public.vehicles(id) ON DELETE SET NULL,
    driver_id       UUID REFERENCES public.drivers(id) ON DELETE SET NULL,
    guide_id        UUID REFERENCES public.guides(id) ON DELETE SET NULL,

    transfer_type   TEXT NOT NULL DEFAULT 'tour',     -- tour | airport | intercity | custom
    origin          TEXT NOT NULL,
    destination     TEXT NOT NULL,
    pickup_time     TIMESTAMPTZ,
    dropoff_time    TIMESTAMPTZ,
    pax_count       INT NOT NULL DEFAULT 0,
    status          TEXT NOT NULL DEFAULT 'planned',  -- planned | in_progress | completed | cancelled
    price           NUMERIC(12,2) NOT NULL DEFAULT 0,
    currency        TEXT NOT NULL DEFAULT 'TRY',
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_transfers_tenant    ON public.transfers(tenant_id);
CREATE INDEX idx_transfers_booking   ON public.transfers(booking_id);
CREATE INDEX idx_transfers_departure ON public.transfers(departure_id);
CREATE INDEX idx_transfers_vehicle   ON public.transfers(vehicle_id);
CREATE INDEX idx_transfers_driver    ON public.transfers(driver_id);
CREATE INDEX idx_transfers_guide     ON public.transfers(guide_id);
CREATE INDEX idx_transfers_pickup    ON public.transfers(pickup_time);
CREATE INDEX idx_transfers_status    ON public.transfers(status);

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================
CREATE TRIGGER trg_vehicles_updated_at
    BEFORE UPDATE ON public.vehicles
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_drivers_updated_at
    BEFORE UPDATE ON public.drivers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_guides_updated_at
    BEFORE UPDATE ON public.guides
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_transfers_updated_at
    BEFORE UPDATE ON public.transfers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  VEHICLES  ----------
ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;

CREATE POLICY "vehicles_select" ON public.vehicles FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "vehicles_insert" ON public.vehicles FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "vehicles_update" ON public.vehicles FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "vehicles_delete" ON public.vehicles FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  DRIVERS  ----------
ALTER TABLE public.drivers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "drivers_select" ON public.drivers FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "drivers_insert" ON public.drivers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "drivers_update" ON public.drivers FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "drivers_delete" ON public.drivers FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  GUIDES  ----------
ALTER TABLE public.guides ENABLE ROW LEVEL SECURITY;

CREATE POLICY "guides_select" ON public.guides FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "guides_insert" ON public.guides FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "guides_update" ON public.guides FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "guides_delete" ON public.guides FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  TRANSFERS  ----------
ALTER TABLE public.transfers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "transfers_select" ON public.transfers FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "transfers_insert" ON public.transfers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "transfers_update" ON public.transfers FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "transfers_delete" ON public.transfers FOR DELETE
    USING (tenant_id = public.current_tenant_id());
