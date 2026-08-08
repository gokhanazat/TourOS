-- ============================================================
-- TourOS Migration: 20260808_006_booking_tables_and_relations.sql
-- Prompt 0.2.3: Rezervasyon Tabloları (bookings, booking_items, passengers)
-- Status CHECK constraint, FK ilişkileri (booking_items -> bookings, passengers -> bookings) ve RLS.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. BOOKINGS TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.bookings (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_number  TEXT NOT NULL,
    departure_id    UUID REFERENCES public.departures(id) ON DELETE SET NULL,
    tour_id         UUID REFERENCES public.tours(id) ON DELETE SET NULL,
    customer_name   TEXT NOT NULL,
    customer_email  TEXT,
    customer_phone  TEXT,
    total_amount    NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    paid_amount     NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    currency        TEXT NOT NULL DEFAULT 'TRY',
    status          TEXT NOT NULL DEFAULT 'PENDING',
    notes           TEXT,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    CONSTRAINT uq_bookings_tenant_number UNIQUE (tenant_id, booking_number),
    CONSTRAINT chk_bookings_status CHECK (
        status IN (
            'PENDING', 'OPTION', 'CONFIRMED', 'CANCELLED', 'COMPLETED',
            'Bekliyor', 'Opsiyon', 'Onaylandı', 'İptal', 'Tamamlandı'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_bookings_tenant ON public.bookings(tenant_id);
CREATE INDEX IF NOT EXISTS idx_bookings_status ON public.bookings(status);
CREATE INDEX IF NOT EXISTS idx_bookings_departure ON public.bookings(departure_id);

-- ===================  2. BOOKING_ITEMS TABLOSU  ============
-- booking_items -> bookings ilişkisi (FK: booking_id ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS public.booking_items (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id  UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    item_type   TEXT NOT NULL DEFAULT 'TOUR', -- TOUR, HOTEL, TRANSFER, EXTRA
    title       TEXT NOT NULL,
    quantity    INT NOT NULL DEFAULT 1,
    unit_price  NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    total_price NUMERIC(12,2) NOT NULL DEFAULT 0.00,

    -- Audit & Multi-tenant
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID
);

CREATE INDEX IF NOT EXISTS idx_booking_items_booking ON public.booking_items(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_items_tenant ON public.booking_items(tenant_id);

-- ===================  3. PASSENGERS TABLOSU  ==============
-- passengers -> bookings ilişkisi (FK: booking_id ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS public.passengers (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    booking_id          UUID NOT NULL REFERENCES public.bookings(id) ON DELETE CASCADE,
    first_name          TEXT NOT NULL,
    last_name           TEXT NOT NULL,
    id_number           TEXT, -- TC Kimlik No / Pasaport No
    birth_date          DATE,
    gender              TEXT,
    passenger_type      TEXT NOT NULL DEFAULT 'ADULT', -- ADULT, CHILD, INFANT
    room_number         TEXT,
    special_requests    TEXT,

    -- Audit & Multi-tenant
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID
);

CREATE INDEX IF NOT EXISTS idx_passengers_booking ON public.passengers(booking_id);
CREATE INDEX IF NOT EXISTS idx_passengers_tenant ON public.passengers(tenant_id);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DROP TRIGGER IF EXISTS trg_bookings_updated_at ON public.bookings;
CREATE TRIGGER trg_bookings_updated_at
    BEFORE UPDATE ON public.bookings
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_booking_items_updated_at ON public.booking_items;
CREATE TRIGGER trg_booking_items_updated_at
    BEFORE UPDATE ON public.booking_items
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_passengers_updated_at ON public.passengers;
CREATE TRIGGER trg_passengers_updated_at
    BEFORE UPDATE ON public.passengers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı RLS)
-- ============================================================
ALTER TABLE public.bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.booking_items ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.passengers ENABLE ROW LEVEL SECURITY;

-- BOOKINGS RLS
DROP POLICY IF EXISTS "bookings_select" ON public.bookings;
DROP POLICY IF EXISTS "bookings_insert" ON public.bookings;
DROP POLICY IF EXISTS "bookings_update" ON public.bookings;
DROP POLICY IF EXISTS "bookings_delete" ON public.bookings;

CREATE POLICY "bookings_select" ON public.bookings FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "bookings_insert" ON public.bookings FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "bookings_update" ON public.bookings FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "bookings_delete" ON public.bookings FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- BOOKING_ITEMS RLS
DROP POLICY IF EXISTS "booking_items_select" ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_insert" ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_update" ON public.booking_items;
DROP POLICY IF EXISTS "booking_items_delete" ON public.booking_items;

CREATE POLICY "booking_items_select" ON public.booking_items FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "booking_items_insert" ON public.booking_items FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "booking_items_update" ON public.booking_items FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "booking_items_delete" ON public.booking_items FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- PASSENGERS RLS
DROP POLICY IF EXISTS "passengers_select" ON public.passengers;
DROP POLICY IF EXISTS "passengers_insert" ON public.passengers;
DROP POLICY IF EXISTS "passengers_update" ON public.passengers;
DROP POLICY IF EXISTS "passengers_delete" ON public.passengers;

CREATE POLICY "passengers_select" ON public.passengers FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "passengers_insert" ON public.passengers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "passengers_update" ON public.passengers FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "passengers_delete" ON public.passengers FOR DELETE
    USING (tenant_id = public.current_tenant_id());
