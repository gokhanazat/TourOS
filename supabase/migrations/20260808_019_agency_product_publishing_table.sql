-- ============================================================
-- TourOS Migration: 20260808_019_agency_product_publishing_table.sql
-- Prompt 4.6.7: Ürün Seçimi / Yayınlama Ekranı Veri Tablosu ve RPC'ler
-- Acentenin operatör turlarını kendi kanalında yayınlayıp gizleme ve fiyat override tercihleri.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. AGENCY_PUBLISHED_TOURS TABLOSU
CREATE TABLE IF NOT EXISTS public.agency_published_tours (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agency_id               UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    tour_id                 UUID NOT NULL REFERENCES public.tours(id) ON DELETE CASCADE,
    is_published            BOOLEAN NOT NULL DEFAULT true,
    custom_price_override   NUMERIC(12,2),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_agency_tour_published UNIQUE (agency_id, tour_id)
);

CREATE INDEX IF NOT EXISTS idx_agency_published_agency ON public.agency_published_tours(agency_id);
CREATE INDEX IF NOT EXISTS idx_agency_published_tour ON public.agency_published_tours(tour_id);

-- RLS
ALTER TABLE public.agency_published_tours ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "agency_published_tours_all" ON public.agency_published_tours;
CREATE POLICY "agency_published_tours_all" ON public.agency_published_tours
    USING (agency_id = public.current_tenant_id() OR public.is_valid_tenant(agency_id))
    WITH CHECK (agency_id = public.current_tenant_id() OR public.is_valid_tenant(agency_id));

-- 2. Acentenin Bağlı Kataloğunu Listeleyen RPC (Yayın Durumu ve Fiyat Önizlemesi ile)
CREATE OR REPLACE FUNCTION public.get_agency_catalog_tours(p_agency_id UUID)
RETURNS TABLE (
    id UUID,
    agency_id UUID,
    tour_id UUID,
    tour_title TEXT,
    tour_code TEXT,
    operator_name TEXT,
    base_price NUMERIC,
    calculated_price NUMERIC,
    is_published BOOLEAN,
    custom_price_override NUMERIC,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COALESCE(apt.id, uuid_generate_v4()) AS id,
        p_agency_id AS agency_id,
        t.id AS tour_id,
        t.title AS tour_title,
        t.code AS tour_code,
        comp.name AS operator_name,
        COALESCE(t.base_price, 0.0) AS base_price,
        COALESCE(apt.custom_price_override, 
            CASE 
                WHEN conn.price_adjustment_type = 'percentage' THEN 
                    COALESCE(t.base_price, 0.0) * (1 + (conn.price_adjustment_value / 100.0))
                ELSE 
                    COALESCE(t.base_price, 0.0) + conn.price_adjustment_value
            END
        ) AS calculated_price,
        COALESCE(apt.is_published, true) AS is_published,
        apt.custom_price_override,
        COALESCE(apt.created_at, now()) AS created_at
    FROM public.tours t
    JOIN public.companies comp ON comp.id = t.tenant_id
    JOIN public.agency_operator_connections conn ON conn.operator_company_id = t.tenant_id
    LEFT JOIN public.agency_published_tours apt ON apt.agency_id = p_agency_id AND apt.tour_id = t.id
    WHERE conn.agency_id = p_agency_id
      AND conn.status = 'ACTIVE'
      AND t.is_active = true;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 3. Ürün Yayınlama ve Fiyat Override Güncelleme RPC
CREATE OR REPLACE FUNCTION public.toggle_agency_tour_publishing(
    p_agency_id UUID,
    p_tour_id UUID,
    p_is_published BOOLEAN,
    p_custom_price NUMERIC DEFAULT NULL
) RETURNS VOID AS $$
BEGIN
    INSERT INTO public.agency_published_tours (
        agency_id,
        tour_id,
        is_published,
        custom_price_override,
        updated_at
    ) VALUES (
        p_agency_id,
        p_tour_id,
        p_is_published,
        p_custom_price,
        now()
    )
    ON CONFLICT (agency_id, tour_id)
    DO UPDATE SET 
        is_published = EXCLUDED.is_published,
        custom_price_override = EXCLUDED.custom_price_override,
        updated_at = now();
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
