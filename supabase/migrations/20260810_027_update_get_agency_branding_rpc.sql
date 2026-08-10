-- ============================================================
-- TourOS Migration: 20260810_027_update_get_agency_branding_rpc.sql
-- Updates get_agency_branding RPC to return contact_phone, contact_email, whatsapp_number, and contact_address.
-- ============================================================

DROP FUNCTION IF EXISTS public.get_agency_branding(UUID);

CREATE OR REPLACE FUNCTION public.get_agency_branding(p_agency_id UUID)
RETURNS TABLE (
    id UUID,
    agency_id UUID,
    hero_title TEXT,
    hero_subtitle TEXT,
    custom_logo_url TEXT,
    primary_color TEXT,
    footer_text TEXT,
    header_image_url TEXT,
    contact_phone TEXT,
    contact_email TEXT,
    whatsapp_number TEXT,
    contact_address TEXT
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
        b.header_image_url,
        b.contact_phone,
        b.contact_email,
        b.whatsapp_number,
        b.contact_address
    FROM public.agency_branding b
    WHERE b.agency_id = p_agency_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;
