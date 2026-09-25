-- 20260923_001_web_b2b_custom_domain_and_agency_rpc.sql
ALTER TABLE public.companies
ADD COLUMN IF NOT EXISTS custom_domain TEXT,
ADD COLUMN IF NOT EXISTS subdomain TEXT;

CREATE OR REPLACE FUNCTION public.get_web_b2b_agency(p_identifier TEXT)
RETURNS TABLE (
    company_id UUID,
    agency_name TEXT,
    logo_url TEXT,
    operator_code TEXT,
    phone TEXT,
    email TEXT,
    whatsapp TEXT,
    address TEXT,
    is_authorized BOOLEAN
) 
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $func$
DECLARE
    v_clean TEXT := TRIM(COALESCE(p_identifier, ''));
    v_is_uuid BOOLEAN;
BEGIN
    IF v_clean = '' THEN
        RETURN;
    END IF;

    IF v_clean ILIKE 'DOMAIN:%' THEN
        v_clean := TRIM(SUBSTRING(v_clean FROM 8));
    ELSIF v_clean ILIKE 'SUB:%' THEN
        v_clean := TRIM(SUBSTRING(v_clean FROM 5));
    END IF;

    v_is_uuid := v_clean ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';

    RETURN QUERY
    SELECT 
        c.id AS company_id,
        c.name AS agency_name,
        c.logo_url AS logo_url,
        COALESCE(c.operator_code, '') AS operator_code,
        COALESCE(c.web_phone, c.phone, '') AS phone,
        COALESCE(c.web_email, c.email, '') AS email,
        COALESCE(c.web_whatsapp, '') AS whatsapp,
        COALESCE(c.web_address, c.address, '') AS address,
        (
            c.is_active IS TRUE 
            AND ('SUBDOMAIN' = ANY(c.allowed_modules) OR 'WEB_B2B' = ANY(c.allowed_modules))
        ) AS is_authorized
    FROM public.companies c
    WHERE 
        (v_is_uuid AND c.id = v_clean::UUID)
        OR (c.operator_code IS NOT NULL AND LOWER(c.operator_code) = LOWER(v_clean))
        OR (c.custom_domain IS NOT NULL AND LOWER(c.custom_domain) = LOWER(v_clean))
        OR (c.subdomain IS NOT NULL AND LOWER(c.subdomain) = LOWER(v_clean))
    LIMIT 1;
END;
$func$;

GRANT EXECUTE ON FUNCTION public.get_web_b2b_agency(TEXT) TO anon, authenticated, service_role;
NOTIFY pgrst, 'reload schema';
