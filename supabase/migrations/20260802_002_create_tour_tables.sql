-- ============================================================
-- TourOS Tour Tables Migration
-- tour_categories → tours → departures, itineraries
-- Tüm tablolar tenant_id bazlı RLS ile izole edilir.
-- ============================================================

-- ===================  1. TOUR_CATEGORIES  ===================
CREATE TABLE IF NOT EXISTS public.tour_categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    slug        TEXT NOT NULL,
    description TEXT,
    icon_url    TEXT,
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID,

    UNIQUE (tenant_id, slug)
);

CREATE INDEX idx_tour_categories_tenant ON public.tour_categories(tenant_id);

-- ===================  2. TOURS  =============================
CREATE TABLE IF NOT EXISTS public.tours (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id     UUID REFERENCES public.tour_categories(id) ON DELETE SET NULL,
    title           TEXT NOT NULL,
    slug            TEXT NOT NULL,
    description     TEXT,
    cover_image_url TEXT,
    duration_days   INT NOT NULL DEFAULT 1,
    base_price      NUMERIC(12,2) NOT NULL DEFAULT 0,
    currency        TEXT NOT NULL DEFAULT 'TRY',
    max_capacity    INT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tenant_id, slug)
);

CREATE INDEX idx_tours_tenant   ON public.tours(tenant_id);
CREATE INDEX idx_tours_category ON public.tours(category_id);

-- ===================  3. DEPARTURES  ========================
CREATE TABLE IF NOT EXISTS public.departures (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tour_id         UUID NOT NULL REFERENCES public.tours(id) ON DELETE CASCADE,
    departure_date  DATE NOT NULL,
    return_date     DATE,
    price_override  NUMERIC(12,2),            -- NULL ise tours.base_price geçerli
    capacity        INT,                      -- NULL ise tours.max_capacity geçerli
    booked_count    INT NOT NULL DEFAULT 0,
    status          TEXT NOT NULL DEFAULT 'planned',  -- planned | confirmed | cancelled | completed
    notes           TEXT,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tour_id, departure_date)
);

CREATE INDEX idx_departures_tenant ON public.departures(tenant_id);
CREATE INDEX idx_departures_tour   ON public.departures(tour_id);
CREATE INDEX idx_departures_date   ON public.departures(departure_date);
CREATE INDEX idx_departures_status ON public.departures(status);

-- ===================  4. ITINERARIES  =======================
CREATE TABLE IF NOT EXISTS public.itineraries (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    tour_id         UUID NOT NULL REFERENCES public.tours(id) ON DELETE CASCADE,
    day_number      INT NOT NULL,
    title           TEXT NOT NULL,
    description     TEXT,
    location        TEXT,
    start_time      TIME,
    end_time        TIME,
    sort_order      INT NOT NULL DEFAULT 0,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tour_id, day_number, sort_order)
);

CREATE INDEX idx_itineraries_tenant ON public.itineraries(tenant_id);
CREATE INDEX idx_itineraries_tour   ON public.itineraries(tour_id);

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================
CREATE TRIGGER trg_tour_categories_updated_at
    BEFORE UPDATE ON public.tour_categories
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_tours_updated_at
    BEFORE UPDATE ON public.tours
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_departures_updated_at
    BEFORE UPDATE ON public.departures
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_itineraries_updated_at
    BEFORE UPDATE ON public.itineraries
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  TOUR_CATEGORIES  ----------
ALTER TABLE public.tour_categories ENABLE ROW LEVEL SECURITY;

CREATE POLICY "tour_categories_select" ON public.tour_categories FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "tour_categories_insert" ON public.tour_categories FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tour_categories_update" ON public.tour_categories FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tour_categories_delete" ON public.tour_categories FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  TOURS  ----------
ALTER TABLE public.tours ENABLE ROW LEVEL SECURITY;

CREATE POLICY "tours_select" ON public.tours FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "tours_insert" ON public.tours FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tours_update" ON public.tours FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "tours_delete" ON public.tours FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  DEPARTURES  ----------
ALTER TABLE public.departures ENABLE ROW LEVEL SECURITY;

CREATE POLICY "departures_select" ON public.departures FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "departures_insert" ON public.departures FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "departures_update" ON public.departures FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "departures_delete" ON public.departures FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  ITINERARIES  ----------
ALTER TABLE public.itineraries ENABLE ROW LEVEL SECURITY;

CREATE POLICY "itineraries_select" ON public.itineraries FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "itineraries_insert" ON public.itineraries FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "itineraries_update" ON public.itineraries FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "itineraries_delete" ON public.itineraries FOR DELETE
    USING (tenant_id = public.current_tenant_id());
