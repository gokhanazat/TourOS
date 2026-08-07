-- ============================================================
-- TourOS Hotel Tables Migration
-- hotels → room_types, hotel_contracts
-- ============================================================

-- ===================  1. HOTELS  ============================
CREATE TABLE IF NOT EXISTS public.hotels (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            TEXT NOT NULL,
    slug            TEXT NOT NULL,
    star_rating     INT,                          -- 1-5 yıldız
    address         TEXT,
    city            TEXT,
    country         TEXT NOT NULL DEFAULT 'TR',
    phone           TEXT,
    email           TEXT,
    website         TEXT,
    cover_image_url TEXT,
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,

    UNIQUE (tenant_id, slug)
);

CREATE INDEX idx_hotels_tenant ON public.hotels(tenant_id);
CREATE INDEX idx_hotels_city   ON public.hotels(city);

-- ===================  2. ROOM_TYPES  ========================
CREATE TABLE IF NOT EXISTS public.room_types (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id            UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    name                TEXT NOT NULL,                 -- Standard, Deluxe, Suite …
    description         TEXT,
    base_price_per_night NUMERIC(12,2) NOT NULL DEFAULT 0,
    currency            TEXT NOT NULL DEFAULT 'TRY',
    max_occupancy       INT NOT NULL DEFAULT 2,
    total_rooms         INT NOT NULL DEFAULT 0,       -- oteldeki toplam oda sayısı
    allotment           INT NOT NULL DEFAULT 0,       -- kontenjan (ayrılan oda)
    booked_rooms        INT NOT NULL DEFAULT 0,       -- dolu oda
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID,

    UNIQUE (hotel_id, name)
);

CREATE INDEX idx_room_types_tenant ON public.room_types(tenant_id);
CREATE INDEX idx_room_types_hotel  ON public.room_types(hotel_id);

-- ===================  3. HOTEL_CONTRACTS  ===================
CREATE TABLE IF NOT EXISTS public.hotel_contracts (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id            UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    room_type_id        UUID REFERENCES public.room_types(id) ON DELETE SET NULL,
    season_name         TEXT NOT NULL,                 -- Yaz 2026, Kış 2026 …
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    price_per_night     NUMERIC(12,2) NOT NULL,
    currency            TEXT NOT NULL DEFAULT 'TRY',
    allotment           INT NOT NULL DEFAULT 0,       -- kontrat kontenjanı
    release_days        INT NOT NULL DEFAULT 7,       -- kaç gün önce serbest bırakılır
    meal_plan           TEXT NOT NULL DEFAULT 'BB',    -- BB | HB | FB | AI | RO
    notes               TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,

    -- audit
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID,

    CONSTRAINT chk_contract_dates CHECK (end_date > start_date)
);

CREATE INDEX idx_hotel_contracts_tenant    ON public.hotel_contracts(tenant_id);
CREATE INDEX idx_hotel_contracts_hotel     ON public.hotel_contracts(hotel_id);
CREATE INDEX idx_hotel_contracts_room_type ON public.hotel_contracts(room_type_id);
CREATE INDEX idx_hotel_contracts_dates     ON public.hotel_contracts(start_date, end_date);

-- ============================================================
-- UPDATED_AT TRIGGERS
-- ============================================================
CREATE TRIGGER trg_hotels_updated_at
    BEFORE UPDATE ON public.hotels
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_room_types_updated_at
    BEFORE UPDATE ON public.room_types
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

CREATE TRIGGER trg_hotel_contracts_updated_at
    BEFORE UPDATE ON public.hotel_contracts
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY  (tenant_id bazlı izolasyon)
-- ============================================================

-- ----------  HOTELS  ----------
ALTER TABLE public.hotels ENABLE ROW LEVEL SECURITY;

CREATE POLICY "hotels_select" ON public.hotels FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "hotels_insert" ON public.hotels FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotels_update" ON public.hotels FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotels_delete" ON public.hotels FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  ROOM_TYPES  ----------
ALTER TABLE public.room_types ENABLE ROW LEVEL SECURITY;

CREATE POLICY "room_types_select" ON public.room_types FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "room_types_insert" ON public.room_types FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "room_types_update" ON public.room_types FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "room_types_delete" ON public.room_types FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ----------  HOTEL_CONTRACTS  ----------
ALTER TABLE public.hotel_contracts ENABLE ROW LEVEL SECURITY;

CREATE POLICY "hotel_contracts_select" ON public.hotel_contracts FOR SELECT
    USING (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_contracts_insert" ON public.hotel_contracts FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_contracts_update" ON public.hotel_contracts FOR UPDATE
    USING (tenant_id = public.current_tenant_id())
    WITH CHECK (tenant_id = public.current_tenant_id());
CREATE POLICY "hotel_contracts_delete" ON public.hotel_contracts FOR DELETE
    USING (tenant_id = public.current_tenant_id());
