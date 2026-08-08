-- ============================================================
-- TourOS Migration: 20260808_005_tour_tables_and_relations.sql
-- Prompt 0.2.2: Tur Tabloları (tours, departures, itineraries, tour_categories)
-- Foreign key ilişkileri, indexler ve tenant_id bazlı audit kolonları.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. TOUR_CATEGORIES TABLOSU  ===========
CREATE TABLE IF NOT EXISTS public.tour_categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    slug        TEXT NOT NULL,
    description TEXT,
    icon_url    TEXT,
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    CONSTRAINT uq_tour_categories_tenant_slug UNIQUE (tenant_id, slug)
);

CREATE INDEX IF NOT EXISTS idx_tour_categories_tenant ON public.tour_categories(tenant_id);

-- ===================  2. TOURS TABLOSU  =====================
CREATE TABLE IF NOT EXISTS public.tours (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code                TEXT NOT NULL,
    title               TEXT NOT NULL,
    category            TEXT NOT NULL,
    country             TEXT NOT NULL,
    city                TEXT NOT NULL,
    category_id         UUID REFERENCES public.tour_categories(id) ON DELETE SET NULL,
    duration_days       INT NOT NULL DEFAULT 1,
    base_price          NUMERIC(12,2) NOT NULL DEFAULT 0.00,
    child_price_0_6     NUMERIC(12,2) DEFAULT 0.00,
    child_price_7_12    NUMERIC(12,2) DEFAULT 0.00,
    capacity            INT NOT NULL DEFAULT 20,
    min_participants    INT NOT NULL DEFAULT 1,
    max_participants    INT NOT NULL DEFAULT 30,
    description         TEXT,
    cancellation_policy TEXT,
    insurance_details   TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID,

    CONSTRAINT uq_tours_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX IF NOT EXISTS idx_tours_tenant ON public.tours(tenant_id);
CREATE INDEX IF NOT EXISTS idx_tours_code ON public.tours(code);
CREATE INDEX IF NOT EXISTS idx_tours_category ON public.tours(category);
CREATE INDEX IF NOT EXISTS idx_tours_is_active ON public.tours(tenant_id, is_active);

-- ===================  3. DEPARTURES TABLOSU  ================
-- tours -> departures ilişkisi (FK: tour_id ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS public.departures (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tour_id                 UUID NOT NULL REFERENCES public.tours(id) ON DELETE CASCADE,
    departure_date          DATE NOT NULL,
    return_date             DATE,
    price_override          NUMERIC(12,2),
    child_price_override    NUMERIC(12,2),
    infant_price_override   NUMERIC(12,2),
    currency                TEXT NOT NULL DEFAULT 'TRY',
    capacity                INT NOT NULL DEFAULT 20,
    booked_count            INT NOT NULL DEFAULT 0,
    option_deadline_days    INT DEFAULT 7,
    is_guaranteed           BOOLEAN NOT NULL DEFAULT FALSE,
    status                  TEXT NOT NULL DEFAULT 'PLANNED',
    notes                   TEXT,

    -- Audit & Multi-tenant
    tenant_id               UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by              UUID
);

CREATE INDEX IF NOT EXISTS idx_departures_tour ON public.departures(tour_id);
CREATE INDEX IF NOT EXISTS idx_departures_tenant ON public.departures(tenant_id);
CREATE INDEX IF NOT EXISTS idx_departures_dates ON public.departures(departure_date, return_date);

-- ===================  4. ITINERARIES TABLOSU  ===============
-- tours -> itineraries ilişkisi (FK: tour_id ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS public.itineraries (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tour_id     UUID NOT NULL REFERENCES public.tours(id) ON DELETE CASCADE,
    day_number  INT NOT NULL,
    title       TEXT NOT NULL,
    description TEXT,
    location    TEXT,
    start_time  TIME,
    end_time    TIME,
    sort_order  INT NOT NULL DEFAULT 0,

    -- Audit & Multi-tenant
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    CONSTRAINT uq_itineraries_tour_day UNIQUE (tour_id, day_number)
);

CREATE INDEX IF NOT EXISTS idx_itineraries_tour ON public.itineraries(tour_id);
CREATE INDEX IF NOT EXISTS idx_itineraries_tenant ON public.itineraries(tenant_id);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DROP TRIGGER IF EXISTS trg_tour_categories_updated_at ON public.tour_categories;
CREATE TRIGGER trg_tour_categories_updated_at
    BEFORE UPDATE ON public.tour_categories
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_tours_updated_at ON public.tours;
CREATE TRIGGER trg_tours_updated_at
    BEFORE UPDATE ON public.tours
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_departures_updated_at ON public.departures;
CREATE TRIGGER trg_departures_updated_at
    BEFORE UPDATE ON public.departures
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_itineraries_updated_at ON public.itineraries;
CREATE TRIGGER trg_itineraries_updated_at
    BEFORE UPDATE ON public.itineraries
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı RLS)
-- ============================================================
ALTER TABLE public.tour_categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tours ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.departures ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.itineraries ENABLE ROW LEVEL SECURITY;

-- TOUR_CATEGORIES RLS
DROP POLICY IF EXISTS "tour_categories_select" ON public.tour_categories;
DROP POLICY IF EXISTS "tour_categories_insert" ON public.tour_categories;
DROP POLICY IF EXISTS "tour_categories_update" ON public.tour_categories;
DROP POLICY IF EXISTS "tour_categories_delete" ON public.tour_categories;

CREATE POLICY "tour_categories_select" ON public.tour_categories FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "tour_categories_insert" ON public.tour_categories FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "tour_categories_update" ON public.tour_categories FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "tour_categories_delete" ON public.tour_categories FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- TOURS RLS
DROP POLICY IF EXISTS "tours_select" ON public.tours;
DROP POLICY IF EXISTS "tours_insert" ON public.tours;
DROP POLICY IF EXISTS "tours_update" ON public.tours;
DROP POLICY IF EXISTS "tours_delete" ON public.tours;

CREATE POLICY "tours_select" ON public.tours FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "tours_insert" ON public.tours FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "tours_update" ON public.tours FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "tours_delete" ON public.tours FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- DEPARTURES RLS
DROP POLICY IF EXISTS "departures_select" ON public.departures;
DROP POLICY IF EXISTS "departures_insert" ON public.departures;
DROP POLICY IF EXISTS "departures_update" ON public.departures;
DROP POLICY IF EXISTS "departures_delete" ON public.departures;

CREATE POLICY "departures_select" ON public.departures FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "departures_insert" ON public.departures FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "departures_update" ON public.departures FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "departures_delete" ON public.departures FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ITINERARIES RLS
DROP POLICY IF EXISTS "itineraries_select" ON public.itineraries;
DROP POLICY IF EXISTS "itineraries_insert" ON public.itineraries;
DROP POLICY IF EXISTS "itineraries_update" ON public.itineraries;
DROP POLICY IF EXISTS "itineraries_delete" ON public.itineraries;

CREATE POLICY "itineraries_select" ON public.itineraries FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "itineraries_insert" ON public.itineraries FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "itineraries_update" ON public.itineraries FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "itineraries_delete" ON public.itineraries FOR DELETE
    USING (tenant_id = public.current_tenant_id());
