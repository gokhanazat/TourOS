-- ============================================================================
-- ACENTE ONAY, OTOMATİK KOD SIRASI VE REDDETME / YÖNETİM ŞEMASI
-- Dosya: supabase/migrations/20260819_019_agency_approval_tabs_and_code_sequence.sql
-- ============================================================================

-- 1. Tabloya status ve rejection_reason kolonları ekle
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS status text DEFAULT 'ACTIVE';
ALTER TABLE public.companies ADD COLUMN IF NOT EXISTS rejection_reason text;

-- 2. Sıradaki Boş Acente Kodunu Otomatik Hesaplama Fonksiyonu (Örn: AXL-0001, AXL-0002...)
CREATE OR REPLACE FUNCTION public.get_next_suggested_agency_code()
RETURNS text
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_max_num int := 0;
    v_code text;
    v_num_part text;
BEGIN
    FOR v_code IN 
        SELECT default_master_agency_code 
        FROM public.companies 
        WHERE default_master_agency_code ~ '^AXL-[0-9]+$'
    LOOP
        v_num_part := substring(v_code from '^AXL-([0-9]+)$');
        IF v_num_part IS NOT NULL AND v_num_part::int > v_max_num THEN
            v_max_num := v_num_part::int;
        END IF;
    END LOOP;

    RETURN 'AXL-' || LPAD((v_max_num + 1)::text, 4, '0');
END;
$$;

-- 3. Onay Bekleyen Acenteleri Listeleme Fonksiyonu (Reddedilenleri hariç tutar)
DROP FUNCTION IF EXISTS public.get_pending_approval_agencies();

CREATE OR REPLACE FUNCTION public.get_pending_approval_agencies()
RETURNS TABLE (
    company_id uuid,
    agency_name text,
    full_name text,
    email text,
    phone text,
    current_code text,
    is_active boolean,
    status text,
    created_at timestamp with time zone
)
LANGUAGE sql
SECURITY DEFINER
AS $$
    SELECT 
        c.id AS company_id,
        c.name AS agency_name,
        COALESCE(u.user_full_name, c.name) AS full_name,
        COALESCE(u.user_email, c.email, '') AS email,
        COALESCE(c.phone, '') AS phone,
        COALESCE(c.default_master_agency_code, c.operator_code, '') AS current_code,
        c.is_active,
        COALESCE(c.status, 'PENDING') AS status,
        c.created_at
    FROM public.companies c
    LEFT JOIN (
        SELECT DISTINCT ON (tenant_id) 
            tenant_id, 
            full_name AS user_full_name,
            email AS user_email
        FROM public.users 
        ORDER BY tenant_id, created_at ASC
    ) u ON u.tenant_id = c.id
    WHERE c.id != '00000000-0000-0000-0000-000000000001'
      AND (c.is_active = false OR c.default_master_agency_code IS NULL OR c.default_master_agency_code = '' OR c.default_master_agency_code = 'AGN-MASTER')
      AND (c.status IS NULL OR c.status != 'REJECTED')
    ORDER BY c.created_at DESC;
$$;

-- 4. Kayıtlı ve Onaylanmış Acenteleri Listeleme Fonksiyonu
DROP FUNCTION IF EXISTS public.get_registered_agencies();

CREATE OR REPLACE FUNCTION public.get_registered_agencies()
RETURNS TABLE (
    company_id uuid,
    agency_name text,
    full_name text,
    email text,
    phone text,
    agency_code text,
    is_active boolean,
    status text,
    created_at timestamp with time zone
)
LANGUAGE sql
SECURITY DEFINER
AS $$
    SELECT 
        c.id AS company_id,
        c.name AS agency_name,
        COALESCE(u.user_full_name, c.name) AS full_name,
        COALESCE(u.user_email, c.email, '') AS email,
        COALESCE(c.phone, '') AS phone,
        COALESCE(c.default_master_agency_code, c.operator_code, '') AS agency_code,
        c.is_active,
        COALESCE(c.status, 'ACTIVE') AS status,
        c.created_at
    FROM public.companies c
    LEFT JOIN (
        SELECT DISTINCT ON (tenant_id) 
            tenant_id, 
            full_name AS user_full_name,
            email AS user_email
        FROM public.users 
        ORDER BY tenant_id, created_at ASC
    ) u ON u.tenant_id = c.id
    WHERE c.id != '00000000-0000-0000-0000-000000000001'
      AND c.is_active = true
      AND c.default_master_agency_code IS NOT NULL
      AND c.default_master_agency_code != ''
      AND c.default_master_agency_code != 'AGN-MASTER'
    ORDER BY c.name ASC;
$$;

-- 5. Başvuruyu Reddetme Fonksiyonu (Soft Reject)
DROP FUNCTION IF EXISTS public.reject_agency(uuid, text);

CREATE OR REPLACE FUNCTION public.reject_agency(
    p_company_id uuid,
    p_reason text DEFAULT 'Sistem yöneticisi tarafından reddedildi'
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
    UPDATE public.companies
    SET is_active = false,
        status = 'REJECTED',
        rejection_reason = p_reason,
        updated_at = now()
    WHERE id = p_company_id;

    UPDATE public.users
    SET is_active = false,
        updated_at = now()
    WHERE tenant_id = p_company_id;

    UPDATE auth.users
    SET raw_user_meta_data = jsonb_set(
        jsonb_set(COALESCE(raw_user_meta_data, '{}'::jsonb), '{is_approved}', 'false'::jsonb),
        '{status}', '"REJECTED"'::jsonb
    )
    WHERE id IN (SELECT id FROM public.users WHERE tenant_id = p_company_id);

    RETURN json_build_object('success', true, 'company_id', p_company_id, 'status', 'REJECTED')::jsonb;
END;
$$;

-- 6. Başvuruyu Kalıcı Olarak Silme Fonksiyonu (Hard Delete)
DROP FUNCTION IF EXISTS public.delete_agency_permanently(uuid);

CREATE OR REPLACE FUNCTION public.delete_agency_permanently(
    p_company_id uuid
)
RETURNS jsonb
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user_ids uuid[];
BEGIN
    SELECT array_agg(id) INTO v_user_ids FROM public.users WHERE tenant_id = p_company_id;

    DELETE FROM public.users WHERE tenant_id = p_company_id;
    DELETE FROM public.companies WHERE id = p_company_id;

    IF v_user_ids IS NOT NULL THEN
        DELETE FROM auth.users WHERE id = ANY(v_user_ids);
    END IF;

    RETURN json_build_object('success', true, 'company_id', p_company_id, 'deleted', true)::jsonb;
END;
$$;

-- 7. Yetkilendirmeleri Ver
GRANT EXECUTE ON FUNCTION public.get_next_suggested_agency_code() TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.get_pending_approval_agencies() TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.get_registered_agencies() TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.assign_agency_code_and_activate(uuid, text) TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.reject_agency(uuid, text) TO anon, authenticated, service_role;
GRANT EXECUTE ON FUNCTION public.delete_agency_permanently(uuid) TO anon, authenticated, service_role;

NOTIFY pgrst, 'reload schema';
