-- ============================================================================
-- TourOS Migration: 20260815_005_agency_approval_queue_and_code_assignment.sql
-- DESCRIPTION: Onay Bekleyen Acenteler Kuyruğu ve Manuel Acente Kodu Tanımlama RPC
-- ============================================================================

DROP FUNCTION IF EXISTS public.get_pending_approval_agencies();
DROP FUNCTION IF EXISTS public.assign_agency_code_and_activate(UUID, TEXT);

-- 1. Bekleyen Acenteleri Listelemek İçin RPC
CREATE OR REPLACE FUNCTION public.get_pending_approval_agencies()
RETURNS TABLE (
    company_id UUID,
    agency_name TEXT,
    email TEXT,
    phone TEXT,
    current_code TEXT,
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
        COALESCE(c.email, '') AS email,
        COALESCE(c.phone, '') AS phone,
        COALESCE(c.operator_code, '') AS current_code,
        c.is_active,
        c.created_at
    FROM public.companies c
    WHERE c.company_type = 'acente'
      AND (c.is_active = FALSE OR c.operator_code IS NULL OR c.operator_code = '')
    ORDER BY c.created_at DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 2. Acenteye Özel Kod Atayıp Aktifleştiren RPC
CREATE OR REPLACE FUNCTION public.assign_agency_code_and_activate(
    p_company_id UUID,
    p_agency_code TEXT
) 
RETURNS BOOLEAN 
SET search_path = public
AS $$
DECLARE
    v_agency_name TEXT;
    v_clean_code TEXT;
BEGIN
    v_clean_code := UPPER(TRIM(p_agency_code));

    IF v_clean_code IS NULL OR LENGTH(v_clean_code) = 0 THEN
        RAISE EXCEPTION 'Acente kodu boş olamaz.';
    END IF;

    SELECT c.name INTO v_agency_name
    FROM public.companies c
    WHERE c.id = p_company_id;

    IF v_agency_name IS NULL THEN
        RAISE EXCEPTION 'Belirtilen acente bulunamadı.';
    END IF;

    UPDATE public.companies
    SET 
        operator_code = v_clean_code,
        is_active = TRUE,
        updated_at = NOW()
    WHERE id = p_company_id;

    INSERT INTO public.agency_referral_codes (
        tenant_id,
        agency_code,
        agency_name,
        is_active
    ) VALUES (
        p_company_id,
        v_clean_code,
        v_agency_name,
        TRUE
    )
    ON CONFLICT (agency_code) DO UPDATE SET
        agency_name = EXCLUDED.agency_name,
        is_active = TRUE;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
