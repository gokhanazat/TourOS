-- ============================================================================
-- TourOS Migration: 20260815_006_agency_search_and_detail_rpc.sql
-- DESCRIPTION: Acente Sorgulama, Ülke/İsim Filtreleme ve Detay Getirme RPC
-- ============================================================================

DROP FUNCTION IF EXISTS public.search_agencies(TEXT, TEXT);

CREATE OR REPLACE FUNCTION public.search_agencies(
    p_search_query TEXT DEFAULT '',
    p_country TEXT DEFAULT ''
)
RETURNS TABLE (
    company_id UUID,
    agency_name TEXT,
    operator_code TEXT,
    email TEXT,
    phone TEXT,
    country TEXT,
    address TEXT,
    tax_number TEXT,
    tax_office TEXT,
    mersis_no TEXT,
    is_active BOOLEAN,
    created_at TIMESTAMPTZ
) 
SET search_path = public
AS $$
BEGIN
    RETURN QUERY
    SELECT 
        c.id AS company_id,
        c.name AS agency_name,
        COALESCE(c.operator_code, 'TANIMSIZ') AS operator_code,
        COALESCE(c.email, '') AS email,
        COALESCE(c.phone, '') AS phone,
        COALESCE(c.address, 'Türkiye') AS country,
        COALESCE(c.address, '') AS address,
        COALESCE(c.tax_number, '') AS tax_number,
        COALESCE(c.tax_office, '') AS tax_office,
        COALESCE(c.mersis_no, '') AS mersis_no,
        c.is_active,
        c.created_at
    FROM public.companies c
    WHERE c.company_type = 'acente'
      AND (p_search_query = '' OR (
          c.name ILIKE '%' || p_search_query || '%' OR 
          c.email ILIKE '%' || p_search_query || '%' OR 
          c.operator_code ILIKE '%' || p_search_query || '%'
      ))
      AND (p_country = '' OR c.address ILIKE '%' || p_country || '%')
    ORDER BY c.name ASC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
