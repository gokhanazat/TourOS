-- ============================================================
-- TourOS CRM Tables Migration
-- customers, agencies → bookings ilişkisi
-- loyalty_points, customer_notes → customers ilişkisi
-- ============================================================

-- ===================  1. CUSTOMERS  =========================
CREATE TABLE IF NOT EXISTS public.customers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       TEXT NOT NULL,
    email           TEXT,
    phone           TEXT,
    tc_no           TEXT,
    passport_no     TEXT,
    birth_date      DATE,
    gender          TEXT,                             -- M | F | O
    address         TEXT,
    city            TEXT,
    country         TEXT NOT NULL DEFAULT 'TR',
    source          TEXT NOT NULL DEFAULT 'direct',   -- direct | agency | web | referral
    tags            TEXT[],                            -- VIP, corporate, repeat …
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tenant_id, email)
);

CREATE INDEX idx_customers_tenant ON public.customers(tenant_id);
CREATE INDEX idx_customers_phone  ON public.customers(phone);
CREATE INDEX idx_customers_city   ON public.customers(city);

-- ===================  2. AGENCIES  ==========================
CREATE TABLE IF NOT EXISTS public.agencies (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name                TEXT NOT NULL,
    contact_person      TEXT,
    email               TEXT,
    phone               TEXT,
    address             TEXT,
    city                TEXT,
    country             TEXT NOT NULL DEFAULT 'TR',
    tax_no              TEXT,
    commission_rate     NUMERIC(5,2) NOT NULL DEFAULT 0,  -- varsayılan komisyon oranı (%)
    balance             NUMERIC(14,2) NOT NULL DEFAULT 0, -- cari bakiye
    currency            TEXT NOT NULL DEFAULT 'TRY',
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID,

    UNIQUE (tenant_id, name),

    CONSTRAINT chk_agencies_commission_rate CHECK (commission_rate >= 0)
);

CREATE INDEX idx_agencies_tenant ON public.agencies(tenant_id);

-- ===================  3. BOOKINGS FK EKLEMELERİ  ============
-- customers ve agencies kolonlarını mevcut bookings tablosuna ekle

ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL;

ALTER TABLE public.bookings
    ADD COLUMN IF NOT EXISTS agency_id UUID REFERENCES public.agencies(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_bookings_customer ON public.bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_agency   ON public.bookings(agency_id);

-- ===================  4. LOYALTY_POINTS  ====================
CREATE TABLE IF NOT EXISTS public.loyalty_points (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES public.customers(id) ON DELETE CASCADE,
    booking_id      UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    points          INT NOT NULL DEFAULT 0,
    transaction_type TEXT NOT NULL DEFAULT 'earn',    -- earn | redeem | expire | adjust
    description     TEXT,
    expires_at      TIMESTAMPTZ,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_loyalty_points_tenant   ON public.loyalty_points(tenant_id);
CREATE INDEX idx_loyalty_points_customer ON public.loyalty_points(customer_id);
CREATE INDEX idx_loyalty_points_booking  ON public.loyalty_points(booking_id);

-- ===================  5. CUSTOMER_NOTES  ====================
CREATE TABLE IF NOT EXISTS public.customer_notes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES public.customers(id) ON DELETE CASCADE,
    note            TEXT NOT NULL,
    note_type       TEXT NOT NULL DEFAULT 'general',  -- general | complaint | request | feedback
    is_pinned       BOOLEAN NOT NULL DEFAULT FALSE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX idx_customer_notes_tenant   ON public.customer_notes(tenant_id);
CREATE INDEX idx_customer_notes_customer ON public.customer_notes(customer_id);

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================
CREATE TRIGGER trg_customers_updated_at
    BEFORE UPDATE ON public.customers
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_agencies_updated_at
    BEFORE UPDATE ON public.agencies
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_loyalty_points_updated_at
    BEFORE UPDATE ON public.loyalty_points
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_customer_notes_updated_at
    BEFORE UPDATE ON public.customer_notes
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  CUSTOMERS  ----------
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;

CREATE POLICY "customers_select" ON public.customers FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "customers_insert" ON public.customers FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "customers_update" ON public.customers FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "customers_delete" ON public.customers FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  AGENCIES  ----------
ALTER TABLE public.agencies ENABLE ROW LEVEL SECURITY;

CREATE POLICY "agencies_select" ON public.agencies FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "agencies_insert" ON public.agencies FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "agencies_update" ON public.agencies FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "agencies_delete" ON public.agencies FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  LOYALTY_POINTS  ----------
ALTER TABLE public.loyalty_points ENABLE ROW LEVEL SECURITY;

CREATE POLICY "loyalty_points_select" ON public.loyalty_points FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "loyalty_points_insert" ON public.loyalty_points FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "loyalty_points_update" ON public.loyalty_points FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "loyalty_points_delete" ON public.loyalty_points FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  CUSTOMER_NOTES  ----------
ALTER TABLE public.customer_notes ENABLE ROW LEVEL SECURITY;

CREATE POLICY "customer_notes_select" ON public.customer_notes FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "customer_notes_insert" ON public.customer_notes FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "customer_notes_update" ON public.customer_notes FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "customer_notes_delete" ON public.customer_notes FOR DELETE
    USING (tenant_id = public.current_tenant_id());
