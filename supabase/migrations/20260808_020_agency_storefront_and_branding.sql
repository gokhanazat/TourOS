-- ============================================================
-- TourOS Migration: 20260808_020_agency_storefront_and_branding.sql
-- Prompt 4.6.8: Acente Storefront (Kendi Web Sitesi) — Branding ve Agregasyon RPC
-- Travelata.ru tarzı çoklu operatör karşılaştırmalı tur agregatörü sorgusu ve marka özelleştirme tablosu.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. AGENCY_BRANDING TABLOSU
CREATE TABLE IF NOT EXISTS public.agency_branding (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agency_id           UUID NOT NULL REFERENCES public.companies(id) ON DELETE CASCADE,
    hero_title          TEXT NOT NULL DEFAULT 'Hayalinizdeki Turu Keşfedin',
    hero_subtitle       TEXT NOT NULL DEFAULT 'En iyi tur operatörlerinden karşılaştırmalı teklifler ve fırsatlar',
    custom_logo_url     TEXT,
    primary_color       TEXT DEFAULT '#1F4E5F',
    footer_text         TEXT DEFAULT '© 2026 Tüm Hakları Saklıdır',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_agency_branding UNIQUE (agency_id)
);

-- RLS
ALTER TABLE public.agency_branding ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "agency_branding_all" ON public.agency_branding;
CREATE POLICY "agency_branding_all" ON public.agency_branding
    USING (true);

-- 2. Acente Branding Bilgilerini Getiren RPC
CREATE OR REPLACE FUNCTION public.get_agency_branding(p_agency_id UUID)
RETURNS TABLE (
    id UUID,
    agency_id UUID,
    hero_title TEXT,
    hero_subtitle TEXT,
    custom_logo_url TEXT,
    primary_color TEXT,
    footer_text TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        b.id,
        b.agency_id,
        b.hero_title,
        b.hero_subtitle,
        b.custom_logo_url,
        b.primary_color,
        b.footer_text
    FROM public.agency_branding b
    WHERE b.agency_id = p_agency_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- 3. Travelata.ru Tarzı Karşılaştırmalı Storefront Arama ve Agregasyon RPC
CREATE OR REPLACE FUNCTION public.search_agency_storefront_tours(
    p_agency_id UUID,
    p_country TEXT DEFAULT '',
    p_min_nights INT DEFAULT 0,
    p_max_nights INT DEFAULT 30,
    p_max_budget NUMERIC DEFAULT 100000.0
) RETURNS TABLE (
    tour_id TEXT,
    title TEXT,
    code TEXT,
    country TEXT,
    city TEXT,
    nights INT,
    base_price NUMERIC,
    final_price NUMERIC,
    operator_name TEXT,
    compared_operator_count INT,
    cover_image_url TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        t.id::text AS tour_id,
        t.title,
        t.code,
        t.country,
        t.city,
        t.duration_days AS nights,
        COALESCE(t.base_price, 0.0) AS base_price,
        COALESCE(apt.custom_price_override, 
            CASE 
                WHEN conn.price_adjustment_type = 'percentage' THEN 
                    COALESCE(t.base_price, 0.0) * (1 + (conn.price_adjustment_value / 100.0))
                ELSE 
                    COALESCE(t.base_price, 0.0) + conn.price_adjustment_value
            END
        ) AS final_price,
        comp.name AS operator_name,
        (
            SELECT COUNT(DISTINCT conn2.operator_company_id)::int
            FROM public.agency_operator_connections conn2
            WHERE conn2.agency_id = p_agency_id AND conn2.status = 'ACTIVE'
        ) AS compared_operator_count,
        NULL::text AS cover_image_url
    FROM public.tours t
    JOIN public.companies comp ON comp.id = t.tenant_id
    JOIN public.agency_operator_connections conn ON conn.operator_company_id = t.tenant_id
    LEFT JOIN public.agency_published_tours apt ON apt.agency_id = p_agency_id AND apt.tour_id = t.id
    WHERE conn.agency_id = p_agency_id
      AND conn.status = 'ACTIVE'
      AND t.is_active = true
      AND COALESCE(apt.is_published, true) = true
      AND (p_country IS NULL OR p_country = '' OR LOWER(t.country) LIKE '%' || LOWER(p_country) || '%' OR LOWER(t.city) LIKE '%' || LOWER(p_country) || '%')
      AND (t.duration_days >= p_min_nights AND t.duration_days <= p_max_nights)
      AND (
        COALESCE(apt.custom_price_override, 
            CASE 
                WHEN conn.price_adjustment_type = 'percentage' THEN 
                    COALESCE(t.base_price, 0.0) * (1 + (conn.price_adjustment_value / 100.0))
                ELSE 
                    COALESCE(t.base_price, 0.0) + conn.price_adjustment_value
            END
        ) <= p_max_budget
      );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
