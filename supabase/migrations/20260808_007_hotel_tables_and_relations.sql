-- ============================================================
-- TourOS Migration: 20260808_007_hotel_tables_and_relations.sql
-- Prompt 0.2.4: Konaklama Tabloları (hotels, hotel_contracts, room_types)
-- room_types -> hotels FK, Kontenjan kolonları ve RLS.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================  1. HOTELS TABLOSU  ===================
CREATE TABLE IF NOT EXISTS public.hotels (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    city        TEXT NOT NULL,
    country     TEXT NOT NULL DEFAULT 'Türkiye',
    address     TEXT,
    star_rating INT DEFAULT 4,
    phone       TEXT,
    email       TEXT,
    board_type  TEXT DEFAULT 'HB', -- BB, HB, FB, AI, UAI
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,

    -- Audit & Multi-tenant
    tenant_id   UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by  UUID
);

CREATE INDEX IF NOT EXISTS idx_hotels_tenant ON public.hotels(tenant_id);

-- ===================  2. HOTEL_CONTRACTS TABLOSU  ===========
-- hotel_contracts -> hotels ilişkisi (FK: hotel_id ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS public.hotel_contracts (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id            UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    contract_name       TEXT NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    currency            TEXT NOT NULL DEFAULT 'TRY',
    cancellation_days   INT DEFAULT 3,
    notes               TEXT,

    -- Audit & Multi-tenant
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID
);

CREATE INDEX IF NOT EXISTS idx_hotel_contracts_hotel ON public.hotel_contracts(hotel_id);
CREATE INDEX IF NOT EXISTS idx_hotel_contracts_tenant ON public.hotel_contracts(tenant_id);

-- ===================  3. ROOM_TYPES TABLOSU  ===============
-- room_types -> hotels ilişkisi (FK: hotel_id ON DELETE CASCADE) + Kontenjan Kolonları
CREATE TABLE IF NOT EXISTS public.room_types (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    hotel_id            UUID NOT NULL REFERENCES public.hotels(id) ON DELETE CASCADE,
    name                TEXT NOT NULL, -- Standard, Deluxe, Suite, Triple, Family
    code                TEXT,
    max_occupancy       INT NOT NULL DEFAULT 2,
    base_price          NUMERIC(12,2) DEFAULT 0.00,
    allotment           INT NOT NULL DEFAULT 10,       -- Toplam Kontenjan
    booked_allotment    INT NOT NULL DEFAULT 0,        -- Rezerve Edilen Kontenjan
    available_allotment INT NOT NULL DEFAULT 10,       -- Kalan Kontenjan

    -- Audit & Multi-tenant
    tenant_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by          UUID
);

CREATE INDEX IF NOT EXISTS idx_room_types_hotel ON public.room_types(hotel_id);
CREATE INDEX IF NOT EXISTS idx_room_types_tenant ON public.room_types(tenant_id);

-- ============================================================
-- TRIGGER: Automatic updated_at
-- ============================================================
DROP TRIGGER IF EXISTS trg_hotels_updated_at ON public.hotels;
CREATE TRIGGER trg_hotels_updated_at
    BEFORE UPDATE ON public.hotels
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_hotel_contracts_updated_at ON public.hotel_contracts;
CREATE TRIGGER trg_hotel_contracts_updated_at
    BEFORE UPDATE ON public.hotel_contracts
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

DROP TRIGGER IF EXISTS trg_room_types_updated_at ON public.room_types;
CREATE TRIGGER trg_room_types_updated_at
    BEFORE UPDATE ON public.room_types
    FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();

-- ============================================================
-- ROW LEVEL SECURITY (tenant_id bazlı RLS)
-- ============================================================
ALTER TABLE public.hotels ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.hotel_contracts ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.room_types ENABLE ROW LEVEL SECURITY;

-- HOTELS RLS
DROP POLICY IF EXISTS "hotels_select" ON public.hotels;
DROP POLICY IF EXISTS "hotels_insert" ON public.hotels;
DROP POLICY IF EXISTS "hotels_update" ON public.hotels;
DROP POLICY IF EXISTS "hotels_delete" ON public.hotels;

CREATE POLICY "hotels_select" ON public.hotels FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "hotels_insert" ON public.hotels FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "hotels_update" ON public.hotels FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "hotels_delete" ON public.hotels FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- HOTEL_CONTRACTS RLS
DROP POLICY IF EXISTS "hotel_contracts_select" ON public.hotel_contracts;
DROP POLICY IF EXISTS "hotel_contracts_insert" ON public.hotel_contracts;
DROP POLICY IF EXISTS "hotel_contracts_update" ON public.hotel_contracts;
DROP POLICY IF EXISTS "hotel_contracts_delete" ON public.hotel_contracts;

CREATE POLICY "hotel_contracts_select" ON public.hotel_contracts FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "hotel_contracts_insert" ON public.hotel_contracts FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "hotel_contracts_update" ON public.hotel_contracts FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "hotel_contracts_delete" ON public.hotel_contracts FOR DELETE
    USING (tenant_id = public.current_tenant_id());

-- ROOM_TYPES RLS
DROP POLICY IF EXISTS "room_types_select" ON public.room_types;
DROP POLICY IF EXISTS "room_types_insert" ON public.room_types;
DROP POLICY IF EXISTS "room_types_update" ON public.room_types;
DROP POLICY IF EXISTS "room_types_delete" ON public.room_types;

CREATE POLICY "room_types_select" ON public.room_types FOR SELECT
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "room_types_insert" ON public.room_types FOR INSERT
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "room_types_update" ON public.room_types FOR UPDATE
    USING (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001')
    WITH CHECK (tenant_id = public.current_tenant_id() OR tenant_id = '00000000-0000-0000-0000-000000000001');

CREATE POLICY "room_types_delete" ON public.room_types FOR DELETE
    USING (tenant_id = public.current_tenant_id());
