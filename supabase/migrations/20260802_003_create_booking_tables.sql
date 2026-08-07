-- ============================================================
-- TourOS Booking Tables Migration
-- bookings → booking_items, passengers
-- ============================================================

-- ===================  1. BOOKINGS  ==========================
CREATE TABLE IF NOT EXISTS public.bookings (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_code      TEXT NOT NULL,               -- B-20260802-001 gibi unique kod
    departure_id      UUID NOT NULL REFERENCES public.departures(id) ON DELETE RESTRICT,
    customer_name     TEXT NOT NULL,
    customer_email    TEXT,
    customer_phone    TEXT,
    total_price       NUMERIC(12,2) NOT NULL DEFAULT 0,
    currency          TEXT NOT NULL DEFAULT 'TRY',
    pax_count         INT NOT NULL DEFAULT 1,
    status            TEXT NOT NULL DEFAULT 'Bekliyor',
    notes             TEXT,
    confirmed_at      TIMESTAMPTZ,
    cancelled_at      TIMESTAMPTZ,

    -- audit
    tenant_id         UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by        UUID,

    UNIQUE (tenant_id, booking_code),

    CONSTRAINT chk_bookings_status CHECK (
        status IN ('Bekliyor', 'Opsiyon', 'Onaylandı', 'İptal', 'Tamamlandı')
    )
);

CREATE INDEX idx_bookings_tenant    ON public.bookings(tenant_id);
CREATE INDEX idx_bookings_departure ON public.bookings(departure_id);
CREATE INDEX idx_bookings_status    ON public.bookings(status);
CREATE INDEX idx_bookings_code      ON public.bookings(booking_code);

-- ===================  2. BOOKING_ITEMS  =====================
CREATE TABLE IF NOT EXISTS public.booking_items (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    description     TEXT NOT NULL,                 -- oda tipi, ekstra servis vb.
    quantity        INT NOT NULL DEFAULT 1,
    unit_price      NUMERIC(12,2) NOT NULL DEFAULT 0,
    total_price     NUMERIC(12,2) NOT NULL DEFAULT 0,
    item_type       TEXT NOT NULL DEFAULT 'service', -- service | accommodation | transfer | extra
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_booking_items_tenant  ON public.booking_items(tenant_id);
CREATE INDEX idx_booking_items_booking ON public.booking_items(booking_id);

-- ===================  3. PASSENGERS  ========================
CREATE TABLE IF NOT EXISTS public.passengers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id      UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    full_name       TEXT NOT NULL,
    tc_no           TEXT,                          -- TC Kimlik No
    passport_no     TEXT,
    birth_date      DATE,
    gender          TEXT,                          -- M | F | O
    phone           TEXT,
    email           TEXT,
    is_lead         BOOLEAN NOT NULL DEFAULT FALSE, -- ana yolcu mu?
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_passengers_tenant  ON public.passengers(tenant_id);
CREATE INDEX idx_passengers_booking ON public.passengers(booking_id);

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================
CREATE TRIGGER trg_bookings_updated_at
    BEFORE UPDATE ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_booking_items_updated_at
    BEFORE UPDATE ON public.booking_items
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_passengers_updated_at
    BEFORE UPDATE ON public.passengers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  BOOKINGS  ----------
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "bookings_select" ON public.bookings FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "bookings_insert" ON public.bookings FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "bookings_update" ON public.bookings FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "bookings_delete" ON public.bookings FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  BOOKING_ITEMS  ----------
ALTER TABLE public.booking_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "booking_items_select" ON public.booking_items FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "booking_items_insert" ON public.booking_items FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "booking_items_update" ON public.booking_items FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "booking_items_delete" ON public.booking_items FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  PASSENGERS  ----------
ALTER TABLE public.passengers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "passengers_select" ON public.passengers FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "passengers_insert" ON public.passengers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "passengers_update" ON public.passengers FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "passengers_delete" ON public.passengers FOR DELETE
    USING (tenant_id = public.current_tenant_id());
