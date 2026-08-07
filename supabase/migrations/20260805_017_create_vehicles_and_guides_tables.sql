-- ============================================================
-- TourOS 2.1.2 Vehicles & Guides Infrastructure Migration
-- ============================================================

-- 1. VEHICLES (ARAÇLAR) TABLOSU
CREATE TABLE IF NOT EXISTS public.vehicles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plate_number    TEXT NOT NULL,
    model_name      TEXT NOT NULL,
    capacity        INT NOT NULL DEFAULT 46,
    driver_name     TEXT,
    driver_phone    TEXT,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID,
    UNIQUE (tenant_id, plate_number)
);

CREATE INDEX IF NOT EXISTS idx_vehicles_tenant ON public.vehicles(tenant_id);

-- 2. GUIDES (REHBERLER) TABLOSU
CREATE TABLE IF NOT EXISTS public.guides (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    full_name       TEXT NOT NULL,
    phone           TEXT,
    email           TEXT,
    languages       TEXT[] DEFAULT ARRAY['Türkçe'],
    status          TEXT NOT NULL DEFAULT 'Müsait', -- 'Müsait', 'Görevde', 'İzinli'
    badge_number    TEXT,
    tenant_id       UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_by      UUID
);

CREATE INDEX IF NOT EXISTS idx_guides_tenant ON public.guides(tenant_id);
CREATE INDEX IF NOT EXISTS idx_guides_status ON public.guides(tenant_id, status);

-- RLS ETKİNLEŞTİRME
ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.guides ENABLE ROW LEVEL SECURITY;
