-- ============================================================
-- TourOS Migration: 20260808_024_add_header_image_url_to_agency_branding.sql
-- Adds header_image_url to agency_branding table and updates get_agency_branding RPC
-- ============================================================

ALTER TABLE public.agency_branding 
ADD COLUMN IF NOT EXISTS header_image_url TEXT;

CREATE OR REPLACE FUNCTION public.get_agency_branding(p_agency_id UUID)
RETURNS TABLE (
    id UUID,
    agency_id UUID,
    hero_title TEXT,
    hero_subtitle TEXT,
    custom_logo_url TEXT,
    primary_color TEXT,
    footer_text TEXT,
    header_image_url TEXT
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
        b.footer_text,
        b.header_image_url
    FROM public.agency_branding b
    WHERE b.agency_id = p_agency_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
