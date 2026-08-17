-- ============================================================================
-- ACENTE ONAY & KOD ATAMA VE ÇOK KİRACILI (MULTI-TENANT) İZOLASYON ŞEMASI
-- Dosya: supabase/migrations/20260817_012_agency_approval_workflow.sql
-- ============================================================================

-- 1. Onay Bekleyen Acenteleri Listeleme Fonksiyonu
DROP FUNCTION IF EXISTS public.get_pending_approval_agencies();

CREATE OR REPLACE FUNCTION public.get_pending_approval_agencies()
RETURNS TABLE (
    company_id uuid,
    agency_name text,
    email text,
    phone text,
    current_code text,
    is_active boolean,
    created_at timestamp with time zone
)
LANGUAGE sql
SECURITY DEFINER
AS $$
    SELECT 
        c.id AS company_id,
        c.name AS agency_name,
        COALESCE(u.user_email, c.email, '') AS email,
        COALESCE(c.phone, '') AS phone,
        COALESCE(c.default_master_agency_code, c.operator_code, '') AS current_code,
        c.is_active,
        c.created_at
    FROM public.companies c
    LEFT JOIN (
        SELECT DISTINCT ON (tenant_id) 
            tenant_id, 
            email AS user_email
        FROM public.users 
        ORDER BY tenant_id, created_at ASC
    ) u ON u.tenant_id = c.id
    WHERE c.id != '00000000-0000-0000-0000-000000000001'
      AND (c.is_active = false OR c.default_master_agency_code IS NULL OR c.default_master_agency_code = '')
    ORDER BY c.created_at DESC;
$$;

-- 2. Admin Tarafından Acente Kodu Atama ve Hesabı Aktifleştirme Fonksiyonu
DROP FUNCTION IF EXISTS public.assign_agency_code_and_activate(uuid,text);

CREATE OR REPLACE FUNCTION public.assign_agency_code_and_activate(
    p_company_id uuid,
    p_agency_code text
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    clean_code text := UPPER(TRIM(p_agency_code));
BEGIN
    IF clean_code IS NULL OR clean_code = '' THEN
        RAISE EXCEPTION 'Acente kodu boş olamaz.';
    END IF;

    -- Şirket tablosunu güncelle
    UPDATE public.companies
    SET default_master_agency_code = clean_code,
        operator_code = clean_code,
        is_active = true,
        updated_at = now()
    WHERE id = p_company_id;

    -- Acenteye bağlı kullanıcıları aktifleştir
    UPDATE public.users
    SET is_active = true,
        updated_at = now()
    WHERE tenant_id = p_company_id;

    -- Supabase Auth metadata güncelle
    UPDATE auth.users
    SET raw_user_meta_data = jsonb_set(
        jsonb_set(COALESCE(raw_user_meta_data, '{}'::jsonb), '{agency_code}', to_jsonb(clean_code)),
        '{is_approved}',
        'true'::jsonb
    )
    WHERE id IN (SELECT id FROM public.users WHERE tenant_id = p_company_id);

    RETURN json_build_object('success', true, 'agency_code', clean_code, 'company_id', p_company_id)::jsonb;
END;
$$;
