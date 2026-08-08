-- ============================================================
-- TourOS Migration: 20260808_010_crm_tables_and_relations.sql
-- Prompt 0.2.7: CRM Tabloları (customers, agencies, loyalty_points, customer_notes)
-- customers -> bookings ve agencies -> bookings FK ilişkileri ve RLS.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. CUSTOMERS TABLOSU  =================
CREATE TABLE IF NOT EXISTS public.customers (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name      TEXT NOT NULL,
    last_name       TEXT NOT NULL,
    email           TEXT,
    phone           TEXT,
    id_number       TEXT, -- TC / Pasaport
    address         TEXT,
    city            TEXT,
    country         TEXT DEFAULT 'Türkiye',
    segment         TEXT DEFAULT 'REGULAR', -- REGULAR, VIP, CORPORATE
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS first_name TEXT NOT NULL DEFAULT '';
ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS last_name TEXT NOT NULL DEFAULT '';
ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS email TEXT;
ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS phone TEXT;
ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS id_number TEXT;
ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS segment TEXT DEFAULT 'REGULAR';
ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE public.customers ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_customers_tenant ON public.customers(tenant_id);
CREATE INDEX IF NOT EXISTS idx_customers_email ON public.customers(email);

-- ===================  2. AGENCIES TABLOSU  ==================
CREATE TABLE IF NOT EXISTS public.agencies (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agency_name     TEXT NOT NULL,
    code            TEXT NOT NULL,
    contact_person  TEXT,
    email           TEXT,
    phone           TEXT,
    commission_rate NUMERIC(5,2) DEFAULT 10.00,
    credit_limit    NUMERIC(12,2) DEFAULT 0.00,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.agencies ADD COLUMN IF NOT EXISTS agency_name TEXT NOT NULL DEFAULT '';
ALTER TABLE public.agencies ADD COLUMN IF NOT EXISTS code TEXT NOT NULL DEFAULT '';
ALTER TABLE public.agencies ADD COLUMN IF NOT EXISTS commission_rate NUMERIC(5,2) DEFAULT 10.00;
ALTER TABLE public.agencies ADD COLUMN IF NOT EXISTS credit_limit NUMERIC(12,2) DEFAULT 0.00;
ALTER TABLE public.agencies ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE public.agencies ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_agencies_tenant ON public.agencies(tenant_id);
CREATE INDEX IF NOT EXISTS idx_agencies_code ON public.agencies(code);

-- ===================  3. BOOKINGS -> CUSTOMERS & AGENCIES  ==
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS customer_id UUID REFERENCES public.customers(id) ON DELETE SET NULL;
ALTER TABLE public.bookings ADD COLUMN IF NOT EXISTS agency_id UUID REFERENCES public.agencies(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_bookings_customer ON public.bookings(customer_id);
CREATE INDEX IF NOT EXISTS idx_bookings_agency ON public.bookings(agency_id);

-- ===================  4. LOYALTY_POINTS TABLOSU  ===========
CREATE TABLE IF NOT EXISTS public.loyalty_points (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES public.customers(id) ON DELETE CASCADE,
    points          INT NOT NULL DEFAULT 0,
    earned_from     TEXT DEFAULT 'BOOKING', -- BOOKING, PROMOTION, REFERRAL
    booking_id      UUID REFERENCES public.bookings(id) ON DELETE SET NULL,
    expiry_date     DATE,

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.loyalty_points ADD COLUMN IF NOT EXISTS customer_id UUID REFERENCES public.customers(id) ON DELETE CASCADE;
ALTER TABLE public.loyalty_points ADD COLUMN IF NOT EXISTS points INT NOT NULL DEFAULT 0;
ALTER TABLE public.loyalty_points ADD COLUMN IF NOT EXISTS booking_id UUID REFERENCES public.bookings(id) ON DELETE SET NULL;
ALTER TABLE public.loyalty_points ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_loyalty_points_customer ON public.loyalty_points(customer_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_points_tenant ON public.loyalty_points(tenant_id);

-- ===================  5. CUSTOMER_NOTES TABLOSU  ===========
CREATE TABLE IF NOT EXISTS public.customer_notes (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id     UUID NOT NULL REFERENCES public.customers(id) ON DELETE CASCADE,
    note            TEXT NOT NULL,
    category        TEXT DEFAULT 'GENERAL', -- GENERAL, PREFERENCE, COMPLAINT, SPECIAL_REQUEST

    -- Audit & Multi-tenant
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

ALTER TABLE public.customer_notes ADD COLUMN IF NOT EXISTS customer_id UUID REFERENCES public.customers(id) ON DELETE CASCADE;
ALTER TABLE public.customer_notes ADD COLUMN IF NOT EXISTS note TEXT NOT NULL DEFAULT '';
ALTER TABLE public.customer_notes ADD COLUMN IF NOT EXISTS category TEXT DEFAULT 'GENERAL';
ALTER TABLE public.customer_notes ADD COLUMN IF NOT EXISTS tenant_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'::uuid;

CREATE INDEX IF NOT EXISTS idx_customer_notes_customer ON public.customer_notes(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_notes_tenant ON public.customer_notes(tenant_id);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DO $$ BEGIN
    DROP TRIGGER IF EXISTS trg_customers_updated_at ON public.customers;
    CREATE TRIGGER trg_customers_updated_at BEFORE UPDATE ON public.customers FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_agencies_updated_at ON public.agencies;
    CREATE TRIGGER trg_agencies_updated_at BEFORE UPDATE ON public.agencies FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_loyalty_points_updated_at ON public.loyalty_points;
    CREATE TRIGGER trg_loyalty_points_updated_at BEFORE UPDATE ON public.loyalty_points FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

    DROP TRIGGER IF EXISTS trg_customer_notes_updated_at ON public.customer_notes;
    CREATE TRIGGER trg_customer_notes_updated_at BEFORE UPDATE ON public.customer_notes FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
END $$;

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı RLS)
-- ============================================================
ALTER TABLE public.customers ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.agencies ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.loyalty_points ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.customer_notes ENABLE ROW LEVEL SECURITY;

-- CUSTOMERS RLS
DROP POLICY IF EXISTS "customers_select" ON public.customers;
DROP POLICY IF EXISTS "customers_insert" ON public.customers;
DROP POLICY IF EXISTS "customers_update" ON public.customers;
DROP POLICY IF EXISTS "customers_delete" ON public.customers;

CREATE POLICY "customers_select" ON public.customers FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "customers_insert" ON public.customers FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "customers_update" ON public.customers FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "customers_delete" ON public.customers FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- AGENCIES RLS
DROP POLICY IF EXISTS "agencies_select" ON public.agencies;
DROP POLICY IF EXISTS "agencies_insert" ON public.agencies;
DROP POLICY IF EXISTS "agencies_update" ON public.agencies;
DROP POLICY IF EXISTS "agencies_delete" ON public.agencies;

CREATE POLICY "agencies_select" ON public.agencies FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "agencies_insert" ON public.agencies FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "agencies_update" ON public.agencies FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "agencies_delete" ON public.agencies FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- LOYALTY_POINTS RLS
DROP POLICY IF EXISTS "loyalty_points_select" ON public.loyalty_points;
DROP POLICY IF EXISTS "loyalty_points_insert" ON public.loyalty_points;
DROP POLICY IF EXISTS "loyalty_points_update" ON public.loyalty_points;
DROP POLICY IF EXISTS "loyalty_points_delete" ON public.loyalty_points;

CREATE POLICY "loyalty_points_select" ON public.loyalty_points FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "loyalty_points_insert" ON public.loyalty_points FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "loyalty_points_update" ON public.loyalty_points FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "loyalty_points_delete" ON public.loyalty_points FOR DELETE USING (public.is_valid_tenant(tenant_id));

-- CUSTOMER_NOTES RLS
DROP POLICY IF EXISTS "customer_notes_select" ON public.customer_notes;
DROP POLICY IF EXISTS "customer_notes_insert" ON public.customer_notes;
DROP POLICY IF EXISTS "customer_notes_update" ON public.customer_notes;
DROP POLICY IF EXISTS "customer_notes_delete" ON public.customer_notes;

CREATE POLICY "customer_notes_select" ON public.customer_notes FOR SELECT USING (public.is_valid_tenant(tenant_id));
CREATE POLICY "customer_notes_insert" ON public.customer_notes FOR INSERT WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "customer_notes_update" ON public.customer_notes FOR UPDATE USING (public.is_valid_tenant(tenant_id)) WITH CHECK (public.is_valid_tenant(tenant_id));
CREATE POLICY "customer_notes_delete" ON public.customer_notes FOR DELETE USING (public.is_valid_tenant(tenant_id));
